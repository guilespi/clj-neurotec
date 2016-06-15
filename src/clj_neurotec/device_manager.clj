(ns clj-neurotec.device-manager
  (:require [clj-neurotec.finger-scanner :as fscanner])
  (:import (com.neurotec.devices NDeviceManager NDeviceType)
           (com.neurotec.licensing NLicense)))

(defprotocol ILicenses
  (obtainLicenses [this])
  (releaseLicenses [this]))

(defprotocol IDeviceManager
  (initFingerScanners [this]))


(def components "Devices.FingerScanners")
(defrecord DeviceManager [license-server-address license-server-port opts]
  ILicenses
  (obtainLicenses [this]
    (when-let [licenses (get-in this [:opts :licenses])]
      (dorun (map #(NLicense/add %)
                  licenses)))
    (NLicense/obtainComponents ^String (:license-server-address this)
                               ^String (:license-server-port this)
                               ^String components))
  (releaseLicenses [this]
    (NLicense/releaseComponents ^String components))

  IDeviceManager
  (initFingerScanners [this]
    (->> (doto (NDeviceManager.)
           (.setDeviceTypes (java.util.EnumSet/of NDeviceType/FSCANNER))
           (.initialize))
         (.getDevices)
         (map fscanner/->FingerScanner))))


