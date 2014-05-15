(ns clj-neurotec.subject
  (:import (com.neurotec.biometrics NFinger NSubject NGender NFPosition)))


(def genders {:male NGender/MALE
              :female NGender/FEMALE
              :unknown NGender/UNKNOWN
              :unspecified NGender/UNSPECIFIED})

(def finger-positions {:right-index-finger NFPosition/RIGHT_INDEX_FINGER
                       :unknown NFPosition/UNKNOWN})

(defn make-subject
  "Creates a new subject with the given identifier, gender and finger information.
   fingers is expected to be a NFinger list and missing-fingers a NFPosition list"
  [{:keys [id fingers gender missing-fingers]}]
  (let [subject (NSubject.)
        sbj-fingers (.getFingers subject)
        sbj-miss-fingers (.getMissingFingers subject)]
    (doseq [f fingers]
      (.add sbj-fingers f))
    (doseq [f missing-fingers]
      (.add sbj-miss-fingers f))
    (doto subject
      (.setGender (get genders (or gender :unspecified)))
      (.setId (or id (str (java.util.UUID/randomUUID)))))

    ))


(defn finger-from-file
  "Creates a new finger from an image file"
  [filename & {:keys [position] :as opts}]
  (doto (NFinger.)
    (.setFileName filename)
    (.setPosition (get finger-positions (or position :unknown)))))

(defn finger-from-image
  "Creates a new finger from an NImage object"
  [image & {:keys [position] :as opts}]
  (doto (NFinger.)
    (.setImage image)
    (.setPosition (get finger-positions (or position :unknown)))))

(defn finger-position
  [finger]
  (.getPosition finger))

(defn finger-image
  [finger]
  (.getImage finger))

  (comment
    (finger-from-file "/Users/guilespi/Documents/Development/interrupted/biometrics/Neurotech/Bin/watson/index.bmp"
                      :position :right-index-finger)

    (for [i (range 10)]
      (let [finger (finger-from-file "/Users/guilespi/Documents/Development/interrupted/biometrics/Neurotech/Bin/watson/index.bmp"
                                     :position :right-index-finger)]
        (make-subject {:id (str i) :gender :male :fingers [finger]})))

    (clj-neurotec.client/enroll cli subj2)
)
