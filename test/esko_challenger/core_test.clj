(ns esko-challenger.core-test
  (:use clojure.test)
  (:require [esko-challenger.core :as core]))

(deftest ping-test
  (is (= "pong" (core/answer ["ping"]))))

(deftest say-hello-test
  (is (= "Hello World" (core/answer ["say-hello" "World"]))))

(deftest plus-test
  (is (= "10" (core/answer ["+" "3" "7"]))))

(deftest minus-test
  (is (= "-4" (core/answer ["-" "3" "7"]))))

(deftest fizzbuzz-test
  (is (= "1" (core/answer ["fizzbuzz" "1"])))
  (is (= "2" (core/answer ["fizzbuzz" "2"])))
  (is (= "Fizz" (core/answer ["fizzbuzz" "3"])))
  (is (= "Buzz" (core/answer ["fizzbuzz" "5"])))
  (is (= "FizzBuzz" (core/answer ["fizzbuzz" "15"]))))

(deftest palindrome?-test
  (is (= "true" (core/answer ["palindrome?" "innostunutsonni"])))
  (is (= "false" (core/answer ["palindrome?" "passiivinensonni"])))
  (is (= "true" (core/answer ["palindrome?" "Tiku, Matti maalasi salaa mittamukit."]))))
