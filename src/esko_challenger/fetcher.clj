(ns esko-challenger.fetcher
  (:require [net.cgrand.enlive-html :as html]
            [clojure.string :as str]))

(defn- column-indexes [header]
  (apply hash-map (flatten (map-indexed
                             (fn [idx column]
                               [(apply str (:content column)) (inc idx)])
                             (html/select header [:th ])))))

(defn- nth-column-texts [rows column-index]
  (map html/text (html/select rows [:tr [:td (html/nth-of-type column-index)]])))

(defn- parse-table [table]
  (let [rows (html/select table [:tr ])
        header (first rows)
        strike-rows (rest rows)
        columns (column-indexes header)
        questions (nth-column-texts strike-rows (get columns "Input"))
        answers (nth-column-texts strike-rows (get columns "Expected"))]
    (zipmap questions answers)))

(defn parse-page [resource]
  (let [tables (html/select (html/html-resource resource) [:table.strikes ])]
    (apply merge (map parse-table tables))))
