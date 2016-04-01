(ns clj-neurotec.client
  (:require [clojure.walk])
  (:import (com.neurotec.biometrics.client NBiometricClient)
           (com.neurotec.biometrics NMatchingSpeed NTemplateSize NBiometricStatus NSubject
                                    NMatchingResult)
           (com.neurotec.licensing NLicense)))


(set! *warn-on-reflection* true)

(def ^:dynamic *default-components* "Biometrics.FingerExtraction,Biometrics.FingerMatching")

(defn obtain-license
  [components opts]
  (when (:licenses opts)
    (dorun (map #(NLicense/add %)
                (:licenses opts))))

  (NLicense/obtainComponents (or (:license-server-address opts) "/local")
                             (or (:license-server-port opts) 5000)
                             ^String components))

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
  (when (obtain-license *default-components* options)
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
  {:result (= (NBiometricStatus/OK) (.verify ^NBiometricClient client reference candidate))
   :score (.getScore ^NMatchingResult (first (.getMatchingResults ^NSubject reference)))})

(defn build-matching-result
  [m]
  {:id (.getId ^NMatchingResult m)
   :score (.getScore ^NMatchingResult m)
   :details (.getMatchingDetails ^NMatchingResult m)
   :subject (.getSubject ^NMatchingResult m)})

(defn identify
  [client subject]
  (when (= (NBiometricStatus/OK) (.identify ^NBiometricClient client subject))
    (map build-matching-result (.getMatchingResults ^NSubject subject))))

(defn enroll
  [client subject]
  (= (NBiometricStatus/OK) (.enroll ^NBiometricClient client subject false)))

(defn create-template
  [^NBiometricClient client ^NSubject subject]
  (when (= (NBiometricStatus/OK) (.createTemplate ^NBiometricClient client subject))
    (.getTemplate ^NSubject subject)))
