(ns com.gaumala.felec.api-insert-contribuyente-test
  (:require [com.gaumala.felec.db.queries :as queries]
            [com.gaumala.felec :as felec]
            [com.gaumala.felec.examples :refer [example-contrib-datos]]
            [com.gaumala.felec.mocks :refer [mocked-bytes
                                             mocked-ctx]]
            [com.gaumala.sri.xades-bes :as xades-bes]
            [clojure.test :refer [deftest is]]
            [matcher-combinators.test :refer [thrown-match?]]
            [spy.core :refer [calls stub stub-throws]]
            [spy.assert :as assert]))

(def ctx mocked-ctx)
(def mocked-keystore-file "./deps.edn")

(deftest throws-with-type-spec-validation-if-input-keystore-not-a-valid-path
  (let [input {:ruc "1792146739001"
               :datos example-contrib-datos
               :keystore "/some/path/keys.p12"}]
    (is (thrown-match? {:type :spec-validation}
                       (felec/insert-contribuyente ctx input)))))

(deftest throws-with-type-spec-validation-if-input-datos-is-invalid
  (let [input {:ruc "1792146739001"
               :datos {:foo "bar"}
               :keystore mocked-bytes}]
    (is (thrown-match? {:type :spec-validation}
                       (felec/insert-contribuyente ctx input)))))

(deftest throws-with-type-spec-validation-if-keystore-test-fails
  (let [input {:ruc "1792146739001"
               :datos example-contrib-datos
               :keystore mocked-bytes
               :keystore-pwd "mypass123"}]
    (is (thrown-match?
         {:type :xml-signature}
         (felec/insert-contribuyente ctx input)))))

(deftest returns-query-result-if-keystore-is-a-valid-file-path
  (let [input {:ruc "1792146739001"
               :datos example-contrib-datos
               :keystore "./deps.edn"}
        expected {:ID 1}
        insert-fn (stub expected)
        actual (with-redefs [queries/insert-contribuyente insert-fn]
                 (felec/insert-contribuyente ctx input))]
    (is (= expected actual))))

(deftest returns-query-result-if-keystore-is-a-valid-byte-array
  (let [input {:ruc "1792146739001"
               :datos example-contrib-datos
               :keystore mocked-bytes}
        expected {:ID 1}
        insert-fn (stub expected)
        actual (with-redefs [queries/insert-contribuyente insert-fn]
                 (felec/insert-contribuyente ctx input))]
    (is (= expected actual))))

(deftest returns-query-result-if-keystore-passes-keystore-test
  (let [input {:ruc "1792146739001"
               :datos example-contrib-datos
               :keystore mocked-bytes
               :keystore-pwd "mypass123"}
        expected {:ID 1}
        insert-fn (stub expected)
        ;; mock sign-comprobante so that run-keystore-test passes
        actual (with-redefs [queries/insert-contribuyente insert-fn
                             xades-bes/sign-comprobante (stub "signed")]
                 (felec/insert-contribuyente ctx input))]
    (is (= expected actual))))

(deftest calls-db-method-to-insert-with-correct-args
  (let [keystore mocked-bytes
        input {:ruc "1792146739001"
               :datos example-contrib-datos
               :keystore keystore}
        insert-fn (stub {:ID 1})]
    (with-redefs [queries/insert-contribuyente insert-fn]
      (felec/insert-contribuyente ctx input))
    (assert/called-with? insert-fn ctx input)))
