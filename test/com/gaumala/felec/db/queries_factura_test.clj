(ns com.gaumala.felec.db.queries-factura-test
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
            [matcher-combinators.test :refer [thrown-match?]]
            [clojure.test :refer [deftest is use-fixtures]]))

(use-fixtures :once reset-database)

(deftest should-retrieve-inserted-factura
  (let [ruc "1792146739001"
        codigo "00001021"
        secuencial 1
        estado ESTADO_RECIBIDA
        tipo TIPO_FACTURA
        last_update 1000
        expected {:ruc ruc
                  :tipo tipo
                  :codigo codigo
                  :secuencial secuencial
                  :estado estado
                  :last_update last_update
                  :content mocked-factura-content
                  :xml mocked-factura-xml}
        actual (do (q/insert-comprobante ctx {:ruc ruc
                                              :tipo tipo
                                              :codigo codigo
                                              :secuencial secuencial
                                              :estado estado
                                              :last_update last_update
                                              :content mocked-factura-content
                                              :xml mocked-factura-xml})
                   (q/find-comprobante-by-codigo ctx ruc tipo codigo))]
    (is (= expected actual))))

(deftest should-retrieve-inserted-factura-xml
  (let [ruc "1792146739001"
        codigo "00001022"
        secuencial 2
        estado ESTADO_RECIBIDA
        tipo TIPO_FACTURA
        last_update 1000
        expected {:ruc ruc
                  :tipo tipo
                  :codigo codigo
                  :xml mocked-factura-xml}
        actual (do (q/insert-comprobante ctx {:ruc ruc
                                              :tipo tipo
                                              :codigo codigo
                                              :secuencial secuencial
                                              :estado estado
                                              :last_update last_update
                                              :content mocked-factura-content
                                              :xml mocked-factura-xml})
                   (q/find-comprobante-xml-by-codigo ctx ruc tipo codigo))]
    (is (= expected actual))))

(deftest inserting-duplicate-factura-should-throw-exception-info
  (let [ruc "1792146739001"
        tipo TIPO_FACTURA
        codigo "00001023"
        secuencial 3
        estado ESTADO_PENDIENTE
        factura {:ruc ruc
                 :tipo tipo
                 :codigo codigo
                 :secuencial secuencial
                 :estado estado
                 :content mocked-factura-content
                 :xml mocked-factura-xml}]
    (q/insert-comprobante ctx factura)
    (is (thrown-match? {:type :sql-unique-constraint}
                       (q/insert-comprobante ctx factura)))))

(deftest should-return-nil-if-factura-is-not-found
  (let [ruc "1792146739001"
        codigo "00009999"
        tipo TIPO_FACTURA
        xml-row (q/find-comprobante-xml-by-codigo ctx ruc tipo codigo)
        data-row (q/find-comprobante-data-by-codigo ctx ruc tipo codigo)]
    (is (nil? xml-row))
    (is (nil? data-row))))

(deftest should-update-factura-estado
  (let [ruc "1792146739001"
        tipo TIPO_FACTURA
        codigo "00003001"
        expected ESTADO_AUTORIZADO
        actual (do (q/insert-comprobante ctx
                                         {:ruc ruc
                                          :tipo tipo
                                          :codigo codigo
                                          :secuencial 31
                                          :estado ESTADO_PENDIENTE
                                          :content mocked-factura-content
                                          :xml mocked-factura-xml})
                   (q/update-comprobante-estado ctx ruc tipo codigo ESTADO_AUTORIZADO)
                   (-> ctx
                       (q/find-comprobante-by-codigo ruc tipo codigo)
                       (:estado)))]
    (is (= expected actual))))
