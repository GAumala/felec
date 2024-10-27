(ns com.gaumala.felec.server
  (:require [compojure.core :refer [defroutes GET PUT]]
            [org.httpkit.server :refer [run-server]]))
(defn get-env
  ([key fallback]
   (if-let [raw-env (java.lang.System/getenv key)]
     (cond
       (int? fallback) (java.lang.Integer/parseInt raw-env)
       :else raw-env)
     fallback))
  ([key] (java.lang.System/getenv key)))

(defn put-factura [req] "RECHAZADA")
(defn get-factura-status [ruc codigo] "RECHAZADA")

(defroutes app
  (PUT "/factura/:ruc/:codigo" req (put-factura req))
  (GET "/factura/:ruc/:codigo/status" [ruc codigo] (get-factura-status ruc codigo)))

(defn run [opts]
  (let [server-opts {:port (get-env "PORT" 9300)}]
    (run-server app server-opts)))
