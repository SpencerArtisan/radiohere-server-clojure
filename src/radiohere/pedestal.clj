(ns radiohere.pedestal
  (:require [io.pedestal.http :as http]
            [clojure.string :as str]
            [radiohere.songkick :as songkick]
            [clojure.core.async :as async]
            [clojure.data.json :as json]
            [io.pedestal.http.jetty.websockets :as ws]
            [com.stuartsierra.component :as component]))

(defn test?
  [service-map]
  (= :test (:env service-map)))

(defn send-gigs 
  [ws-ch message]
  (println "Websocket invoked with message " message)
  (let [args (str/split message #";")]
    (if (= 1 (count args))
      (songkick/find-gigs-by-keyword (get args 0) #(async/put! @ws-ch (json/write-str %)))
      (songkick/find-gigs-by-address (get args 0) (read-string (get args 1)) #(async/put! @ws-ch (json/write-str %))))))

(defn new-ws-client
  [ws-channel-atom ws-session send-ch]
  (println "New WS client connected")
  (swap! ws-channel-atom (fn [prev] (identity send-ch))))

(defn ws-paths
  [ws-channel-atom]
  {"/ws" {:on-connect (ws/start-ws-connection (partial new-ws-client ws-channel-atom))
          :on-text (fn [msg] (send-gigs ws-channel-atom msg))
          :on-error (fn [t] (println :msg "WS Error happened" :exception t))
          :on-close (fn [num-code reason-text] (println :msg "WS Closed:" :reason reason-text))}})

(defrecord Pedestal [service-map
                     service]
  component/Lifecycle
   (start [this]
    (if service
      this
      (let [ws-atom (atom nil)
            with-ws-config (assoc service-map 
                                  ::http/container-options 
                                  {:context-configurator #(ws/add-ws-endpoints % (ws-paths ws-atom))})]
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


