(ns clj-neurotec.samples.enroll-from-disk
  (:gen-class)
  (:require [clj-neurotec.client :as c]
            [clj-neurotec.subject :as s]
            [clj-neurotec.util :as u]
            [clojure.java.io :as io]))


(defn enroll-from-dir
  [client dir]
  (println "Enrolling from dir" dir)
  (let [files (take 1 (file-seq (io/file dir)))]
    (doseq [f files]
      (let [finger (s/finger-from-file (.getPath f))
            subject (s/make-subject {:id (.getName f)
                                     :fingers [finger]})]
        (println "Enrolling subject" subject)
        (c/enroll client subject)))))

(defn identify-from-file
  [client filename]
  (println "Identifying from file" filename)
  (let [finger (s/finger-from-file filename)
        subject (s/make-subject {:id (str (java.util.UUID/randomUUID))
                                 :fingers [finger]})]
    (println "Identifying subject" subject)
    (c/identify client subject)))

(defn -main
  []
  (println "Running enroll from disk")
  (u/init-library-path!)
  (let [client (c/make-client {})]
    (enroll-from-dir client "/home/guilespi/Dbs/DB4_A")
    (identify-from-file client "/home/guilespi/Dbs/DB4_A/9_10.bmp")))
