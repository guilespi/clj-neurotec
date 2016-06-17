(ns clj-neurotec.util
  (:import (com.sun.jna Platform)))

(defonce file-separator (System/getProperty "file.separator"))
(defonce path-separator (System/getProperty "path.separator"))

(defmulti get-os-path (fn []
                        (cond
                         (Platform/isWindows) :windows
                         (Platform/isLinux) :linux
                         :else :mac)))

;;TODO: this path mangling strategy was copied from the tutorials source
;;code and is a piece of shit, needs rethinking

(defmethod get-os-path :windows
  []
	"")

(defmethod get-os-path :linux
  []
  
 	"")

(defmethod get-os-path :mac
  []
  "/Users/guilespi/Documents/Development/neurotechnology/Neurotec_Biometric_6_0_SDK_Trial/Frameworks/MacOSX/")

(defn init-library-path!
  "Updates jna and java library paths in order for the Neurotec
   native libraries to be found"
  []
  (let [field-sys-path (.getDeclaredField ClassLoader "sys_paths")
        os-path (get-os-path)
        jna-path (or (System/getProperty "jna.library.path") "")
        lava-path (or (System/getProperty "java.library.path") "")]
    (System/setProperty "jna.library.path" (format "%s%s%s" jna-path path-separator os-path))
    (System/setProperty "java.library.path" (format "%s%s%s" lava-path path-separator os-path))
    (.setAccessible field-sys-path true)
    (.set field-sys-path nil nil)))


(defn load-libraries
  "Loads libraries in the proper places for clojure to see them, see here:
   https://groups.google.com/forum/#!searchin/clojure/classloader%7Csort:date/clojure/px5uYUU8sfM/N8ZVXkgYdxUJ"
  []
  (init-library-path!)
  (doseq [lib ["NCore" "NLicensing" "NBiometrics" "NBiometricClient" "NMedia" "NMediaProc"]]
    (clojure.lang.RT/loadLibrary lib)))
