(ns esko-challenger.fetcher-test
  (:use clojure.test)
  (:require [esko-challenger.fetcher :as fetcher])
  (:import [java.io File]))

(deftest parse-strikes-test
  (let [page (File. "test/participant-details.html")
        answers (fetcher/parse-strikes page)]
    (is (= answers {"ping" "pong",
                    "the question" "the expected answer"}))))

(deftest parse-participants-urls
  (let [page (File. "test/tournament-overview.html")
        participans (fetcher/parse-participans page)]
    (is (= participans ["/participant-1" "/participant-2"]))))

; TODO: background poller thread
