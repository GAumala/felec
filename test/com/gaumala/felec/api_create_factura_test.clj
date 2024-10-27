(ns com.gaumala.felec.api-create-factura-test
  (:require [com.gaumala.felec :as felec]
            [com.gaumala.felec.constants :refer [ESTADO_PENDIENTE
                                                 TIPO_FACTURA]]
            [com.gaumala.felec.db.queries :as queries]
            [com.gaumala.felec.examples :refer [example-contrib-datos
                                                example-factura-content
                                                example-factura-input]]
            [com.gaumala.felec.mocks :refer [mocked-bytes
                                             mocked-ctx]]
            [com.gaumala.utils.time :as time]
            [com.gaumala.sri.xades-bes :as xades-bes]
            [clojure.test :refer [deftest is]]
            [matcher-combinators.test :refer [thrown-match?]]
            [spy.core :refer [calls stub stub-throws]]
            [spy.assert :as assert]))

(def ctx mocked-ctx)
(def factura-args {:ruc "1792146739001"
                   :codigo "00000001"
                   :keystore-pwd "my-pwd-123"})
(def contrib {:ruc "1792146739001"
              :datos example-contrib-datos
              :keystore mocked-bytes})

(deftest throws-with-type-spec-validation-if-input-factura_is_invalid
  (let [args factura-args
        input (assoc example-factura-input
                     :infoTributaria
                     {:secuencial 1})]
    (is (thrown-match? {:type :spec-validation}
                       (felec/create-factura ctx args input)))))

(deftest throws-with-type-bad-input-if-contribuyente-not-found
  (let [args factura-args
        input example-factura-input
        ;; stub find-contribuyente-by-ruc to not find anything
        query-fn (stub nil)]
    ;; first assert that the correct error is thrown
    (is (thrown-match?
         {:type :bad-input}
         (with-redefs [queries/find-contribuyente-by-ruc query-fn]
           (felec/create-factura ctx args input))))
    ;; then assert that the correct params are sent to the query
    (assert/called-with? query-fn ctx (:ruc args))))

(deftest throws-with-type-secuencial-missing-if-secuencial-is-not-on-input-or-db
  (let [args factura-args
        ;; input factura without secuencial
        input (assoc example-factura-input :infoTributaria {})
        ;; stub find-contribuyente-by-ruc to return valid contribuyente
        contrib-query-fn (stub contrib)
        ;; stub find-max-secuencial-by-ruc to not find anything
        secu-query-fn (stub nil)]
    ;; first assert that the correct error is thrown
    (is (thrown-match?
         {:type :secuencial-missing}
         (with-redefs [queries/find-max-secuencial-by-ruc secu-query-fn
                       queries/find-contribuyente-by-ruc contrib-query-fn]
           (felec/create-factura ctx args input))))
    ;; then assert that the correct params are sent to the queries
    (assert/called-with? contrib-query-fn ctx (:ruc args))
    (assert/called-with? secu-query-fn ctx TIPO_FACTURA (:ruc args))))

(deftest throws-with-type-xml-signature-if-failed-to-sign-factura
  (let [args factura-args
        ;; input factura with secuencial
        input example-factura-input
        ;; stub find-contribuyente-by-ruc to return valid contribuyente
        contrib-query-fn (stub contrib)]
    ;; first assert that the correct error is thrown
    (is (thrown-match?
         {:type :xml-signature}
         (with-redefs [queries/find-contribuyente-by-ruc contrib-query-fn]
           (felec/create-factura ctx args input))))
    ;; then assert that the correct params are sent to the queries
    (assert/called-with? contrib-query-fn ctx (:ruc args))))

(deftest returns-new-factura-data-if-succeeds-with-secuencial-in-input
  (let [args factura-args
        ;; input factura with secuencial
        input example-factura-input
        ;; stub find-contribuyente-by-ruc to return valid contribuyente
        contrib-query-fn (stub contrib)
        ;; stub sign-comprobante to succeed
        sign-fn (stub "<factura/>")
        ;; stub get-unix-time to return constant
        time-fn (stub 1000)
        ;; stub insert-comprobante to succeed
        insert-query-fn (stub nil)
        expected-insert-input {:ruc (:ruc args)
                               :codigo "00000001"
                               :tipo TIPO_FACTURA
                               :estado ESTADO_PENDIENTE
                               :secuencial 1
                               :last_update 1000
                               :xml "<factura/>"
                               :content example-factura-content}
        expected {:ruc (:ruc args)
                  :codigo "00000001"
                  :tipo TIPO_FACTURA
                  :estado ESTADO_PENDIENTE
                  :secuencial 1
                  :last_update 1000}
        actual (with-redefs [queries/find-contribuyente-by-ruc contrib-query-fn
                             queries/insert-comprobante insert-query-fn
                             xades-bes/sign-comprobante sign-fn
                             time/get-unix-time time-fn]
                 (felec/create-factura ctx args input))]
    ;; first assert that the correct value is returned
    (is (= expected actual))
    ;; then assert that the correct params are sent to the queries
    (assert/called-with? contrib-query-fn ctx (:ruc args))
    (assert/called-with? insert-query-fn ctx expected-insert-input)))

(deftest returns-new-factura-data-if-succeeds-with-secuencial-in-db
  (let [args factura-args
        ;; input factura without secuencial
        input (assoc example-factura-input :infoTributaria {})
        ;; stub find-contribuyente-by-ruc to return valid contribuyente
        contrib-query-fn (stub contrib)
        ;; stub find-max-secuencial-by-ruc to return 1
        secu-query-fn (stub 1)
        ;; stub sign-comprobante to succeed
        sign-fn (stub "<factura/>")
        ;; stub get-unix-time to return constant
        time-fn (stub 1000)
        ;; stub insert-comprobante to succeed
        insert-query-fn (stub nil)
        ;; because the secuencial is different, we have to change 
        ;; a few fields in the expected content
        expected-content
        (-> example-factura-content
            (assoc-in [:infoTributaria :secuencial]
                      "000000002")
            (assoc-in [:infoTributaria :claveAcceso]
                      "2410202401179214673900110020010000000020000000119"))
        expected-insert-input {:ruc (:ruc args)
                               :codigo "00000001"
                               :tipo TIPO_FACTURA
                               :estado ESTADO_PENDIENTE
                               :secuencial 2
                               :last_update 1000
                               :xml "<factura/>"
                               :content expected-content}
        expected {:ruc (:ruc args)
                  :codigo "00000001"
                  :tipo TIPO_FACTURA
                  :estado ESTADO_PENDIENTE
                  :secuencial 2
                  :last_update 1000}
        actual (with-redefs [queries/find-contribuyente-by-ruc contrib-query-fn
                             queries/find-max-secuencial-by-ruc secu-query-fn
                             queries/insert-comprobante insert-query-fn
                             xades-bes/sign-comprobante sign-fn
                             time/get-unix-time time-fn]
                 (felec/create-factura ctx args input))]
    ;; first assert that the correct value is returned
    (is (= expected actual))
    ;; then assert that the correct params are sent to the queries
    (assert/called-with? contrib-query-fn ctx (:ruc args))
    (assert/called-with? secu-query-fn ctx TIPO_FACTURA (:ruc args))
    (assert/called-with? insert-query-fn ctx expected-insert-input)))
