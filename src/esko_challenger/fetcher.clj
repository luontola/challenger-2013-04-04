(ns esko-challenger.fetcher
  (:require [esko-challenger.cache :as cache]
            [net.cgrand.enlive-html :as html]
            [clojure.string :as str])
  (:import [org.slf4j LoggerFactory Logger]
           [java.net URL]))


; parsing challenger pages

(defn- column-indexes [header]
  (apply hash-map (flatten (map-indexed
                             (fn [idx column]
                               [(apply str (:content column)) (inc idx)])
                             (html/select header [:th ])))))

(defn- nth-column-texts [rows column-index]
  (map html/text (html/select rows [:tr [:td (html/nth-of-type column-index)]])))

(defn- parse-strikes-table [table]
  (let [rows (html/select table [:tr ])
        header (first rows)
        strike-rows (rest rows)
        columns (column-indexes header)
        questions (nth-column-texts strike-rows (get columns "Input"))
        answers (nth-column-texts strike-rows (get columns "Expected"))]
    (zipmap questions answers)))

(defn parse-strikes [details-page]
  (let [tables (html/select (html/html-resource details-page) [:table.strikes ])]
    (apply merge (map parse-strikes-table tables))))


(defn parse-participans [overview-page]
  (let [table (first (html/select (html/html-resource overview-page) [:table#participants ]))
        links (html/select table [:a ])]
    (map #(:href (:attrs %)) links)))


; background poller thread

(def logger (LoggerFactory/getLogger (str (ns-name *ns*))))

(defn fetch-answers [^URL challenger-url cache]
  (.info logger "Locating participants from {}" challenger-url)
  (let [participants (parse-participans challenger-url)
        participants (map #(URL. challenger-url %) participants)]

    (doseq [participant participants]
      (.info logger "Fetching strikes from {}" participant)
      (doseq [[question answer] (seq (parse-strikes participant))]
        (cache/learn cache question answer)))))

(defn- infinite-loop [f]
  (while (not (Thread/interrupted))
    (try
      (f)
      (catch Throwable t
        (.warn logger "Uncaught exception" t)))))

(defn start [^URL challenger-url cache]
  (let [poller #(fetch-answers challenger-url cache)]
    (.start (Thread. #(infinite-loop poller) "answer-fetcher"))))
