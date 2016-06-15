(ns clj-neurotec.enums
  (:import (com.neurotec.biometrics NFPosition NGender)))

(def genders {:male NGender/MALE
              :female NGender/FEMALE
              :unknown NGender/UNKNOWN
              :unspecified NGender/UNSPECIFIED})

(def finger-positions {:right-index-finger NFPosition/RIGHT_INDEX_FINGER
                       :right-middle-finger NFPosition/LEFT_MIDDLE_FINGER
                       :right-ring-finger NFPosition/LEFT_RING_FINGER
                       :right-little-finger NFPosition/LEFT_LITTLE_FINGER
                       :right-thumb-finger NFPosition/LEFT_THUMB
                       :left-index-finger NFPosition/LEFT_INDEX_FINGER
                       :left-middle-finger NFPosition/LEFT_MIDDLE_FINGER
                       :left-ring-finger NFPosition/LEFT_RING_FINGER
                       :left-little-finger NFPosition/LEFT_LITTLE_FINGER
                       :left-thumb-finger NFPosition/LEFT_THUMB
                       :unknown NFPosition/UNKNOWN})

