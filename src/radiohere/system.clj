(ns radiohere.system
  (:require [com.stuartsierra.component :as component]       
            [reloaded.repl :refer[init start stop go reset]] 
            [io.pedestal.http :as http]                      
            [clojure.string :as str]
            [radiohere.pedestal]                                       
            [radiohere.routes])
  (:gen-class))

(defn system
  [env]
  (component/system-map
   :service-map
   {:env          env
    ::http/routes radiohere.routes/routes
    ::http/type   :jetty
    ::http/port (Integer. (or (env :port) 8080))
    ::http/join?  false}
   :pedestal
   (component/using
    (radiohere.pedestal/new-pedestal)
    [:service-map])))


(defn -main
  [& args]
  (println "\nCreating your server...")
  (reloaded.repl/set-init! #(system :prod))
  (reloaded.repl/go))

