(ns esko-challenger.main
  (:use [clojure.tools.cli :only [cli]]
        ring.adapter.jetty
        ring.middleware.file
        ring.middleware.reload
        ring.middleware.stacktrace)
  (:require [esko-challenger.core :as core]
            [esko-challenger.proxy :as proxy])
  (:gen-class ))

(defn make-webapp [options]
  (->
    (if (:proxy options)
      (proxy/make-routes
        (+ 1 (:port options))
        (+ 10 (:port options)))
      (core/make-routes))
    (wrap-reload)
    (wrap-stacktrace)))

(defn run [options]
  (run-jetty (make-webapp options) options))

(defn start [options]
  (.start (Thread. (fn [] (run options)))))

(defn -main [& args]
  (let [[options args banner] (cli args
    ["--port" "Port for the HTTP server to listen to" :default 8080 :parse-fn #(Integer. %)]
    ["--proxy" "Start in proxy mode" :flag true]
    ["--help" "Show this help" :flag true])]
    (when (:help options)
      (println banner)
      (System/exit 0))
    (start options)))
