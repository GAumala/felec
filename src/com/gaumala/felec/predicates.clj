(ns com.gaumala.felec.predicates)

(defn real-path? [x]
  (and (string? x)
       (.exists (java.io.File. x))))
