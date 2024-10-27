(ns com.gaumala.utils.crypto-test
  (:require [com.gaumala.utils.crypto :as crypto]
            [clojure.test :refer [deftest is]]))

(def password "pass1234")
(deftest should-decrypt-encrypted-text
  (let [plain "This is a plain text string."
        encrypted (crypto/encrypt plain password)
        decrypted (-> encrypted
                      (crypto/decrypt password)
                      String.)]
    (is (= plain decrypted))))

(deftest should-decrypt-encrypted-empty-string
  (let [plain ""
        encrypted (crypto/encrypt plain password)
        decrypted (-> encrypted
                      (crypto/decrypt password)
                      String.)]
    (is (= plain decrypted))))

(deftest should-be-able-to-handle-nil-as-plain-text-input
  (let [plain nil
        encrypted (crypto/encrypt plain password)
        decrypted (crypto/decrypt encrypted password)]
    (is (= nil encrypted))
    (is (= plain decrypted))))
