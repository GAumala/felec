(ns com.gaumala.sri.web-service
  (:require [com.gaumala.sri.decoders :as decoders]
            [com.gaumala.sri.encoders :as encoders]
            [org.httpkit.client :as client]))

(def PRUEBAS_URLS
  {:recepcion "https://celcer.sri.gob.ec/comprobantes-electronicos-ws/RecepcionComprobantesOffline?wsdl"
   :autorizacion "https://celcer.sri.gob.ec/comprobantes-electronicos-ws/AutorizacionComprobantesOffline?wsdl"})

(def PROD_URLS
  {:recepcion "https://cel.sri.gob.ec/comprobantes-electronicos-ws/RecepcionComprobantesOffline?wsdl"
   :autorizacion "https://cel.sri.gob.ec/comprobantes-electronicos-ws/AutorizacionComprobantesOffline?wsdl"})

(defn- get-ambiente-urls [ambiente]
  (condp = ambiente
    1 PRUEBAS_URLS
    2 PROD_URLS
    (throw (Exception. (str "ambiente desconocido: " ambiente)))))

(defn validar-comprobante [ctx xml-string]
  (let [url (-> (:ambiente ctx)
                get-ambiente-urls
                :recepcion)
        req-body (encoders/validar-comprobante xml-string)
        response @(client/post url
                               {:body req-body
                                :as :text})
        res-text (:body response)
        decoded (decoders/respuesta-recepcion-comprobante res-text)]
    (if (nil? decoded)
      (throw (ex-info (str "failed request validar-comprobante. "
                           "status: " (:status response))
                      {:type :web-service-call
                       :url url
                       :status (:status response)
                       :response res-text}))
      decoded)))

(defn autorizacion-comprobante [ctx clave-acceso]
  (let [url (-> (:ambiente ctx)
                get-ambiente-urls
                :autorizacion)
        req-body (encoders/autorizacion-comprobante clave-acceso)
        response @(client/post url
                               {:body req-body
                                :as :text})
        res-text (:body response)
        decoded (decoders/respuesta-autorizacion-comprobante res-text)]
    (if (nil? decoded)
      (throw (ex-info (str "failed request autorizacion-comprobante. "
                           "status: " (:status response))
                      {:type :web-service-call
                       :url url
                       :status (:status response)
                       :response res-text}))
      decoded)))
