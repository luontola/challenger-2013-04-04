(ns esko-challenger.cache-test
  (:use clojure.test)
  (:require [esko-challenger.cache :as cache]))

(deftest unknown-question-test
  (is (nil? (cache/answer "question" {}))))

(deftest known-question-test
  (is (= "answer" (cache/answer "question" {"question" "answer"}))))
