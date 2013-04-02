(ns esko-challenger.core-test
  (:use clojure.test)
  (:require [esko-challenger.core :as core]))

(deftest ping-test
  (is (= "pong" (core/answer ["ping"]))))
