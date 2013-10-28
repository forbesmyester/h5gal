(ns h5gal.core
  (:gen-class))

;;;; Gallery Generator built to learn Clojure

;;; Directory strucutre example:
;;;
;;;     <datadir>/2011/04/28/Tokyo Trip/Picture3425.jpg
;;;
;;; The location of photo's, the first three levels of directory outside of the
;;; <datadir> are teh date in reverse order. The next "Tokyo Trip" is the name
;;; of the specific event. These combined should give enough information to
;;; supply a title and a date of a set of pictures, which might be enough for
;;; lots of people.
;;;
;;;     <datadir>/2011/04/28/Tokyo Trip/README.md
;;;
;;; Should you wish to describe the event in detail you can include a README
;;; within the event directory. It should be formatted using Markdown.
;;;
;;;     <datadir>/2011/04/28/Tokyo Trip/Picture3425/README.md
;;;
;;; If you want to add a description of what is in the individual picture
;;; create a directory of the same name, but without the extension and add a
;;; README file there.
;;;
;;;     <datadir>/2011/04/28/Tokyo Trip/Picture3425/_default.jpg
;;;     <datadir>/2011/04/28/Tokyo Trip/Picture3425/_thumb_small.jpg
;;;     <datadir>/2011/04/28/Tokyo Trip/Picture3425/_thumb_medium.jpg
;;;     <datadir>/2011/04/28/Tokyo Trip/Picture3425/_thumb_large.jpg
;;;
;;; NOTE: These files will be created automatically for displaying within the
;;; browser.
;;;
;;;     <datadir>/2011/04/28/Tokyo Trip/Picture3425/override.jpg
;;;
;;; But if it seems a file called override it will show this within the browser
;;; instead as well as use it for thumbnails.

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
