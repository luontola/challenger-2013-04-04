(ns esko-challenger.core
  (:use ring.util.response
        compojure.core)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [net.cgrand.enlive-html :as html]
            [clojure.string :as string]
            [clj-json.core :as json])
  (:import [org.slf4j LoggerFactory Logger]))

(defn divisible? [n div] (= 0 (mod n div)))

(defn fizzbuzz [n]
  (cond
    (and (divisible? n 3) (divisible? n 5)) "FizzBuzz"
    (divisible? n 3) "Fizz"
    (divisible? n 5) "Buzz"
    :else (str n)))

(defn palindrome? [s]
  (let [s (.toLowerCase (clojure.string/replace s #"\W" ""))]
    (= s (apply str (reverse s)))))

(defn answer [question]
  (let [[op & args] question]
    (cond
      (= op "ping") "pong"
      (= op "say-hello") (str "Hello " (first args))
      (= op "+") (str (apply + (map #(Integer/parseInt %) args)))
      (= op "-") (str (apply - (map #(Integer/parseInt %) args)))
      (= op "fizzbuzz") (fizzbuzz (Integer/parseInt (first args)))
      (= op "palindrome?") (str (palindrome? (first args)))
      :else nil)))

(defn make-routes []
  (->
    (routes
      (POST "/" {body :body} (answer (string/split-lines (slurp body))))
      (GET "/" [] "It's me, Mario!")
      (route/not-found "404 Page Not Found"))
    (handler/site)))
