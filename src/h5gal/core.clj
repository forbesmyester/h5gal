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
;;;;     <datadir>/2011/04/28/Tokyo Trip/Picture3425.edited.jpg
;;;;


(use '[clojure.java.io])
(use 'clj-time.core)
(import 'java.util.Locale)
(import '(java.text DateFormat))
(require 'digest)
(use '[clojure.set])

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

(defn nonEditedVersions [filenames]
  (let [
        orImgFormats (clojure.string/join "|" (into [] (imageFormats)))
        reStr (str "(.*\\.)(edited\\.)(" orImgFormats ")$")
        sequ (keep #(re-matches (re-pattern reStr) %1) filenames)
        ]
    (map #(str (nth % 1) (nth % 3)) sequ)
    )
 )

(defn removeNonEditedVersions [filenames]
  (let [
        edited (vec (nonEditedVersions filenames))
        ]
    (filter #(= -1 (.indexOf edited %1)) filenames)
    )
  )

(defn isImage [filename]
  #(some (re-find #"[^\.]+$" %) (imageFormats))
  )

(defn generateThumbnailFilename [thumbOrDisplay size fullDirPath filename modifiedTimestamp]
  (str fullDirPath "/" filename "/" modifiedTimestamp "/" thumbOrDisplay "/" size ".jpg")
)

(defn createFileRecord [baseDir leafDir filename]
  (let [
        fileReadme (clojure.string/replace filename #"(edited\.)?([^\.]+)$" "md")
        modifiedTimestamp (.lastModified (as-file (str baseDir "/" leafDir "/" filename)))
        ]
    (conj {
           :filename filename
           :scaledVersions {
                            :thumbs {
                                     :small (generateThumbnailFilename "thumb" "small" leafDir filename modifiedTimestamp)
                                     :medium (generateThumbnailFilename "thumb" "medium" leafDir filename modifiedTimestamp)
                                     :large (generateThumbnailFilename "thumb" "large" leafDir filename modifiedTimestamp)
                                     }
                            :display {
                                     :small (generateThumbnailFilename "display" "small" leafDir filename modifiedTimestamp)
                                     :medium (generateThumbnailFilename "display" "medium" leafDir filename modifiedTimestamp)
                                     :large (generateThumbnailFilename "display" "large" leafDir filename modifiedTimestamp)
                                      }
                            }
           }
          (if (.exists (as-file (str baseDir "/" leafDir "/" filename)))
            { :readme fileReadme }
            )
          )
    )
  )

(defn parseDirectory [baseDir leafDir]
  (let [
        inDirReadmeFilename "README.md"
        readmeFilename (str baseDir "/" leafDir "/" inDirReadmeFilename)
        isImage (fn isImage [img] (> (count (filter #(= (re-find #"[^\.]+$" img) %) (imageFormats))) 0))
        filesWithin (removeNonEditedVersions (filter isImage (map #(.getName %) (file-seq (as-file (str baseDir "/" leafDir))))))
        ]
    (conj
     {
      :files (map
              (partial createFileRecord baseDir leafDir)
              filesWithin
              )
      }
     (if (.exists (as-file readmeFilename))
       {:readme inDirReadmeFilename}
       )
     )
    )
  )

(parseDirectory "data" "2011/05/09/Going Fishing")

(defn getRelativePath [baseDir leafDir]
  (subs (.getAbsolutePath leafDir) (+ (count (.getAbsolutePath baseDir)) 1))
  )

(defn extractPathInfo [baseDir leafDir]
  (let
    [ matches (re-matches #"^([0-9]{4})\/([0-9]{2})\/([0-9]{2})\/?(.*)?" leafDir)
      localDate (if matches (local-date (Integer. (matches 1)) (Integer. (matches 2)) (Integer. (matches 3))) nil)
      dateTime (if matches (date-time (Integer. (matches 1)) (Integer. (matches 2)) (Integer. (matches 3))) nil)
      ]
    (if matches
      (conj {
             :albumPath leafDir
             :dateInst localDate
             :dateComm (clojure.string/join "-" [(matches 1) (matches 2) (matches 3)])
             :dateDisp (formatLocalDate localDate)
             :id (digest/md5 leafDir)
             }
            (parseDirectory baseDir leafDir)
            (if (> (count (matches 4)) 0)
              {:title (matches 4)  :subtitle (formatLocalDate localDate)}
              {:title (formatLocalDate localDate)}
              )
            )
      nil
      )
    )
  )

(extractPathInfo "data" "2011/05/09/Going Fishing")

(walk dataDir)

(map (partial getRelativePath (file dataDir)) (walk dataDir))

(remove nil? (map
              (partial extractPathInfo dataDir)
              (map (partial getRelativePath (file dataDir)) (walk dataDir)))
        )

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
