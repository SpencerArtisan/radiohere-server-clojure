(ns radiohere.pedestal
  (:require [io.pedestal.http :as http]
            [clojure.string :as str]
            [radiohere.websocket :as websocket]
            [io.pedestal.http.jetty.websockets :as ws]
            [com.stuartsierra.component :as component]))

(defn test?
  [service-map]
  (= :test (:env service-map)))
(defrecord Pedestal [service-map
                     service]
  component/Lifecycle
   (start [this]
    (if service
      this
      (let [ws-atom (atom nil)
            with-ws-config (assoc service-map 
                                  ::http/container-options 
                                  {:context-configurator #(ws/add-ws-endpoints % (websocket/ws-paths ws-atom))})]
        (cond-> with-ws-config
          true                      http/create-server                  
          (not (test? service-map)) http/start                          
          true                      ((partial assoc this :ws-channel-atom ws-atom :service))))))
   (stop [this]
      (when (and service (not (test? service-map)))
        (http/stop service))
      (assoc this :service nil)))

(defn new-pedestal
  []
  (map->Pedestal {}))


