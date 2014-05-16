(ns clj-neurotec.samples.enroll-from-disk
  (:require [clj-neurotec.client :as client]
            [clj-neurotec.subject :as subject]
            [clojure.java.io :as io]))


(defn enroll-from-dir
  [cli dir]
  (let [files (take 1 (file-seq (io/file dir)))]
    (doseq [f files]
      (let [finger (subject/finger-from-file (.getPath f))
            sbj (subject/make-subject {:id (.getName f)
                                       :fingers [finger]})]
        (client/enroll cli sbj)))))

(defn identify-from-file
  [cli filename]
  (let [finger (subject/finger-from-file filename)
        sbj (subject/make-subject {:id (str (java.util.UUID/randomUUID))
                                   :fingers [finger]})]
    (client/identify cli sbj)))

(comment
  (let [bio-cli (client/make-client {})]
    (enroll-from-dir bio-cli "/home/guilespi/Dbs/DB4_A")
    (identify-from-file bio-cli "/home/guilespi/Dbs/DB4_A/9_10.bmp")))
