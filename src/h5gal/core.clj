(ns h5gal.core
  (:gen-class))

;;;; # Gallery Generator built to learn Clojure
;;;;
;;;; ## Where To Put Your Images
;;;;
;;;; Pictures should be stored beneath a git-annex routeconforming to the
;;;; following directory structure.
;;;;
;;;;     <datadir>/2011/04/28/Tokyo Trip/Picture3425.jpg
;;;;
;;;; The first three levels of directory outside of the <datadir> are the date
;;;; in reverse order. The next "Tokyo Trip" is the name of the specific event.
;;;; These combined should give enough information to supply a title and a date
;;;; of a set of pictures, which might be enough for lots of people.
;;;;
;;;; ## Describing What The Set Of Pictures Is About
;;;;
;;;; Should you wish to describe what the set if pictures is about in detail you
;;;; can include a README within the event directory. It should be formatted
;;;; using Markdown.
;;;;
;;;;     <datadir>/2011/04/28/Tokyo Trip/README.md
;;;;
;;;; ## Describing A Specific Picture
;;;;
;;;; If you want to add a description of what is in the individual picture
;;;; create a markdown document with the same name but with a md (markdown)
;;;; extension
;;;;
;;;;     <datadir>/2011/04/28/Tokyo Trip/Picture3425.md
;;;;
;;;; ## Photo Editing
;;;;
;;;; It may be that you want to keep an originals of all images for posterity
;;;; but also want to touch up and experiment with photo editing. To do this
;;;; append a .edited to the end of the name component of the filename as shown
;;;; below.
;;;;
;;;;     <datadir>/2011/04/28/Tokyo Trip/Picture3425/Picture3425.edited.jpg
;;;;


(use '[clojure.java.io])
(use 'clj-time.core)
(import 'java.util.Locale)
(import '(java.text DateFormat))
(require 'digest)

(def dataDir "data")

(defn formatLocalDate [localDate]
  (. (. DateFormat getDateInstance (. DateFormat MEDIUM ) (Locale. "en-gb")) format (. localDate toDate) )
  )

(def imageConversions [
                   { :extensions ["jpeg", "jpg"] :conversion nil }
                   { :extensions ["bmp"] :conversion nil }
                   ])

(defn imageFormats []
  (flatten (map (fn [{ext :extensions}] ext) imageConversions))
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

(defn walked [baseDir leafDir]
  {:b baseDir :l leafDir}
  )

(defn extractPathInfo [baseDir leafDir]
  (let
    [ relativePath (subs (.getAbsolutePath leafDir) (+ (count (.getAbsolutePath baseDir)) 1))
      matches (re-matches #"^([0-9]{4})\/([0-9]{2})\/([0-9]{2})\/?(.*)?" relativePath)
      localDate (if matches (local-date (Integer. (matches 1)) (Integer. (matches 2)) (Integer. (matches 3))) nil)
      dateTime (if matches (date-time (Integer. (matches 1)) (Integer. (matches 2)) (Integer. (matches 3))))
      ]
    (if matches
      (conj {
             :dateInst localDate
             :dateComm (clojure.string/join "-" [(matches 1) (matches 2) (matches 3)])
             :dateDisp (formatLocalDate localDate)
             :id (digest/md5 relativePath)
             }
            (if (> (count (matches 4)) 0)
              {:title (matches 4)  :subtitle (formatLocalDate localDate)}
              {:title (formatLocalDate localDate)}
              )
            )
      nil
      )
    )
  )

(extractPathInfo (file "data") (file "data/2012/11/25/Christmas"))
(extractPathInfo (file "data") (file "data/2012/11/25"))

(remove nil? (map (partial extractPathInfo (file dataDir)) (walk dataDir)))



(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
