(ns esko-challenger.cache
  (:use ring.util.response
        compojure.core)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [net.cgrand.enlive-html :as html]
            [clojure.string :as str]
            [digest :as digest])
  (:import [org.slf4j LoggerFactory Logger]
           [java.io File]))


; persistence

(defprotocol Answers
  (recall [this question])
  (learn [this question answer]))


(deftype InMemoryAnswers [answers]
  Answers
  (recall [this question] (get @answers question))
  (learn [this question answer] (dosync (alter answers assoc question answer))))

(defn in-memory-answers
  ([] (in-memory-answers {}))
  ([answers-map] (InMemoryAnswers. (ref answers-map))))


(defn- answer-path [dir question]
  (File. dir (digest/md5 question)))

(defn- read-file [file]
  (if (.exists file)
    (slurp file :encoding "UTF-8")
    nil))

(defn- write-file [file content]
  (if (not (= content (read-file file)))
    (spit file content :encoding "UTF-8")))

(deftype FileSystemAnswers [dir]
  Answers
  (recall [this question]
    (read-file (answer-path dir question)))
  (learn [this question answer]
    (write-file (answer-path dir question) answer)))

(defn filesystem-answers [dir]
  (FileSystemAnswers. dir))


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
