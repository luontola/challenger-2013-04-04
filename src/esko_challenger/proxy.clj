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

(defn backend-urls [low-port high-port]
  (map #(str "http://localhost:" %) (range low-port (inc high-port))))

(defn ask-proxies [question low-port high-port]
  (let [urls (backend-urls low-port high-port)
        commands (map #(make-asker % question) urls)]
    (first-success commands)))

(defn make-routes [low-port high-port]
  (->
    (routes
      (POST "/" {body :body} (ask-proxies (slurp body) low-port high-port))
      (GET "/" [] (str "I'm proxying " low-port "-" high-port))
      (route/not-found "Answer Not Known"))
    (handler/site)))
