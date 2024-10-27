(ns com.gaumala.felec.db.queries-find-max-secuencial-by-ruc-test
  (:require [com.gaumala.felec.constants :refer [ESTADO_AUTORIZADO
                                                 ESTADO_PENDIENTE
                                                 TIPO_FACTURA]]
            [com.gaumala.felec.mocks :refer [mocked-factura-content
                                             mocked-factura-xml]]
            [com.gaumala.felec.db.helper :refer [ctx
                                                 del-comprobantes
                                                 reset-database]]
            [com.gaumala.felec.db.queries :as q]
            [clojure.test :refer [deftest is use-fixtures]]))

(use-fixtures :once reset-database)

(deftest find-max-secuencial-by-ruc-should-return-correct-int
  (del-comprobantes)
  (let [ruc "1792146739001"
        tipo TIPO_FACTURA
        expected 22
        actual (do (q/insert-comprobante ctx
                                         {:ruc ruc
                                          :tipo tipo
                                          :codigo "00002006"
                                          :secuencial 20
                                          :estado ESTADO_PENDIENTE
                                          :content mocked-factura-content
                                          :xml mocked-factura-xml})
                   (q/insert-comprobante ctx
                                         {:ruc ruc
                                          :tipo tipo
                                          :codigo "00002007"
                                          :secuencial 21
                                          :estado ESTADO_AUTORIZADO
                                          :content mocked-factura-content
                                          :xml mocked-factura-xml})
                   (q/insert-comprobante ctx
                                         {:ruc ruc
                                          :tipo tipo
                                          :codigo "00002008"
                                          :secuencial 22
                                          :estado ESTADO_PENDIENTE
                                          :content mocked-factura-content
                                          :xml mocked-factura-xml})
                   (q/find-max-secuencial-by-ruc ctx ruc tipo))]
    (is (= expected actual))))

(deftest find-max-secuencial-by-ruc-returns-nil-if-ruc-does-not-exist
  (let [ruc "1792146739003"
        tipo TIPO_FACTURA
        secuencial (q/find-max-secuencial-by-ruc ctx ruc tipo)]
    (is (nil? secuencial))))
