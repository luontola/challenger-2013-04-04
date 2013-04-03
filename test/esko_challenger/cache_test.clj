(ns esko-challenger.cache-test
  (:use clojure.test)
  (:require [esko-challenger.cache :as cache]
            [me.raynes.fs :as fs]))

(defn- unique-subdir [dir]
  (let [subdir (fs/file dir (str (rand-int Integer/MAX_VALUE)))]
    (fs/mkdir subdir)
    subdir))

(defmacro with-temp-dir [var-name & body]
  `(let [~var-name (fs/temp-dir "test")]
     (try
       ~@body
       (finally
         (fs/delete-dir ~var-name)))))


(deftest unknown-question-test
  (is (nil? (cache/answer "question" (cache/in-memory-answers)))))

(deftest known-question-test
  (is (= "answer" (cache/answer "question" (cache/in-memory-answers {"question" "answer"})))))


(defn answers-contract [answers-factory]
  (let [answers (answers-factory)]
    (cache/learn answers "known question" "known answer")

    (testing "Does not recall unknown answers"
      (is (nil? (cache/recall answers "unknown question"))))

    (testing "Recalls previously learned answers"
      (is (= "known answer" (cache/recall answers "known question"))))))

(deftest in-memory-answers-test
  (answers-contract cache/in-memory-answers))

(deftest filesystem-answers-test
  (with-temp-dir tmpdir
    (answers-contract #(cache/filesystem-answers (unique-subdir tmpdir)))))

(deftest datomic-answers-test
  (answers-contract #(cache/datomic-answers "datomic:mem://test")))
