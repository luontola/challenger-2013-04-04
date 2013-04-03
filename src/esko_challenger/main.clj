(ns esko-challenger.main
  (:use [clojure.tools.cli :only [cli]]
        ring.adapter.jetty
        ring.middleware.file
        ring.middleware.reload
        ring.middleware.stacktrace)
  (:require [esko-challenger.cache :as cache]
            [esko-challenger.fetcher :as fetcher])
  (:import [java.io File]
           [java.net URL])
  (:gen-class ))

(defn wrap-if [handler pred wrapper & args]
  (if pred
    (apply wrapper handler args)
    handler))

(defn make-webapp [options]
  (let [answers
        (if (:datomic-uri options)
          (cache/datomic-answers (:datomic-uri options))
          (cache/filesystem-answers (File. (:cache-dir options))))]
    (fetcher/start (URL. (:challenger-url options)) answers)
    (->
      (cache/make-routes answers)
      (wrap-if (:reload options) wrap-reload)
      (wrap-stacktrace))))

(defn run [options]
  (run-jetty (make-webapp options) options))

(defn start [options]
  (.start (Thread. (fn [] (run options)))))

(defn -main [& args]
  (let [[options args banner] (cli args
    ["--challenger-url" "Base URL of the Challenger server (required)"]
    ["--datomic-uri" "Connection URI for Datomic database"]
    ["--cache-dir" "Directory path when using flat files for persistence" :default "answers-cache"]
    ["--port" "Port for the HTTP server to listen to" :default 8080 :parse-fn #(Integer. %)]
    ["--reload" "Reload changes to sources automatically" :flag true]
    ["--help" "Show this help" :flag true])]
    (when (or
            (:help options)
            (nil? (:challenger-url options)))
      (println banner)
      (System/exit 0))
    (start options)))
