(ns com.gaumala.felec.db.h2
  "Funciones para conectarse a una base de datos local H2"
  {:doc/format :markdown}
  (:require [clojure.java.io :refer [resource]]
            [next.jdbc :as jdbc]))

(defn get-ds
  "Obtiene un `DataSource` a una base de datos H2 dentro del directorio `dir`."
  {:doc/format :markdown}
  [dir]
  (jdbc/get-datasource
   {:jdbcUrl (str "jdbc:h2:" dir "/h2.db")}))

(defn initialize
  "Inicializa la base datos creando las tablas e índices usando el
  `DataSource` `ds`."
  {:doc/format :markdown}
  [ds]
  (let [statements (slurp (resource "felec.sql"))]
    (jdbc/execute! ds [statements])))
