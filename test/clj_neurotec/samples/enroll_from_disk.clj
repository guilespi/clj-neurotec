(ns clj-neurotec.samples.enroll-from-disk
  (:gen-class)
  (:require [clj-neurotec.client :as c]
            [clj-neurotec.subject :as s]
            [clj-neurotec.template :as t]
            [clj-neurotec.util :as u]
            [clojure.java.io :as io]))

(defn templates-plain
  [dir]
  (let [files (filter #(and (.startsWith (.getName %) "two-") (.endsWith (.getName %) "dat")) (file-seq (io/file dir)))]
    (map #(t/from-file (.getPath %)) files)))

(defn get-template-metrics
  [dir]
  (for [t (templates-plain dir)]
    (map #(select-keys % [:quality :minutia-count :size]) (t/get-nf-records t))))

(defmacro traverse-and-apply
  [dir fun extension]
  `(let [files# (filter #(.endsWith (.getPath %) ~extension) (file-seq (io/file ~dir)))]
     (doseq [f# files#]
       (println "Enrolling " (.getName f#))
       (let [finger# (s/finger-from-file (.getPath f#))
             subject# (s/make-subject {:id (.getName f#)
                                       :fingers [finger#]})]
         (~fun subject#)))))

(defn enroll-from-dir
  [client dir]
  (println "Enrolling from dir" dir)
  (traverse-and-apply dir (partial c/enroll client) "bmp"))

(defn identify-from-file
  [client filename]
  (println "Identifying from file" filename)
  (let [finger (s/finger-from-file filename)
        subject (s/make-subject {:id (str (java.util.UUID/randomUUID))
                                 :fingers [finger]})]
    (println "Identifying subject" subject)
    (c/identify client subject)))

(defn save-template
  [dir subject template]
  (let [path (str dir "/" (.getId subject) ".dat")
        _ (println "Saving template" path)]
    (t/save template path)))

(defn templates-from-dir
  [client dir]
  (letfn [(create-template [subject]
            (when-let [tmp (c/create-template client subject)]
              (save-template dir subject tmp)))]
    (traverse-and-apply dir create-template "bmp")))

(defn -main
  []
  (println "Running enroll from disk")
  (u/init-library-path!)
  (u/load-libraries)
  (let [client (c/make-client {})]
    (enroll-from-dir client "/home/guilespi/Dbs/DB4_A")
    (identify-from-file client "/home/guilespi/Dbs/DB4_A/9_10.bmp")))


(defn subject-from-template
  [template-file]
  (s/make-subject {:id (.getName template-file)
                   :template (t/from-file (.getPath template-file))}))

(defn enroll-from-templates
  [client dir]
  (let [files (filter #(.endsWith (.getPath %) "dat") (file-seq (io/file dir)))]
    (doseq [f files]
      (c/enroll client (subject-from-template f)))))


(comment
  (let [client (c/make-client {})]
    (templates-from-dir client "/Users/guilespi/Downloads/Dbs/DB4_A"))
    (templates-from-dir client "/home/guilespi/Dbs/DB4_A"))

  )
