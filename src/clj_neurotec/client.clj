(ns clj-neurotec.client
  (:require [clojure.walk])
  (:import (com.neurotec.biometrics.client NBiometricClient)
           (com.neurotec.biometrics NMatchingSpeed NTemplateSize NBiometricStatus)
           (com.neurotec.licensing NLicense)))


(def ^:dynamic *default-components* "Biometrics.FingerExtraction,Biometrics.FingerMatching")

(defn obtain-license
  [components]
  (NLicense/obtainComponents "192.168.0.190" 5000 components))

(defn release-license
  [components]
  (NLicense/releaseComponents components))


;;threshold stuff translates a user specified threshold in % to
;;the internal magic numbers required by the SDK
(defonce thresholds (into (sorted-map) {0.000001 96
                                        0.00001 84
                                        0.0001 72
                                        0.001 60
                                        0.01 48
                                        0.1 36
                                        1 24
                                        10 12
                                        100 0}))

(defn abs [x] (if (neg? x) (- x) x))
(defn closest-threshold [k]
  (get thresholds
       (if-let [a (key (first (rsubseq thresholds <= k)))]
         (if (= a k)
           a
           (if-let [b (key (first (subseq thresholds >= k)))]
             (if (< (abs (- k b)) (abs (- k a)))
               b
               a)))
         (key (first (subseq thresholds >= k))))))

(defn make-client
  [options]
  (when (obtain-license *default-components*)
    (doto (NBiometricClient.)
      (.setMatchingThreshold (closest-threshold 0.00001))
      (.setFingersMatchingSpeed NMatchingSpeed/LOW)
      ;;finger quality threshold from 0-255
      (.setFingersQualityThreshold 10)
      (.setFingersTemplateSize NTemplateSize/LARGE)
      (.setMatchingWithDetails true)
      (.setFingersCalculateNFIQ true)
      (.setFingersMaximalRotation 10)
      (.setMatchingMaximalResultCount 20)
      (.setFingersFastExtraction false))))

(defn verify
  [client reference candidate]
  {:result (= (NBiometricStatus/OK) (.verify client reference candidate))
   :score (-> reference .getMatchingResults first .getScore)})

(defn build-matching-result
  [m]
  {:id (.getId m)
   :score (.getScore m)
   :details (.getMatchingDetails m)
   :subject (.getSubject m)})

(defn identify
  [client subject]
  (when (= (NBiometricStatus/OK) (.identify client subject))
    (map build-matching-result (.getMatchingResults subject))))

(defn enroll
  [client subject]
  (= (NBiometricStatus/OK) (.enroll client subject false)))

(defn create-template
  [client subject]
  (when (= (NBiometricStatus/OK) (.createTemplate client subject))
    (.getTemplate subject)))
