(ns clj-neurotec.template
  (:import (com.neurotec.biometrics NFMinutiaFormat NTemplate)
           (com.neurotec.io NFile NBuffer)))


(defn from-file
  [file]
  (let [buffer (NFile/readAllBytes file)]
    (NTemplate. buffer)))

(defn- rotation->degrees
  [rotation]
  (double (/ (+ (* rotation 2 360) 256)
             (* 2 256))))

(defn- has-quality?
  [format]
  (.contains format NFMinutiaFormat/HAS_QUALITY))

(defn- has-g?
  [format]
  (.contains format NFMinutiaFormat/HAS_G))

(defn- has-curvature?
  [format]
  (.contains format NFMinutiaFormat/HAS_CURVATURE))

(defn- build-minutia
  [format m]
  {:x (. m x)
   :y (. m y)
   :angle (rotation->degrees (bit-and (. m angle) 0xFF))
   :quality (when (has-quality? format) (bit-and (. m quality) 0xFF))
   :g (when (has-g? format) (. m g))
   :curvature (when (has-curvature? format) (. m curvature))})

(defn- build-nf-record
  [r]
  {:g (.getG r)
   :impression-type (.getImpressionType r)
   :pattern-class (.getPatternClass r)
   :beff-product-type (.getCBEFFProductType r)
   :position (.getPosition r)
   :ridge-counts-type (.getRidgeCountsType r)
   :width (.getWidth r)
   :height (.getHeight r)
   :horizontal-resolution (.getHorzResolution r)
   :vertical-resolution (.getVertResolution r)
   :quality (.getQuality r)
   :size (.getSize r)
   :minutia-count (.. r getMinutiae size)
   :minutia (doall (map (partial build-minutia (.getMinutiaFormat r)) (.getMinutiae r)))})

(defn get-nf-records
  [template]
  (when-let [fingers (.getFingers template)]
    (for [r (.getRecords fingers)]
      (build-nf-record r))))

(comment

  (def t (from-file "/Users/guilespi/Documents/Development/interrupted/biometrics/Neurotech/Bin/watson/index.dat"))

)
