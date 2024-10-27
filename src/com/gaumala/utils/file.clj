(ns com.gaumala.utils.file)

(defn rm-rf
  "Recursively delete a directory."
  [^java.io.File file]
  ;; when `file` is a directory, list its entries and call this
  ;; function with each entry. can't `recur` here as it's not a tail
  ;; position, sadly. could cause a stack overflow for many entries?
  ;; thanks to @nikolavojicic for the idea to use `run!` instead of
  ;; `doseq` :)
  (when (.isDirectory file)
    (run! rm-rf (.listFiles file)))
  ;; delete the file or directory. if it it's a file, it's easily
  ;; deletable. if it's a directory, we already have deleted all its
  ;; contents with the code above (remember?)
  (.delete file))

(defn file->bytes [input]
  (let [file (if (instance? java.io.File input) input (java.io.File. input))
        stream (java.io.FileInputStream. file)
        result (byte-array (.length file))]
    (.read stream result)
    result))
