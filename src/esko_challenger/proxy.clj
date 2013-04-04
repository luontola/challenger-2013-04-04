(ns esko-challenger.proxy
  (:use ring.util.response
        compojure.core)
  (:require [esko-challenger.http :as http]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [net.cgrand.enlive-html :as html]
            [clojure.string :as string]
            [clj-json.core :as json])
  (:import [org.slf4j LoggerFactory Logger]
           [java.util.concurrent Executors ExecutorService TimeUnit]))

(defonce executor (Executors/newCachedThreadPool))

(defn wrap-non-nil [f]
  (fn []
    (let [result (f)]
      (if (nil? result)
        (throw (NullPointerException.)))
      result)))

(defn first-success [commands]
  (try
    (.invokeAny executor (map wrap-non-nil commands) 900 TimeUnit/MILLISECONDS)
    (catch Exception e
      nil)))

(defn ask-backend [url question]
  (let [response (http/post-request url question)
        status-code (:code (:status response))]
    (if (= 200 status-code)
      (:body response)
      nil)))

(defn make-asker [url question]
  (fn [] (ask-backend url question)))

(defn local-backend-urls [low-port high-port]
  (map #(str "http://localhost:" %) (range low-port (inc high-port))))

(defn ask-proxies [question urls]
  (let [commands (map #(make-asker % question) urls)]
    (first-success commands)))

(defn watch-updates [content-fn]
  (let [content (ref (content-fn))
        update #(dosync (ref-set content (content-fn)))
        update-loop #(while true
                       (try
                         (update)
                         (Thread/sleep 5000)
                         (catch Throwable t
                           (.printStackTrace t))))]
    (.start (Thread. update-loop "watch-updates"))
    content))

(defn make-backend-list-monitor [file]
  (let [watcher (watch-updates #(string/split-lines (slurp file)))]
    (fn [] @watcher)))

(defn make-routes [urls-fn]
  (->
    (routes
      (POST "/" {body :body} (ask-proxies (slurp body) (urls-fn)))
      (GET "/" [] (str "Proxying:\n" (string/join "\n" (urls-fn))))
      (route/not-found "No Answer"))
    (handler/site)))
