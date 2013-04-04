(ns esko-challenger.core-test
  (:use clojure.test)
  (:require [esko-challenger.core :as core]))

(deftest ping-test
  (is (= "pong" (core/answer ["ping"]))))

(deftest plus-test
  (is (= "10" (core/answer ["+" "3" "7"]))))
