(ns h5gal.core
  (:gen-class))

(use '[clojure.java.io])
(use 'clj-time.core)

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

(defn extractPathInfo [relativePath]
  (let [matches (re-matches #"^([0-9]{4})\/([0-9]{2})\/([0-9]{2})\/?(.*)?" relativePath)]
    (if matches
      (if (> (count (matches 4)) 0)
        {:short-description (matches 4) :date (date-time (Integer. (matches 1)) (Integer. (matches 2)) (Integer. (matches 3)))}
        {:date (date-time (Integer. (matches 1)) (Integer. (matches 2)) (Integer. (matches 3)))}
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

(relativePaths "data")



(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
