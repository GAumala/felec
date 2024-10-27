(ns com.gaumala.felec.comprobantes
  (:require [com.gaumala.utils.spec :refer [validate]]
            [com.gaumala.sri.clave-acceso :refer [gen-clave-acceso]]))

(defn build-factura-map
  [defaults ruc codigo input]
  {:post [(validate :sri.comprobantes/factura %)]}
  (let [full-defaults (conj {:codDoc "01"
                             :ruc ruc
                             :codigoNumerico codigo}
                            defaults)
        claveAcceso (gen-clave-acceso (conj full-defaults
                                            (:infoTributaria input)
                                            (:infoFactura input)))
        infoTributaria (conj {:claveAcceso claveAcceso}
                             (select-keys full-defaults
                                          [:ambiente
                                           :razonSocial
                                           :ruc
                                           :codDoc
                                           :estab
                                           :ptoEmi
                                           :secuencial
                                           :dirMatriz
                                           :nombreComercial])
                             (dissoc (:infoTributaria input)
                                     :ruc
                                     :razonSocial
                                     :ambiente
                                     :codDoc))
        infoFactura (conj (select-keys defaults
                                       [:dirEstablecimiento
                                        :obligadoContabilidad
                                        :contribuyenteEspecial])
                          (:infoFactura input))]
    (-> input
        (assoc :infoTributaria infoTributaria)
        (assoc :infoFactura infoFactura))))
