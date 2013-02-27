(ns esko-challenger.proxy-test
  (:use clojure.test)
  (:require [esko-challenger.proxy :as proxy]))

(defn slow [result]
  (fn []
    (Thread/sleep 1000)
    result))

(defn fast [result]
  (fn []
    result))

(deftest first-success-test

  (testing "Returns the result of the first response"
    (let [response (proxy/first-success [(slow "slow1") (fast "fast") (slow "slow2")])]
      (is (= "fast" response))))

  (testing "Does not return nil if there is at least one non-nil response"
    (let [response (proxy/first-success [(fast nil) (fast "non-nil") (fast nil)])]
      (is (= "non-nil" response))))

  (testing "Returns nil if there are no successful responses"
    (let [response (proxy/first-success [(fast nil)])]
      (is (nil? response)))))
