(ns esko-challenger.cache-test
  (:use clojure.test)
  (:require [esko-challenger.cache :as cache]
            [me.raynes.fs :as fs]))

(defn- unique-subdir [dir]
  (let [subdir (fs/file dir (str (rand-int Integer/MAX_VALUE)))]
    (fs/mkdir subdir)
    subdir))


(deftest unknown-question-test
  (is (nil? (cache/answer "question" (cache/in-memory-answers)))))

(deftest known-question-test
  (is (= "answer" (cache/answer "question" (cache/in-memory-answers {"question" "answer"})))))


(defn answers-contract [answers-factory]
  (let [answers (answers-factory)]
    (cache/learn answers "question 1" "answer 1")

    (testing "Does not recall unknown answers"
      (is (nil? (cache/recall answers "question 2"))))

    (testing "Recalls previously learned answers"
      (is (= "answer 1" (cache/recall answers "question 1"))))))

(deftest in-memory-answers-test
  (answers-contract cache/in-memory-answers))

(deftest filesystem-answers-test
  (let [tmpdir (fs/temp-dir "filesystem-answers-test")]
    (try
      (answers-contract #(cache/filesystem-answers (unique-subdir tmpdir)))
      (finally
        (fs/delete-dir tmpdir)))))
