(ns com.gaumala.utils.time
  (:import java.time.Instant))

(defn get-unix-time [] (-> (Instant/now)
                           (.toEpochMilli)
                           (quot 1000)))
