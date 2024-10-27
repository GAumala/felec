# felec

Librería Clojure para emitir comprobantes electrónicos, guardándolos en una
base de datos local.

Toda información sensible se guarda encriptada. Esto incluye los comprobantes
firmados en su totalidad y los datos del contribuyente.

```clojure
(require '[com.gaumala.felec.db.h2 :as h2])
(require '[com.gaumala.felec :as felec])

;; Primero hay que crear un "contexto"
(def ctx {:ds (h2/get-ds \"./.db\") :ambiente 1 :aes-pwd \"mypass1234\"})

;; Insertar un contribuyente con su firma electrónica en la base de datos
;; local para poder emitir sus facturas
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

;; Crea la primera factura firmada en la base de datos local
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

;; Envía la factura al SRI
(felec/send-comprobante ctx \"1792146739001\" \"factura\" \"00000001\")
;; => {:estado \"RECIBIDA\" mensajes ()}

;; Consulta el estado de la autorización
(felec/check-autorizacion ctx \"1792146739001\" \"factura\" \"00000001\")
;; => {:claveAccesoConsultada \"2410202401179214673900110020010000000010000000113\"
;;     :numeroComprobantes \"1\"
;;     :autorizaciones [{:estado \"AUTORIZADO\"
;;                       :numeroAutorizacion \"2410202401179214673900110020010000000010000000113\"
;;                       :fechaAutorizacion \"2024-10-24T13:03:17-05:00\"
;;                       :ambiente \"PRUEBAS\"
;;                       :comprobante \"...\"}]}
```

Para más información revisa [la Documentación](https://gaumala.github.io/felec/).

## License



Copyright 2024 Gabriel Aumala

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

