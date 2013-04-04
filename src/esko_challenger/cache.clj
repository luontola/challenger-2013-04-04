(ns esko-challenger.cache
  (:use ring.util.response
        compojure.core
        [datomic.api :only [db q] :as d])
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


; DatomicAnswers

(deftype DatomicAnswers [conn]
  Answers
  (recall [this question]
      (let [results (q '[:find ?answer
           :in $ ?question
           :where
           [?c :challenge/answer ?answer]
           [?c :challenge/question ?question]] (db conn) question)]
        (first (first results))))
  (learn [this question answer]
    (d/transact conn [{:db/id #db/id[:db.part/user]
                       :challenge/question question
                       :challenge/answer answer}])))

(defn datomic-answers [conn-uri]
  (if (d/create-database conn-uri)
    (let [conn (d/connect conn-uri)]
      (d/transact conn [{:db/id #db/id[:db.part/db]
                        :db/ident :challenge/question
                        :db/valueType :db.type/string
                        :db/unique :db.unique/identity
                        :db/cardinality :db.cardinality/one
                        :db/doc "A challenge's question"
                        :db.install/_attribute :db.part/db}])
      (d/transact conn [{:db/id #db/id[:db.part/db]
                        :db/ident :challenge/answer
                        :db/valueType :db.type/string
                        :db/cardinality :db.cardinality/one
                        :db/doc "A challenge's answer"
                        :db.install/_attribute :db.part/db}])))
  (DatomicAnswers. (d/connect conn-uri)))


; routing

(defn answer [question answers]
  (recall answers question))

(defn make-routes [answers]
  (->
    (routes
      (POST "/" {body :body} (answer body answers))
      (GET "/" [] "It's me, Luigi!")
      (route/not-found "Answer Not Found"))
    (handler/site)))
