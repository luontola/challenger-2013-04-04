(ns esko-challenger.cache-test
  (:use clojure.test
        [datomic.api :only [db q] :as d])
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
      (is (= "known answer" (cache/recall answers "known question"))))

    (testing "New answers to old questions overwrite them"
      (cache/learn answers "updated question" "old answer")
      (cache/learn answers "updated question" "new answer")
      (is (= "new answer" (cache/recall answers "updated question"))))))

(deftest in-memory-answers-test
  (answers-contract cache/in-memory-answers))

(deftest filesystem-answers-test
  (with-temp-dir tmpdir
    (answers-contract #(cache/filesystem-answers (unique-subdir tmpdir)))))

(deftest datomic-answers-test
  (answers-contract #(cache/datomic-answers "datomic:mem://test1"))

  (testing "Learning the same question many times stores it only once"
    (let [conn-uri "datomic:mem://test2"
          answers (cache/datomic-answers conn-uri)
          conn (d/connect conn-uri)]

      (cache/learn answers "question" "answer 1")
      (cache/learn answers "question" "answer 2")
      (cache/learn answers "question" "answer 2")

      (let [entities (q '[:find ?c
                          :where [?c :challenge/answer ]] (db conn))]
        (is (= 1 (count entities)))))))
