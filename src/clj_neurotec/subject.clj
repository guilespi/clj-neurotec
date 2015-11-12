(ns clj-neurotec.subject
  (:import (com.neurotec.biometrics NFinger NSubject NGender NFPosition NTemplate)))


(set! *warn-on-reflection* true)

(def genders {:male NGender/MALE
              :female NGender/FEMALE
              :unknown NGender/UNKNOWN
              :unspecified NGender/UNSPECIFIED})

(def finger-positions {:right-index-finger NFPosition/RIGHT_INDEX_FINGER
                       :right-middle-finger NFPosition/LEFT_MIDDLE_FINGER
                       :right-ring-finger NFPosition/LEFT_RING_FINGER
                       :right-little-finger NFPosition/LEFT_LITTLE_FINGER
                       :right-thumb NFPosition/LEFT_THUMB
                       :left-index-finger NFPosition/LEFT_INDEX_FINGER
                       :left-middle-finger NFPosition/LEFT_MIDDLE_FINGER
                       :left-ring-finger NFPosition/LEFT_RING_FINGER
                       :left-little-finger NFPosition/LEFT_LITTLE_FINGER
                       :left-thumb NFPosition/LEFT_THUMB
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


;;when says: Invalid sample resolution:
;;NImage image = NImageUtils.fromJPEG(fingerJPG);
;;image.setVertResolution(500);
;;image.setHorzResolution(500);
;;finger.setImage(image);
(defn finger-from-file
  "Creates a new finger from an image file"
  [filename & {:keys [position] :as opts}]
  (doto (NFinger.)
    (.setFileName filename)
    (.setPosition ^NFinger (get finger-positions (or position :unknown)))))

(defn finger-from-image
  "Creates a new finger from an NImage object"
  [image & {:keys [position] :as opts}]
  (doto (NFinger.)
    (.setImage image)
    (.setPosition ^NFinger (get finger-positions (or position :unknown)))))

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
