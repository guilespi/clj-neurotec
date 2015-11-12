(ns clj-neurotec.template
  (:import (com.neurotec.biometrics NFMinutiaFormat NTemplate NFRecord NFMinutia NFTemplate)
           (com.neurotec.io NFile NBuffer)
           (java.util EnumSet)))

(set! *warn-on-reflection* true)

(defn from-file
  [file]
  (let [buffer (NFile/readAllBytes file)]
    (NTemplate. buffer)))

(defn from-bytes
  [bytes]
  (NTemplate. (NBuffer. bytes)))

(defn- rotation->degrees
  [rotation]
  (double (/ (+ (* rotation 2 360) 256)
             (* 2 256))))

(defn- has-quality?
  [format]
  (.contains ^EnumSet format NFMinutiaFormat/HAS_QUALITY))

(defn- has-g?
  [format]
  (.contains ^EnumSet format NFMinutiaFormat/HAS_G))

(defn- has-curvature?
  [format]
  (.contains ^EnumSet format NFMinutiaFormat/HAS_CURVATURE))

(defn- build-minutia
  [format m]
  {:x (. ^NFMinutia m x)
   :y (. ^NFMinutia m y)
   :angle (rotation->degrees (bit-and (. ^NFMinutia m angle) 0xFF))
   :quality (when (has-quality? format) (bit-and (. ^NFMinutia m quality) 0xFF))
   :g (when (has-g? format) (. ^NFMinutia m g))
   :curvature (when (has-curvature? format) (. ^NFMinutia m curvature))})

(defn- build-nf-record
  [r]
  {:g (.getG ^NFRecord r)
   :impression-type (.getImpressionType ^NFRecord r)
   :pattern-class (.getPatternClass ^NFRecord r)
   :beff-product-type (.getCBEFFProductType ^NFRecord r)
   :position (.getPosition ^NFRecord r)
   :ridge-counts-type (.getRidgeCountsType ^NFRecord r)
   :width (.getWidth ^NFRecord r)
   :height (.getHeight ^NFRecord r)
   :horizontal-resolution (.getHorzResolution ^NFRecord r)
   :vertical-resolution (.getVertResolution ^NFRecord r)
   :quality (.getQuality ^NFRecord r)
   :size (.getSize ^NFRecord r)
   :minutia-count (.. ^NFRecord r getMinutiae size)
   :minutia (doall (map (partial build-minutia (.getMinutiaFormat ^NFRecord r)) (.getMinutiae ^NFRecord r)))})

(defn get-nf-records
  [template]
  (when-let [fingers (.getFingers ^NTemplate template)]
    (for [r (.getRecords ^NFTemplate fingers)]
      (build-nf-record r))))

(defn buffer
  [template]
  ;;TODO move the buffer to an autogrow buffer
  (let [buff (NBuffer. 50000)]
    (.save ^NTemplate template buff)
    buff))

(defn save
  [^NTemplate template ^String path]
  (NFile/writeAllBytes path (buffer template)))

(comment

  (def t (from-file "/Users/guilespi/Documents/Development/interrupted/biometrics/Neurotech/Bin/watson/index.dat"))

)
