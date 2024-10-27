(ns com.gaumala.felec.api-send-comprobante-test
  (:require [com.gaumala.felec :as felec]
            [com.gaumala.felec.constants :refer [ESTADO_RECIBIDA
                                                 TIPO_FACTURA]]
            [com.gaumala.felec.db.queries :as queries]
            [com.gaumala.felec.mocks :refer [mocked-ctx]]
            [com.gaumala.sri.web-service :as sri-ws]
            [clojure.test :refer [deftest is]]
            [matcher-combinators.test :refer [thrown-match?]]
            [spy.core :refer [calls stub stub-throws]]
            [spy.assert :as assert]))

(def ctx mocked-ctx)
(def ruc "1792146739001")
(def tipo TIPO_FACTURA)
(def codigo "00000001")

(def factura {:ruc "1792146739001"
              :tipo tipo
              :codigo codigo
              :xml "<factura/>"})

(deftest throws-with-type-bad-input-if-comprobante-not-found
  ;; stub find-comprobante-xml-by-codigo to not find anything
  (let [query-fn (stub nil)]
    ;; first assert that the correct error is thrown
    (is (thrown-match?
         {:type :bad-input}
         (with-redefs [queries/find-comprobante-xml-by-codigo query-fn]
           (felec/send-comprobante ctx ruc tipo codigo))))
    ;; then assert that the correct params are sent to the query
    (assert/called-with? query-fn ctx ruc tipo codigo)))

(deftest calls-web-service-with-correct-xml-and-returns-response
        ;; stub find-comprobante-xml-by-codigo to return valid comprobante
  (let [query-fn (stub factura)
        ;; stub update-comprobante-estado to succeed
        update-fn (stub nil)
        expected-res {:estado ESTADO_RECIBIDA :mensajes []}
        ;; stub validar-comprobante to succeed
        web-service-fn (stub expected-res)
        actual-res (with-redefs
                    [queries/find-comprobante-xml-by-codigo query-fn
                     queries/update-comprobante-estado update-fn
                     sri-ws/validar-comprobante web-service-fn]
                     (felec/send-comprobante ctx ruc tipo codigo))]

    ;; first assert that the result is correct
    (is (= expected-res actual-res))
    ;; then assert that the correct params are sent to query and ws
    (assert/called-with? query-fn ctx ruc tipo codigo)
    (assert/called-with? update-fn ctx ruc tipo codigo ESTADO_RECIBIDA)
    (assert/called-with? web-service-fn ctx "<factura/>")))
