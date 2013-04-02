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

(defprotocol Answers
  (recall [this question])
  (learn [this question answer]))

; InMemoryAnswers

(deftype InMemoryAnswers [answers]
  Answers
  (recall [this question] (get @answers question))
  (learn [this question answer] (dosync (alter answers assoc question answer))))

(defn in-memory-answers
  ([] (in-memory-answers {}))
  ([answers-map] (InMemoryAnswers. (ref answers-map))))


; FileSystemAnswers

(defn- read-file [^File file]
  (if (.exists file)
    (slurp file :encoding "UTF-8")
    nil))

(defn- write-file [^File file ^String content]
  (if (not (= content (read-file file)))
    (spit file content :encoding "UTF-8")))

(defn- answer-path [^File dir ^String question]
  (File. dir (digest/md5 question)))

(deftype FileSystemAnswers [^File dir]
  Answers
  (recall [this question]
    (read-file (answer-path dir question)))
  (learn [this question answer]
    (write-file (answer-path dir question) answer)))

(defn filesystem-answers [^File dir]
  (.mkdirs dir)
  (FileSystemAnswers. dir))

; TODO: database-backed Answers (MongoDB, Memcached, Datomic or similar)


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
