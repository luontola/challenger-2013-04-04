(ns esko-challenger.core-test
  (:use clojure.test)
  (:require [esko-challenger.core :as core]))

(deftest ping-test
  (is (= "pong" (core/answer ["ping"]))))

(deftest plus-test
  (is (= "10" (core/answer ["+" "3" "7"]))))

(deftest minus-test
  (is (= "-4" (core/answer ["-" "3" "7"]))))

(deftest nth-word-test
  (is (= "true" (core/answer ["palindrome?" "aba"])))
  (is (= "true" (core/answer ["palindrome?" "abba"])))
  (is (= "false" (core/answer ["palindrome?" "abc"])))
  (is (= "true" (core/answer ["palindrome?" "A bba."]))))
