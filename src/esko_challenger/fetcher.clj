(ns esko-challenger.fetcher
  (:require [net.cgrand.enlive-html :as html]
            [clojure.string :as str]))


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
