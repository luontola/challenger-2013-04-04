(ns esko-challenger.cache
  (:use ring.util.response
        compojure.core)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [net.cgrand.enlive-html :as html]
            [clojure.string :as str])
  (:import [org.slf4j LoggerFactory Logger]))

(defn answer [question known-answers]
  (get known-answers question))

(defn make-routes []
  (->
    (routes
      (POST "/" {body :body} (answer (slurp body) {}))
      (GET "/" [] "It's me, Luigi!")
      (route/not-found "Answer Not Found"))
    (handler/site)))
