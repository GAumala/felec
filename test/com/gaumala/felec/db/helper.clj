(ns com.gaumala.felec.db.helper
  (:require [com.gaumala.felec.db.h2 :as h2]
            [com.gaumala.utils.file :refer [rm-rf]]
            [next.jdbc :as jdbc]))

(def db-dir "./.test-db")

(defn clear-test-files []
  (rm-rf (java.io.File. db-dir)))

(def ctx {:ds (h2/get-ds db-dir)
          :aes-pwd "mypass1234"})

(defn reset-database [f]
  (clear-test-files)
  (h2/initialize (:ds ctx))
  (f))

(defn del-comprobantes []
  (jdbc/execute-one! (:ds ctx) ["DELETE FROM comprobantes"]))
