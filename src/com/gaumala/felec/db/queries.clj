(ns com.gaumala.felec.db.queries
  (:import javax.sql.rowset.serial.SerialClob)
  (:require [clojure.set :refer [rename-keys]]
            [next.jdbc.sql :as sql]
            [next.jdbc.result-set :as result-set]
            [com.gaumala.utils.crypto :as crypto]))

(defn encrypt-to-clob [plain aes-pwd]
  (if (nil? plain)
    nil
    (-> plain
        (crypto/encrypt aes-pwd)
        (char-array)
        (SerialClob.))))

(def clob-builder (result-set/as-maps-adapter
                   result-set/as-maps
                   result-set/clob-column-reader))

(defn insert-contribuyente [ctx {:keys [keystore datos ruc]}]
  (let [{:keys [ds aes-pwd]} ctx
        contrib {:ruc ruc
                 :datos (-> datos
                            (select-keys [:razonSocial
                                          :ultimo-secuencial
                                          :estab
                                          :ptoEmi
                                          :dirMatriz
                                          :nombreComercial
                                          :dirEstablecimiento
                                          :contribuyenteEspecial
                                          :obligadoContabilidad
                                          :moneda])
                            (pr-str)
                            (encrypt-to-clob aes-pwd))
                 :keystore (encrypt-to-clob keystore aes-pwd)}]
    (try
      (sql/insert! ds :contribuyentes contrib)
      (catch org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException e
        (let [template (str "Unique constraint violation inserting "
                            "contribuyente. RUC: %s")
              msg (format template ruc)]
          (throw (ex-info msg
                          {:type :sql-unique-constraint
                           :row contrib}
                          e)))))))

(defn find-contribuyente-by-ruc [{:keys [ds aes-pwd]} ruc]
  (if-let [row (first (sql/find-by-keys ds
                                        :contribuyentes
                                        {:ruc ruc}
                                        {:builder-fn clob-builder}))]
    {:ruc (:CONTRIBUYENTES/RUC row)
     :keystore (-> (:CONTRIBUYENTES/KEYSTORE row)
                   (crypto/decrypt aes-pwd))
     :datos (-> (:CONTRIBUYENTES/DATOS row)
                (crypto/decrypt aes-pwd)
                (String.)
                (clojure.edn/read-string))}))

(defn del-contribuyente-by-ruc [ctx ruc]
  (sql/delete! (:ds ctx) :contribuyentes {:ruc ruc}))

(defn insert-comprobante [ctx {:keys [ruc
                                      codigo
                                      secuencial
                                      estado
                                      tipo
                                      last_update
                                      content
                                      xml]}]
  (let [{:keys [ds aes-pwd]} ctx
        row {:ruc ruc
             :codigo codigo
             :secuencial secuencial
             :estado estado
             :tipo tipo
             :last_update last_update
             :edn (encrypt-to-clob (pr-str content) aes-pwd)
             :xml (encrypt-to-clob xml aes-pwd)}]
    (try
      (sql/insert! ds :comprobantes row)
      (catch org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException e
        (let [template (str "Unique constraint violation inserting comprobante"
                            " RUC: %s codigo: %s secuencial: %s")
              msg (format template ruc codigo secuencial)]
          (throw (ex-info msg
                          {:type :sql-unique-constraint
                           :row row}
                          e)))))))

(defn find-comprobante-xml-by-codigo [{:keys [ds aes-pwd]} ruc tipo codigo]
  (if-let [row (first (sql/find-by-keys ds
                                        :comprobantes
                                        {:ruc ruc :codigo codigo}
                                        {:columns [:xml]
                                         :builder-fn clob-builder}))]
    {:ruc ruc
     :tipo tipo
     :codigo codigo
     :xml (-> (:COMPROBANTES/XML row)
              (crypto/decrypt aes-pwd)
              (String.))}))

(defn find-comprobante-data-by-codigo [{:keys [ds aes-pwd]} ruc tipo codigo]
  (if-let [row (first (sql/find-by-keys ds
                                        :comprobantes
                                        {:ruc ruc :codigo codigo}
                                        {:columns [:edn]
                                         :builder-fn clob-builder}))]
    {:ruc ruc
     :tipo tipo
     :codigo codigo
     :content (-> (:COMPROBANTES/EDN row)
                  (crypto/decrypt aes-pwd)
                  (String.)
                  (clojure.edn/read-string))}))

(defn find-comprobante-by-codigo [{:keys [ds aes-pwd]} ruc tipo codigo]
  (if-let [row (first (sql/find-by-keys ds
                                        :comprobantes
                                        {:ruc ruc :codigo codigo}
                                        {:builder-fn clob-builder}))]
    {:ruc ruc
     :tipo tipo
     :codigo codigo
     :estado (:COMPROBANTES/ESTADO row)
     :last_update (:COMPROBANTES/LAST_UPDATE row)
     :secuencial (:COMPROBANTES/SECUENCIAL row)
     :xml (-> (:COMPROBANTES/XML row)
              (crypto/decrypt aes-pwd)
              (String.))
     :content (-> (:COMPROBANTES/EDN row)
                  (crypto/decrypt aes-pwd)
                  (String.)
                  (clojure.edn/read-string))}))

(defn del-comprobante-by-codigo [{:keys [ds]} ruc tipo codigo]
  (sql/delete! ds :comprobantes {:ruc ruc :tipo tipo :codigo codigo}))

(defn find-comprobantes-with-estado [ctx tipo estado]
  (let [rows (sql/find-by-keys (:ds ctx)
                               :comprobantes
                               {:tipo tipo :estado estado}
                               {:columns [:ruc :tipo :codigo]})]
    (map #(rename-keys % {:estado estado
                          :COMPROBANTES/RUC :ruc
                          :COMPROBANTES/TIPO :tipo
                          :COMPROBANTES/CODIGO :codigo}) rows)))

(defn find-max-secuencial-by-ruc [ctx ruc tipo]
  (sql/aggregate-by-keys (:ds ctx)
                         :comprobantes
                         "max(secuencial)"
                         {:ruc ruc :tipo tipo}))

(defn update-comprobante-estado [ctx ruc tipo codigo estado]
  (sql/update! (:ds ctx) :comprobantes {:estado estado} {:ruc ruc
                                                         :tipo tipo
                                                         :codigo codigo}))
