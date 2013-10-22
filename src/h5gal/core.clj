(ns h5gal.core
  (:gen-class))

(use '[clojure.java.io])
(use 'clj-time.core)
(import 'java.util.Locale)
(import '(java.text DateFormat))
(require 'digest)

(defn dataDir "data")

(defn formatLocalDate [localDate]
  (. (. DateFormat getDateInstance (. DateFormat MEDIUM ) (Locale. "en-gb")) format (. localDate toDate) )
  )

(defn isLeafDirectory [path]
  (and
   (.isDirectory path)
   (= 1 (count (filter #(.isDirectory %) (file-seq (file path)))))
   )
  )

(defn walk [dirpath]
  (do (filter isLeafDirectory
              (file-seq (file dirpath))
              ))
  )

(defn extractPathInfo [relativePath fullPath]
  (let [matches (re-matches #"^([0-9]{4})\/([0-9]{2})\/([0-9]{2})\/?(.*)?" relativePath)]
    (if matches
      (let [
            localDate (local-date (Integer. (matches 1)) (Integer. (matches 2)) (Integer. (matches 3)))
            dateTime (date-time (Integer. (matches 1)) (Integer. (matches 2)) (Integer. (matches 3)))
            ]
        (conj {
               :dateInst localDate
               :dateComm (clojure.string/join "/" [(matches 1) (matches 2) (matches 3)])
               :dateDisp (formatLocalDate localDate)
               :id (digest/md5 relativePath)
               }
              (if (> (count (matches 4)) 0)
                {:title (matches 4)  :subtitle (formatLocalDate localDate)}
                {:title (formatLocalDate localDate)}
                )
              )
        )
      nil
      )
    )
  )

(extractPathInfo "2012/11/25/Christmas")
(extractPathInfo "2012/11/25")

(defn relativePaths [path]
  (map #(subs (.getAbsolutePath %) (+ (count (.getAbsolutePath (file path))) 1)) (walk path ))
  )


(remove nil? (map extractPathInfo (relativePaths dataDir)))



(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
