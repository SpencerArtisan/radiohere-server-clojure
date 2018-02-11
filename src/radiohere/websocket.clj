(ns radiohere.websocket
  (:require [clojure.string :as str]
            [radiohere.songkick :as songkick]
            [clojure.core.async :as async]
            [clojure.data.json :as json]
            [io.pedestal.http.jetty.websockets :as ws]))

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

