(ns esko-challenger.main
  (:use [clojure.tools.cli :only [cli]]
        ring.adapter.jetty
        ring.middleware.file
        ring.middleware.reload
        ring.middleware.stacktrace)
  (:require [esko-challenger.proxy :as proxy])
  (:import [java.io File])
  (:gen-class ))

(defn make-webapp [options]
  (->
    (proxy/make-routes
      (if (:backend-list options)
        (proxy/make-backend-list-monitor (:backend-list options))
        #(proxy/local-backend-urls (:low-port options) (:high-port options))))
    ;(wrap-reload)
    (wrap-stacktrace)))

(defn run [options]
  (run-jetty (make-webapp options) options))

(defn start [options]
  (.start (Thread. (fn [] (run options)))))

(defn -main [& args]
  (let [[options args banner] (cli args
    ["--port" "Port for this HTTP proxy to listen to" :default 8080 :parse-fn #(Integer. %)]
    ["--backend-list" "File which lists the backend URLs, one per line" :parse-fn #(File. %)]
    ["--low-port" "First backend server port" :default 8090 :parse-fn #(Integer. %)]
    ["--high-port" "Last backend server port" :default 8099 :parse-fn #(Integer. %)]
    ["--help" "Show this help" :flag true])]
    (when (:help options)
      (println banner)
      (System/exit 0))
    (start options)))
