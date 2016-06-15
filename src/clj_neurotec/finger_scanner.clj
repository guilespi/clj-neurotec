(ns clj-neurotec.finger-scanner
  (:require [clj-neurotec.enums :refer [genders finger-positions]])

  (:import (com.neurotec.devices NDevice NBiometricDevice)
           (com.neurotec.devices.event NBiometricDeviceCapturePreviewListener NBiometricDeviceCapturePreviewEvent)
           (com.neurotec.biometrics NFinger NFrictionRidge NFPosition NBiometricStatus)
           (com.neurotec.images NImage NImageFormat)
           (com.neurotec.io NBuffer)
           (java.io ByteArrayInputStream ByteArrayOutputStream OutputStream)
           (javax.imageio ImageIO)))

(defprotocol IFingerScanner
  (startCapture [this finger-position callback])
  (stopCapture [this])
  (get-name [this]))


(defrecord FingerScanner [NFingerScanner]
  IFingerScanner
  (startCapture [this finger-position callback]
    (let [fmt (NImageFormat/getPNG)
          finger (doto (NFinger.)
                   (.setPosition (get finger-positions finger-position)))
          preview-listener (reify
                             NBiometricDeviceCapturePreviewListener
                             (capturePreview [this var1]
                               (let [biometric (.getBiometric var1)]
                                 (when (.getStatus biometric)
                                     (callback (.getValue (.getStatus biometric))
                                               (.getImage finger))))))]
      (doto (:NFingerScanner this)
        (.addCapturePreviewListener preview-listener)
        (.capture finger -1)
        (.removeCapturePreviewListener preview-listener))
      finger))

  (stopCapture [this]
    (.cancel (:NFingerScanner this)))

  (get-name [this]
    (.getDisplayName (:NFingerScanner this))))




