(ns com.gaumala.utils.crypto
  "encrypt/decrypt with AES algorithm (128 bits) using a passphrase to 
  derive key. copied from: https://stackoverflow.com/a/14822871"
  (:require [com.gaumala.utils.base64 :as base64])
  (:import (javax.crypto Cipher KeyGenerator SecretKey)
           javax.crypto.spec.SecretKeySpec
           java.security.SecureRandom))

(defn get-raw-key [^java.lang.String passphrase]
  (let [keygen (KeyGenerator/getInstance "AES")
        sr (SecureRandom/getInstance "SHA1PRNG")]
    (.setSeed sr (.getBytes passphrase))
    (.init keygen 128 sr)
    (.. keygen generateKey getEncoded)))

(defn get-cipher [mode pass]
  (let [key-spec (SecretKeySpec. (get-raw-key pass) "AES")
        cipher (Cipher/getInstance "AES")]
    (.init cipher mode key-spec)
    cipher))

(defmulti get-bytes class)
(defmethod get-bytes String [input] (.getBytes input))
(defmethod get-bytes (Class/forName "[B") [input] input)
(defmethod get-bytes :default [input] nil)

(defn encrypt [plain pass]
  (let [cipher (get-cipher Cipher/ENCRYPT_MODE pass)]
    (some->> (get-bytes plain)
             (.doFinal cipher)
             base64/encode)))

(defn decrypt [encrypted pass]
  (let [cipher (get-cipher Cipher/DECRYPT_MODE pass)]
    (some->> encrypted
             base64/decode
             (.doFinal cipher))))

(defn decrypt-to-string [encrypted pass] (String. (decrypt encrypted pass)))
