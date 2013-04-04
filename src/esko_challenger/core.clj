(ns esko-challenger.core
  (:use ring.util.response
        compojure.core)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [net.cgrand.enlive-html :as html]
            [clojure.string :as str])
  (:import [org.slf4j LoggerFactory Logger]))

(defn answer [question]
  (let [[op & args] question]
    (cond
      (= op "ping") "pong"
      (= op "+") (str (apply + (map #(Integer/parseInt %) args)))
      :else nil)))

(defn make-routes []
  (->
    (routes
      (POST "/" {body :body} (answer (str/split-lines (slurp body))))
      (GET "/" [] "It's me, Mario!")
      (route/not-found "404 Page Not Found"))
    (handler/site)))
