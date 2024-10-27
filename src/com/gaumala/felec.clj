(ns com.gaumala.felec
  "API publico para generar comprobantes electrónicos, insertarlos a una base
  de datos local, enviarlos al SRI, y consultar su estado de autorización.
  
  Todas estas funciones reciben como primer parámetro un mapa \"contexto\"
  con los sigientes campos:

  | key         | Descripción |
  | ------------|-------------|
  | `:ds`       | `DataSource` para conectarse a la base de datos.
  | `:ambiente` | Ambiente a usar en los comprobantes electrónicos. 1 para pruebas y 2 para producción
  | `:aes-pwd`  | Contraseña para encriptar información personal en la base de datos.

  Este es un ejemplo de como crear un contexto con conexión a una base de datos H2 en
  ambiente de pruebas:

  ```clojure
  (require '[com.gaumala.felec.db.h2 :as h2])
  (def ctx {:ds (h2/get-ds \"./.db\") :ambiente 1 :aes-pwd \"mypass1234\"})
  ```"
  {:doc/format :markdown}
  (:require [clojure.spec.alpha :as s]
            [clojure.java.io :refer [input-stream]]
            [com.gaumala.utils.file :refer [file->bytes]]
            [com.gaumala.utils.spec :refer [validate]]
            [com.gaumala.utils.time :refer [get-unix-time]]
            [com.gaumala.sri.encoders :as encoders]
            [com.gaumala.sri.web-service :as sri-ws]
            [com.gaumala.sri.xades-bes :refer [sign-comprobante]]
            [com.gaumala.felec.comprobantes :refer [build-factura-map]]
            [com.gaumala.felec.constants :refer [ESTADO_PENDIENTE
                                                 TIPO_FACTURA]]
            [com.gaumala.felec.keystore :refer [run-keystore-test]]
            [com.gaumala.felec.predicates :refer [real-path?]]
            [com.gaumala.sri.predicates :refer [some-string?]]
            [com.gaumala.felec.db.queries :as q]))

(s/def :felec/keystore (s/or :bytes bytes? :path real-path?))
(s/def :felec/keystore-pwd some-string?)
(s/def :felec/ultimo-secuencial int?)

(s/def :felec.contribuyente/datos
  (s/keys :req-un [:sri.comprobantes/razonSocial
                   :sri.comprobantes/dirMatriz]
          :opt-un [:sri.comprobantes/estab
                   :sri.comprobantes/ptoEmi
                   :sri.comprobantes/nombreComercial
                   :sri.comprobantes/dirEstablecimiento
                   :sri.comprobantes/contribuyenteEspecial
                   :sri.comprobantes/obligadoContabilidad
                   :sri.comprobantes/moneda]))

(s/def :felec/contribuyente
  (s/keys :req-un [:felec/ruc
                   :felec/keystore
                   :felec.contribuyente/datos]
          :opt-un [:felec/keystore-pwd]))

(s/def :felec/infoTributaria
  (s/keys :opt-un [:sri.comprobantes/estab
                   :sri.comprobantes/ptoEmi
                   :sri.comprobantes/secuencial
                   :sri.comprobantes/nombreComercial
                   :sri.comprobantes/claveAcceso
                   :sri.comprobantes/tipoEmision]))

(s/def :felec/factura
  (s/keys :req-un [:felec/infoTributaria
                   :sri.comprobantes/infoFactura
                   :sri.comprobantes/detalles]
          :opt-un [:sri.comprobantes/infoAdicional]))

(defn- read-input-keystore [keystore]
  (if (string? keystore) (file->bytes keystore) keystore))

(defn- get-contribuyente-by-ruc [ctx ruc]
  (if-let [row (q/find-contribuyente-by-ruc ctx ruc)]
    row
    (throw (ex-info (str "Contribuyente con ruc " ruc " no encontrado.")
                    {:type :bad-input
                     :var :ruc
                     :value ruc
                     :problem :not-found}))))

(defn- find-comprobante-xml-by-codigo [ctx ruc tipo codigo]
  (if-let [row (q/find-comprobante-xml-by-codigo ctx ruc tipo codigo)]
    row
    (throw (ex-info (str "Comprobante no encontrado. ruc: "
                         ruc "tipo: " tipo "codigo: " codigo)
                    {:type :bad-input
                     :var :ruc-tipo-codigo
                     :value [ruc tipo codigo]
                     :problem :not-found}))))

(defn- find-comprobante-data-by-codigo [ctx ruc tipo codigo]
  (if-let [row (q/find-comprobante-data-by-codigo ctx ruc tipo codigo)]
    row
    (throw (ex-info (str "Comprobante no encontrado. ruc: "
                         ruc "tipo: " tipo "codigo: " codigo)
                    {:type :bad-input
                     :var :ruc-tipo-codigo
                     :value [ruc tipo codigo]
                     :problem :not-found}))))

(defn- get-next-secuencial [ctx ruc tipo comprobante]
  ;; si el usuario ingreso el secuencial en el comprobante hay que usar eso
  (if-let [input-secuencial (get-in comprobante [:infoTributaria :secuencial])]
    (Integer/parseInt input-secuencial)
    ;; de lo contrario hay que sacar el secuencial de la db
    (if-let [stored-secuencial (q/find-max-secuencial-by-ruc ctx tipo ruc)]
      (inc stored-secuencial)
      ;; si no hay secuencial en db, arrojar error
      (throw (ex-info (str "El parametro secuencial es requerido")
                      {:type :secuencial-missing})))))

(defn insert-contribuyente
  "Inserta un nuevo contribuyente en la base de datos. Para poder emitir
  comprobantes, debes primero tener el contribuyente en la base de datos.
  
  El mapa input necesita los siguiente parámetros:

  | key         | Descripción |
  | ------------|-------------|
  | `:ruc`      | El número de RUC del contribuyente. Requerido.
  | `:keystore` | El keystore del contribuyente para firmar sus comprobantes. Puede ser una ruta a un archivo `.p12` o los bytes del archivo. Requerido.
  | `:datos`    | Un mapa con datos del contribuyente que se incluyen en todos los comprobantes. `:razonSocial` y `:dirMatriz` son obligatorios. Opcionalmente también puedes incluir `:estab`, `:ptoEmi`, `:nombreComercial`, `:dirEstablecimiento`, `:contribuyenteEspecial`, `:obligadoContabilidad` y `:moneda`. Requerido.
  | `:keystore-pwd` | Contraseña para acceder a las llaves privadas del keystore y poder firmar. Si incluyes este dato, Se realizará una firma con un documento de prueba. Si la firma falla se arroja una excepción antes de insertar el contribuyente. La contraseña nunca se guarda. Opcional.

  Este es un ejemplo de como insertar un contribuyente:
   ```clojure
  (require '[com.gaumala.felec :as felec])

  (def datos {:razonSocial \"Distribuidora de Suministros Nacional S.A.\"
                    :estab \"002\"
                    :ptoEmi \"001\"
                    :dirMatriz \"Enrique Guerrero Portilla OE1-34 AV. Galo Plaza Lasso\"
                    :dirEstablecimiento \"Sebastián Moreno S/N Francisco García\"
                    :contribuyenteEspecial \"5368\"
                    :obligadoContabilidad \"SI\"})
  (felec/insert-contribuyente ctx {:ruc \"1792146739001\"
                                   :keystore \"/home/gabriel/Documents/mi_firma.p12\"
                                   :datos datos})
   ```

  Si ocurre algún error, se arroja un `ex-info` cuya data tendrá un key `:type` con
  uno de los siguientes valores:

  | type                     | Descripción |
  | -------------------------|-------------|
  | `:spec-validation`       | Estás ingresando parámetros inválidos en la función. Puedes revisar `:data` para una explicación
  | `:xml-signature`         | La firma del documento de prueba falló. Quizas ingresaste mal `:keystore-pwd`, o tu keystore es inválido.
  | `:sql-unique-constraint` | Ya existe un contribuyente con ese RUC."
  {:doc/format :markdown}
  [ctx input]
  {:pre [(validate :felec/contribuyente input)]}
  (let [contrib (-> input
                    (update :keystore read-input-keystore))]
    (when (:keystore-pwd contrib)
      (run-keystore-test contrib))
    (q/insert-contribuyente ctx contrib)))

(defn create-factura
  "Crea un nuevo comprobante de tipo factura (versión 1.0.0) en la base de datos.
  Para poder envíar un comprobante al SRI, debes primero tener el comprobante en
  la base de datos.

  Cada comprobante debe tener una combinación única de RUC, tipo y código de 8 dígitos.
  Este código es el mismo que se usa para la clave de acceso.
  
  El segundo parámetro es un mapa con los siguiente parámetros:

  | key             | Descripción |
  | ----------------|-------------|
  | `:ruc`          | El número de RUC del contribuyente. Requerido.
  | `:codigo`       | Código de 8 dígitos. Requerido.
  | `:keystore-pwd` | Contraseña del keystore para firmar el comprobante. Requerido.

  El tercer parámetro es un mapa con los datos de la factura, de acuerdo al spec
  `:felec/factura`. Este spec trata de ser idéntico al formato XML de las 
  facturas del SRI, pero usando mapas de Clojure. 
  
  Este es un ejemplo de como crear una factura:

  ```clojure
  (require '[com.gaumala.felec :as felec])

  (def factura
    {:infoTributaria {:secuencial \"000000001\"}
     :infoFactura {:fechaEmision \"24/10/2024\"
                   :tipoIdentificacionComprador \"04\"
                   :razonSocialComprador \"PRUEBAS SERVICIO RENTAS INTERNAS\"
                   :identificacionComprador \"1713328506001\"
                   :direccionComprador \"salinas y santiago\"
                   :totalSinImpuestos 200.0
                   :totalDescuento 0
                   :totalConImpuestos [{:codigo 2
                                      :codigoPorcentaje 4
                                      :baseImponible 200
                                      :valor 30}]
                   :propina 0
                   :importeTotal \"230\"
                   :pagos [{:formaPago \"01\"
                            :total 230
                            :plazo 30
                            :unidadTiempo \"dias\"}]}
     :detalles [{:codigoPrincipal \"001\"
                 :descripcion \"PRESTACION DE SERVICIOS PROFESIONALES\"
                 :cantidad 1
                 :precioUnitario 200
                 :descuento 0
                 :precioTotalSinImpuesto 200
                 :impuestos [{:codigo 2
                              :codigoPorcentaje 4
                              :baseImponible 200
                              :valor 30
                              :tarifa 15}]}]})

  (felec/create-factura ctx {:ruc \"1792146739001\"
                             :codigo \"00000001\"
                             :keystore-pwd  \"my-keys-pwd12346\"} factura)
  ```
  En `:infoTributaria` no es necesario incluir todos los campos requeridos, ya
  que la base datos ya obtiene esos datos al insertar el contribuyente. Incluso el
  secuencial se puede puede omitir si ya tienes facturas en la base de datos.
  Simplemente se calcula el siguiente secuencial en base a tus facturas anteriores.

  Si ocurre algún error, se arroja un `ex-info` cuya data tendrá un key `:type` con
  uno de los siguientes valores:

  | type                     | Descripción |
  | -------------------------|-------------|
  | `:spec-validation`       | Estás ingresando parámetros inválidos en la función. Puedes revisar `:data` para una explicación.
  | `:bad-input`             | No se pudo encontrar un contribuyente en la base de datos con ese RUC.
  | `:xml-signature`         | No se pudo firmar el comprobante Quizas ingresaste mal `:keystore-pwd`, o tu keystore es inválido.
  | `:sql-unique-constraint` | Ya existe una factura con ese RUC y código de 8 dígitos.
  | `:secuencial-missing`    | Necesitas incluir el campo `:secuencial` en la factura."
  {:doc/format :markdown}
  [ctx {:keys [ruc codigo keystore-pwd]} input]
  {:pre [(validate :felec/factura input)]}
  (let [tipo TIPO_FACTURA
        contrib (get-contribuyente-by-ruc ctx ruc)
        secuencial (get-next-secuencial ctx ruc tipo input)
        factura-map (-> (:datos contrib)
                        (conj {:ambiente (:ambiente ctx)
                               :secuencial (format "%09d" secuencial)})
                        (build-factura-map ruc codigo input))
        keystore {:stream (input-stream (:keystore contrib))
                  :pass keystore-pwd}
        signed (-> factura-map
                   (encoders/factura codigo)
                   (sign-comprobante keystore))
        row {:ruc ruc
             :codigo codigo
             :secuencial secuencial
             :tipo tipo
             :estado ESTADO_PENDIENTE
             :content factura-map
             :xml signed
             :last_update (get-unix-time)}]
    (q/insert-comprobante ctx row)
    (dissoc row :content :xml)))

(defn send-comprobante
  "Envía un comprobante de la base de datos al SRI a través del web service
  `validarComprobante`. Los últimos tres parámetros son el RUC, tipo y código
  de 8 dígitos que identifican al comprobante en la base de datos. Devuelve
  la respuesta del SRI después de actualizar el estado del comprobante en
  la base de datos (\"RECIBIDA\" o \"RECHAZADA\").
  
  Ten en cuenta que actualmente sólo soportamos el tipo  \"factura\". 

  Este es un ejemplo de como envíar una factura:

  ```clojure
  (require '[com.gaumala.felec :as felec])

  (felec/send-comprobante ctx \"1792146739001\" \"factura\" \"00000001\")
  ;; => {:estado \"RECIBIDA\" mensajes ()}
   ```
  Si ocurre algún error, se arroja un `ex-info` cuya data tendrá un key `:type` con
  uno de los siguientes valores:

  | type                     | Descripción |
  | -------------------------|-------------|
  | `:bad-input`             | No se pudo encontrar esa combinación de RUC, tipo y código de 8 dígitos en la base de datos.
  | `:web-service-call`      | Se produjo un error al llamar al web service del SRI. Puedes consultar los campos `:url`, `:status`, y `:response` para mas información. 
  "
  {:doc/format :markdown}
  [ctx ruc tipo codigo]
  (let [compr (find-comprobante-xml-by-codigo ctx ruc tipo codigo)
        respuesta (sri-ws/validar-comprobante ctx (:xml compr))]
    (q/update-comprobante-estado ctx ruc tipo codigo (:estado respuesta))
    respuesta))

(defn check-autorizacion
  "Revisa el estado de la autorización de un comprobante en la base de datos 
  a través del web service `autorizacionComprobante`. Los últimos tres 
  parámetros son el RUC, tipo y código de 8 dígitos que identifican al 
  comprobante en la base de datos. Devuelve
  la respuesta del SRI después de actualizar el estado del comprobante en
  la base de datos (\"AUTORIZADO\" o \"DEVUELTA\").
  
  Ten en cuenta que actualmente sólo soportamos el tipo  \"factura\". 

  Este es un ejemplo de como consultar la autorización de una factura:

  ```clojure
  (require '[com.gaumala.felec :as felec])

  (felec/check-autorizacion ctx \"1792146739001\" \"factura\" \"00000001\")
  ;; => {:claveAccesoConsultada \"2410202401179214673900110020010000000010000000113\"
  ;;     :numeroComprobantes \"1\"
  ;;     :autorizaciones [{:estado \"AUTORIZADO\"
  ;;                       :numeroAutorizacion \"2410202401179214673900110020010000000010000000113\"
  ;;                       :fechaAutorizacion \"2024-10-24T13:03:17-05:00\"
  ;;                       :ambiente \"PRUEBAS\"
  ;;                       :comprobante \"...\"}]}
   ```
  Si ocurre algún error, se arroja un `ex-info` cuya data tendrá un key `:type` con
  uno de los siguientes valores:

  | type                     | Descripción |
  | -------------------------|-------------|
  | `:bad-input`             | No se pudo encontrar esa combinación de RUC, tipo y código de 8 dígitos en la base de datos.
  | `:web-service-call`      | Se produjo un error al llamar al web service del SRI. Puedes consultar los campos `:url`, `:status`, y `:response` para mas información."
  {:doc/format :markdown}
  [ctx ruc tipo codigo]
  (if-let [clave (-> ctx
                     (find-comprobante-data-by-codigo ruc tipo codigo)
                     (get-in [:content :infoTributaria :claveAcceso]))]
    (let [respuesta (sri-ws/autorizacion-comprobante ctx clave)
          estado (-> (:autorizaciones respuesta)
                     (first)
                     (:estado))]
      (q/update-comprobante-estado ctx ruc tipo codigo estado)
      respuesta)
    (throw (ex-info (str "Clave de acceso no encontrada en DB. ruc:"
                         ruc "tipo:" tipo "codigo:" codigo)
                    {:type :claveAcceso-missing}))))
