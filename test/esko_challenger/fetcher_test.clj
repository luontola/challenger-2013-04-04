(ns esko-challenger.fetcher-test
  (:use clojure.test)
  (:require [esko-challenger.fetcher :as fetcher])
  (:import [java.io File]))

(deftest parse-page-test
  (let [page (File. "test/participant-details.html")
        answers (fetcher/parse-page page)]
    (is (= answers {"ping" "pong",
                    "the question" "the expected answer"}))))
