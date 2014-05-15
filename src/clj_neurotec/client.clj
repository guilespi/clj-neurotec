(ns clj-neurotec.client
  (:require [clojure.walk])
  (:import (com.neurotec.biometrics.client NBiometricClient)
           (com.neurotec.biometrics NMatchingSpeed NTemplateSize)
           (com.neurotec.licensing NLicense)))


(def ^:dynamic *default-components* "Biometrics.FingerExtraction,Biometrics.FingerMatching")

(defn obtain-license
  [components]
  (NLicense/obtainComponents "/local" 5000 components))

(defn release-license
  [components]
  (NLicense/releaseComponents components))

(defn make-client
  [options]
  (when (obtain-license *default-components*)
    (doto (NBiometricClient.)
      (.setMatchingThreshold 48)
      (.setFingersMatchingSpeed NMatchingSpeed/LOW)
      (.setFingersQualityThreshold 10)
      (.setFingersTemplateSize NTemplateSize/LARGE)
      (.setMatchingWithDetails true)
      (.setFingersCalculateNFIQ true)
      (.setFingersMaximalRotation 10)
      (.setMatchingMaximalResultCount 20)
      (.setFingersFastExtraction false))))

(defn verify
  [client reference candidate]
  (.verify client reference candidate))

(defn identify
  [client subject]
  (.identify client subject))

(defn enroll
  [client subject]
  (.enroll client subject false))
