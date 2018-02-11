(ns radiohere.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [ring.util.response :as ring-resp]
            [clojure.core.async :as async]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [radiohere.songkick :as songkick]
            [environ.core :refer [env]]
            [io.pedestal.http.jetty.websockets :as ws])
  (:import [org.eclipse.jetty.websocket.api Session]))

(comment

(defn home-page
  [request]
  (ring-resp/response "Radiohere"))

(defroutes routes
  [[["/" {:get home-page}
     ^:interceptors [(body-params/body-params) http/html-body]]]])

(def ws-ch (atom nil))

(defn send-gigs [message]
  (println "Websocket invoked with message " message)
  (let [args (str/split message #";")]
    (if (= 1 (count args))
      (songkick/find-gigs-by-keyword (get args 0) #(async/put! @ws-ch (json/write-str %)))
      (songkick/find-gigs-by-address (get args 0) (read-string (get args 1)) #(async/put! @ws-ch (json/write-str %))))))


(defn new-ws-client
  [ws-session send-ch]
  (println "New WS client connected")
  (swap! ws-ch (fn [prev] (identity send-ch))))

(def ws-paths
  {"/ws" {:on-connect (ws/start-ws-connection new-ws-client)
          :on-text (fn [msg] (send-gigs msg))
          :on-error (fn [t] (println :msg "WS Error happened" :exception t))
          :on-close (fn [num-code reason-text] (println :msg "WS Closed:" :reason reason-text))}})

(def service {:env :prod
              ::http/routes routes
              ::http/resource-path "/public"
              ::http/type :jetty
              ::http/container-options {:context-configurator #(ws/add-ws-endpoints % ws-paths)}
              ::http/port (Integer. (or (env :port) 8080))})

)
