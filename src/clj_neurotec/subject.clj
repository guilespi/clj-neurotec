(ns clj-neurotec.subject
  (:import (com.neurotec.biometrics NFinger NSubject NGender NFPosition NTemplate)
           (com.neurotec.io NBuffer)
           (com.neurotec.images NImage NImageFormat)))


(set! *warn-on-reflection* true)

(def genders {:male NGender/MALE
              :female NGender/FEMALE
              :unknown NGender/UNKNOWN
              :unspecified NGender/UNSPECIFIED})

(def finger-positions {:right-index-finger NFPosition/RIGHT_INDEX_FINGER
                       :right-middle-finger NFPosition/RIGHT_MIDDLE_FINGER
                       :right-ring-finger NFPosition/RIGHT_RING_FINGER
                       :right-little-finger NFPosition/RIGHT_LITTLE_FINGER
                       :right-thumb-finger NFPosition/RIGHT_THUMB
                       :left-index-finger NFPosition/LEFT_INDEX_FINGER
                       :left-middle-finger NFPosition/LEFT_MIDDLE_FINGER
                       :left-ring-finger NFPosition/LEFT_RING_FINGER
                       :left-little-finger NFPosition/LEFT_LITTLE_FINGER
                       :left-thumb-finger NFPosition/LEFT_THUMB
                       :unknown NFPosition/UNKNOWN})

(defn make-subject
  "Creates a new subject with the given identifier, gender and finger information.
   fingers is expected to be a NFinger list and missing-fingers a NFPosition list"
  [{:keys [id fingers gender missing-fingers template]}]
  (let [subject (NSubject.)
        sbj-fingers (.getFingers subject)
        sbj-miss-fingers (.getMissingFingers subject)]
    (doseq [f fingers]
      (.add sbj-fingers f))
    (doseq [f missing-fingers]
      (.add sbj-miss-fingers f))
    (when template
      (.setTemplate ^NSubject subject ^NTemplate template))
    (doto subject
      (.setGender (get genders (or gender :unspecified)))
      (.setId (or id (str (java.util.UUID/randomUUID)))))))

(defn finger-from-image
  "Creates a new finger from an NImage object"
  [image {:keys [position] :as opts}]
  (doto (NFinger.)
    (.setImage image)
    (.setPosition ^NFinger (get finger-positions (or position :unknown)))))


(defn finger-from-file
  "Creates a new finger from an image file
   If says: Invalid sample resolution, call
   finger-from-sized-image function and pass resolution parameters"
  [filename & {:keys [position] :as opts}]
  (doto (NFinger.)
    (.setFileName filename)
    (.setPosition ^NFinger (get finger-positions (or position :unknown)))))

(defn slurp-bytes
  "Slurp the bytes from a slurpable thing"
  [x]
  (with-open [out (java.io.ByteArrayOutputStream.)]
    (clojure.java.io/copy (clojure.java.io/input-stream x) out)
    (.toByteArray out)))


(defn finger-from-sized-image
  "Creates a new finger from an image file with resolution parameters"
  [filename & {:keys [position height width] :as opts}]
  (let [pixels (NBuffer/fromArray (slurp-bytes (clojure.java.io/file filename)))
        image (NImage/fromMemory pixels (NImageFormat/getPNG))]
    (.setVertResolution image height)
    (.setHorzResolution image width)
    (finger-from-image image opts)))

(defn finger-from-stream
  [])

(defn finger-position
  [finger]
  (.getPosition ^NFinger finger))

(defn finger-image
  [finger]
  (.getImage ^NFinger finger))

(defn template
  [subject]
  (.getTemplate subject))

  (comment
    (finger-from-file "/Users/guilespi/Documents/Development/interrupted/biometrics/Neurotech/Bin/watson/index.bmp"
                      :position :right-index-finger)

    (for [i (range 10)]
      (let [finger (finger-from-file "/Users/guilespi/Documents/Development/interrupted/biometrics/Neurotech/Bin/watson/index.bmp"
                                     :position :right-index-finger)]
        (make-subject {:id (str i) :gender :male :fingers [finger]})))

    (clj-neurotec.client/enroll cli subj2)
)
