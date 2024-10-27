(ns com.gaumala.felec.db.queries-find-comprobantes-with-estado-test
  (:require [com.gaumala.felec.constants :refer [ESTADO_AUTORIZADO
                                                 ESTADO_PENDIENTE
                                                 ESTADO_RECIBIDA
                                                 TIPO_FACTURA]]
            [com.gaumala.felec.mocks :refer [mocked-factura-content
                                             mocked-factura-xml]]
            [com.gaumala.felec.db.helper :refer [ctx
                                                 reset-database
                                                 del-comprobantes]]
            [com.gaumala.felec.db.queries :as q]
            [clojure.test :refer [deftest is use-fixtures]]))

(use-fixtures :once reset-database)

(deftest find-comprobantes-with-estado-should-return-multiple-items
  (del-comprobantes)
  (let [ruc "1792146739001"
        tipo TIPO_FACTURA
        expected [{:ruc ruc
                   :tipo tipo
                   :codigo "00002001"}
                  {:ruc ruc
                   :tipo tipo
                   :codigo "00002003"}]
        actual (do (q/insert-comprobante ctx
                                         {:ruc ruc
                                          :tipo tipo
                                          :codigo "00002001"
                                          :secuencial 11
                                          :estado ESTADO_PENDIENTE
                                          :content mocked-factura-content
                                          :xml mocked-factura-xml})
                   (q/insert-comprobante ctx
                                         {:ruc ruc
                                          :tipo tipo
                                          :codigo "00002002"
                                          :secuencial 12
                                          :estado ESTADO_AUTORIZADO
                                          :content mocked-factura-content
                                          :xml mocked-factura-xml})
                   (q/insert-comprobante ctx
                                         {:ruc ruc
                                          :tipo tipo
                                          :codigo "00002003"
                                          :secuencial 13
                                          :estado ESTADO_PENDIENTE
                                          :content mocked-factura-content
                                          :xml mocked-factura-xml})
                   (q/insert-comprobante ctx
                                         {:ruc ruc
                                          :tipo tipo
                                          :codigo "00002004"
                                          :secuencial 14
                                          :estado ESTADO_RECIBIDA
                                          :content mocked-factura-content
                                          :xml mocked-factura-xml})
                   (q/find-comprobantes-with-estado ctx tipo ESTADO_PENDIENTE))]
    (is (= expected actual))))

(deftest find-comprobantes-with-estado-should-return-empty-seq-if-db-is-empty
  (del-comprobantes)
  (let [tipo TIPO_FACTURA
        actual (q/find-comprobantes-with-estado ctx tipo ESTADO_AUTORIZADO)]
    (is (empty? actual))))


