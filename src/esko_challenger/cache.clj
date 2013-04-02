(ns esko-challenger.cache
  (:use ring.util.response
        compojure.core)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [net.cgrand.enlive-html :as html]
            [clojure.string :as str])
  (:import [org.slf4j LoggerFactory Logger]))

; cache

(defprotocol Answers
  (recall [this key])
  (learn [this key value]))

(deftype InMemoryAnswers [answers]
  Answers
  (recall [this key] (get @answers key))
  (learn [this key value] (dosync (alter answers assoc key value))))

(defn in-memory-answers
  ([] (in-memory-answers {}))
  ([answers-map] (InMemoryAnswers. (ref answers-map))))


; routing

(defn answer [question answers]
  (recall answers question))

(defn make-routes [answers]
  (->
    (routes
      (POST "/" {body :body} (answer (slurp body) answers))
      (GET "/" [] "It's me, Luigi!")
      (route/not-found "Answer Not Found"))
    (handler/site)))
