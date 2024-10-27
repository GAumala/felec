(ns com.gaumala.felec.db.queries_contribuyente_test
  (:require [com.gaumala.felec.db.helper :refer [ctx
                                                 reset-database]]
            [com.gaumala.felec.db.queries :as q]
            [com.gaumala.felec.mocks :refer [mocked-bytes]]
            [com.gaumala.utils.base64 :as base64]
            [matcher-combinators.test :refer [thrown-match?]]
            [clojure.test :refer [deftest is use-fixtures]]))

(use-fixtures :once reset-database)

(defn encode-keystore-b64 [row]
  (assoc row :keystore (base64/encode (:keystore row))))

(deftest should-find-inserted-contribuyente
  (let [ruc "1792146739001"
        datos {:razonSocial "Distribuidora de Suministros Nacional S.A."
               :estab "002"
               :ptoEmi "001"
               :dirMatriz "Enrique Guerrero Portilla OE1-34 AV. Galo Plaza Lasso"
               :dirEstablecimiento "Sebastián Moreno S/N Francisco García"
               :contribuyenteEspecial "5368"
               :obligadoContabilidad "SI"}
        expected (-> {:ruc "1792146739001"
                      :datos datos
                      :keystore mocked-bytes}
                     (encode-keystore-b64))
        actual (do (q/insert-contribuyente ctx {:keystore mocked-bytes
                                                :datos datos
                                                :ruc ruc})
                   (-> (q/find-contribuyente-by-ruc ctx ruc)
                       (encode-keystore-b64)))]
    (is (= expected actual))))

(deftest inserting-duplicate-contribuyente-should-throw-exception-info
  (let [ruc "1792146739002"
        datos {:razonSocial "Distribuidora de Suministros Nacional S.A."
               :estab "002"
               :ptoEmi "001"
               :dirMatriz "Enrique Guerrero Portilla OE1-34 AV. Galo Plaza Lasso"
               :dirEstablecimiento "Sebastián Moreno S/N Francisco García"
               :contribuyenteEspecial "5368"
               :obligadoContabilidad "SI"}
        contrib {:keystore mocked-bytes
                 :datos datos
                 :ruc ruc}]
    (q/insert-contribuyente ctx contrib)
    (is (thrown-match? {:type :sql-unique-constraint}
                       (q/insert-contribuyente ctx contrib)))))

(deftest should-return-nil-if-contribuyente-not-found
  (let [ruc "1792146739003"
        actual (q/find-contribuyente-by-ruc ctx ruc)]
    (is (nil? actual))))


