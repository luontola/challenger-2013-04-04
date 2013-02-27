(ns esko-challenger.proxy
  (:use ring.util.response
        compojure.core)
  (:require [compojure.route :as route]
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
    (.invokeAny executor (map wrap-non-nil commands) 500 TimeUnit/MILLISECONDS)
    (catch Exception e
      nil)))

(defn ask-proxies [question low-port high-port]
  nil) ; TODO

(defn make-routes [low-port high-port]
  (->
    (routes
      (POST "/" {body :body} (ask-proxies (slurp body) low-port high-port))
      (GET "/" [] (str "I'm proxying " low-port "-" high-port))
      (route/not-found "404 Page Not Found"))
    (handler/site)))
