(ns clj-neurotec.util
  (:import (com.sun.jna Platform)))

(defonce file-separator (System/getProperty "file.separator"))
(defonce path-separator (System/getProperty "path.separator"))

(defmulti get-os-path (fn []
                        (cond
                         (Platform/isWindows) :windows
                         (Platform/isLinux) :linux
                         :isMac :mac)))

(defmethod get-os-path :windows
  []
  (let [working-dir (System/getProperty "user.dir")
        parts (butlast (clojure.string/split working-dir (re-pattern file-separator)))]
    (if (.endsWith (last parts) "Bin")
      (clojure.string/join (concat parts [file-separator (if (Platform/is64Bit) "Win64_x64" "Win32_x86")]))
      "")))

(defmethod get-os-path :linux
  []
  (let [working-dir (System/getProperty "user.dir")
        parts (butlast (clojure.string/split working-dir (re-pattern file-separator)))]
    (if (> (count parts) 1)
      (clojure.string/join (concat (butlast parts)
                                   [file-separator "Lib" file-separator (if (Platform/is64Bit) "Linux_x86_64" "Linux_x86")]))
      "")))

(defmethod get-os-path :mac
  []
  "/Library/Frameworks/Neurotechnology/")

(defn init-library-path!
  []
  (let [field-sys-path (.getDeclaredField ClassLoader "sys_paths")
        os-path (get-os-path)
        _ (println os-path)
        jna-path (or (System/getProperty "jna.library.path") "")
        lava-path (or (System/getProperty "java.library.path") "")]
    (System/setProperty "jna.library.path" (format "%s%s%s" jna-path path-separator os-path))
    (System/setProperty "java.library.path" (format "%s%s%s" lava-path path-separator os-path))
    (.setAccessible field-sys-path true)
    (.set field-sys-path nil nil)))


(defn load-libraries
  []
  (init-library-path!)
  (clojure.lang.RT/loadLibrary "NCore"))
