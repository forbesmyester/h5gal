(ns h5gal.core
  (:gen-class)
  (:import  (javax.imageio ImageIO))
  (:require [clojure.tools.cli :as cli])
  (:require [clojure.java.io :refer [file as-file]])
  (:require [clj-time.core :refer [date-time local-date]])
  (:require [mikera.image.core :refer [scale-image]])
  (:import (java.text.DateFormat))
  (:require [clojure.data.json :as json])
  (:import (java.util.Locale))
  (:require digest)
  )


;  (:use [clojure.contrib.command-line])
;
;  (:use 'clj-time.core)
;
;  (:import '(java.text DateFormat))
;
;  (:use '[clojure.set])
;  (:use 'mikera.image.core)
;  (:use 'mikera.image.colours)
;  (:import [java.awt.image BufferedImage BufferedImageOp])
;  (:import [java.io File])
;
;  (:use '[clojure.java.io :only [as-file]])

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

(defn loadImg [filename]
  (javax.imageio.ImageIO/read (file filename))
  )

(def imgSizes {
               :thumb {
                        :small [64 48]
                        :medium [128 96]
                        :large [320 240]
                        }
               :display {
                         :small [640 480]
                         :medium [1280 720]
                         :large [1920 1200]
                         }
               }
  )


(defn formatLocalDate [localDate]
  (. (. java.text.DateFormat getDateInstance (. java.text.DateFormat MEDIUM ) (java.util.Locale. "en-gb")) format (. localDate toDate) )
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
  (str (digest/md5 (str fullDirPath "/" filename "/" modifiedTimestamp "/" thumbOrDisplay "/" size)) ".jpg")
)

(defn createFileRecord [baseDir leafDir filename]
  (let [
        fileReadme (clojure.string/replace filename #"(edited\.)?([^\.]+)$" "md")
        modifiedTimestamp (.lastModified (as-file (str baseDir "/" leafDir "/" filename)))
        ]
    (conj {
           :filename filename
           :modifiedTimestamp modifiedTimestamp
           }
          (if (.exists (as-file (str baseDir "/" leafDir "/" fileReadme)))
            { :readme fileReadme}
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
             ;:dateInst localDate
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



(defn getAlbumsData [picturePath]
  (remove nil? (map
              (partial extractPathInfo picturePath)
              (map (partial getRelativePath (file picturePath)) (walk picturePath)))
        )
  )

(defn reconstructFilename [albumPath filename]
  (str albumPath filename)
  )

(defn *generateThumbnailFile* [tmpDir dataDir thumbOrDisplay size fullDirPath filename modifiedTimestamp]
  (let [
        extractDimension (fn [ind] (get ((keyword size) ((keyword thumbOrDisplay) imgSizes)) ind))
        width (extractDimension 0)
        height (extractDimension 1)
        thumbFilename (generateThumbnailFilename thumbOrDisplay size fullDirPath filename modifiedTimestamp )
        originalFilename (str dataDir "/" fullDirPath "/" filename)
        *createTmpDir*         (fn [dirname]
                                 (let [f (java.io.File. dirname)]
                                   (if
                                     (not (.exists f))
                                     (.mkdir f)
                                     )
                                   )
                                 )
        *writeFile* (fn [filename bufferedImage]
                      (javax.imageio.ImageIO/write bufferedImage "jpg" (as-file filename))
                      )
        ]
    (*createTmpDir* tmpDir)
    (*writeFile* (str tmpDir "/" thumbFilename) (scale-image (loadImg originalFilename) width height))
    thumbFilename
    )
  )

(defn *prepareAlbumForDisplay* [tmpDir dataDir albumData]

  (update-in
   albumData
   [:files]
   (fn [files]
     (map
      (fn [fileRecord]
        (let [
              tmpFiles {
                        :thumbs {
                                 :small (*generateThumbnailFile* tmpDir dataDir "thumb" "small" (:albumPath albumData) (:filename fileRecord) (:modifiedTimestamp fileRecord))
                                 :medium (*generateThumbnailFile*  tmpDir dataDir "thumb" "medium" (:albumPath albumData) (:filename fileRecord) (:modifiedTimestamp fileRecord))
                                 :large (*generateThumbnailFile*  tmpDir dataDir "thumb" "large" (:albumPath albumData) (:filename fileRecord) (:modifiedTimestamp fileRecord))
                                 }
                        :display {
                                  :small (*generateThumbnailFile*  tmpDir dataDir "display" "small" (:albumPath albumData) (:filename fileRecord) (:modifiedTimestamp fileRecord))
                                  :medium (*generateThumbnailFile*  tmpDir dataDir "display" "medium" (:albumPath albumData) (:filename fileRecord) (:modifiedTimestamp fileRecord))
                                  :large (*generateThumbnailFile*  tmpDir dataDir "display" "large" (:albumPath albumData) (:filename fileRecord) (:modifiedTimestamp fileRecord))
                                  }
                        }
              ]

          (conj fileRecord
                {:tmpFiles tmpFiles}
                )
          )
        )
      files
      )
     )
   )

  )


(let [
      dataDir "data"
      tmpDir "/tmp/h5gal"
      ]

  (map (partial *prepareAlbumForDisplay* tmpDir dataDir) (getAlbumsData dataDir))

  (*prepareAlbumForDisplay* tmpDir dataDir (first (getAlbumsData dataDir)))

  (parseDirectory "data" "2011/05/09/Going Fishing")

  (map (partial getRelativePath (file "data")) (walk dataDir))

  (extractPathInfo "data" "2011/05/09/Going Fishing")

  (walk dataDir)

  (generateThumbnailFilename "display" "small" "a/b" "bob.jpg" 34234)

  )



(defn -main [& args]
  (let [[options args banner]
        (cli/cli args
                 ["--albumdir" "The location of your albums"]
                 ["--tmpdir" "The location to generate thumbnails" :default "/tmp/h5gal"])]
    (println "albumdir:" (:albumdir options))
    (println "tmpdir:" (:tmpdir options))
    ;(println (map (partial *prepareAlbumForDisplay* (:tmpdir options) (:albumdir options)) (getAlbumsData (:albumdir options))))
    (println (json/write-str (map (partial *prepareAlbumForDisplay* (:tmpdir options) (:albumdir options)) (getAlbumsData (:albumdir options)))))
    )
  )
