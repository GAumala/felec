(ns com.gaumala.felec.keystore
  (:require [clojure.java.io :refer [input-stream]]
            [com.gaumala.utils.spec :refer [validate]]
            [com.gaumala.sri.xades-bes :refer [sign-comprobante]]))

(defn run-keystore-test
  [{:keys [keystore keystore-pwd]}]
  {:post [(validate string? %)]}
  (let [store {:stream (input-stream keystore)
               :pass keystore-pwd}
        xml-string "<factura id=\"comprobante\"><bar/></factura>"]
    (sign-comprobante xml-string store)))
