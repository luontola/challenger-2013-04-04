(ns esko-challenger.core
  (:use ring.util.response
        compojure.core)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [net.cgrand.enlive-html :as html]
            [clojure.string :as str])
  (:import [org.slf4j LoggerFactory Logger]))

(defn palindrome? [word]
  (let [word (seq (.replaceAll (.toLowerCase word) "[^a-z]" ""))]
    (= word (reverse word))))

(defn parse-ints [strings]
  (map #(Integer/parseInt %) strings))

(def fib-seq
  ((fn rfib [a b]
     (lazy-seq (cons a (rfib b (+ a b)))))
    (bigint 0) (bigint 1)))

(defn fibonacci [n]
  (nth fib-seq n))

(defn answer [question]
  (let [[op & args] question]
    (cond
      (= op "ping") "pong"
      (= op "+") (str (apply + (parse-ints args)))
      (= op "-") (str (apply - (parse-ints args)))
      (= op "palindrome?") (str (palindrome? (first args)))
      (= op "sort") (str (str/join ", " (sort (parse-ints args))))
      (= op "fibonacci") (str (fibonacci (first (parse-ints args))))
      :else nil)))

(defn make-routes []
  (->
    (routes
      (POST "/" {body :body} (answer (str/split-lines (slurp body))))
      (GET "/" [] "It's me, Mario!")
      (route/not-found "404 Page Not Found"))
    (handler/site)))
