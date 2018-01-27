(ns jetty-web-sockets.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.log :as log]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.route.definition :refer [defroutes]]
            [ring.util.response :as ring-resp]
            [clojure.core.async :as async]
            [clojure.data.json :as json]
            [io.pedestal.http.jetty.websockets :as ws])
  (:import [org.eclipse.jetty.websocket.api Session]))

(defn home-page
  [request]
  (ring-resp/response "Hello New World!"))

(defroutes routes
  [[["/" {:get home-page}
     ^:interceptors [(body-params/body-params) http/html-body]]]])

(def ws-clients (atom {}))

(def test-gig {
               "venueName" "Garage"
               "date" "2018-02-24"
               "distance" 0.42
               "artist" "Pavement"
               "tracks" [ {"name" "Zurich is Stained" "streamUrl" "https://api.soundcloud.com/tracks/63481939/stream?client_id=ab2cd50270f2b1097c169d43f06a3d17"}]
})

(defn send-gigs [send-ch]
  (async/put! send-ch (json/write-str test-gig)))

(defn new-ws-client
  [ws-session send-ch]
  (send-gigs send-ch)
  (swap! ws-clients assoc ws-session send-ch))

(def ws-paths
  {"/ws" {:on-connect (ws/start-ws-connection new-ws-client)
          :on-text (fn [msg] (log/info :msg (str "A client sent - " msg)))
          :on-binary (fn [payload offset length] (log/info :msg "Binary Message!" :bytes payload))
          :on-error (fn [t] (log/error :msg "WS Error happened" :exception t))
          :on-close (fn [num-code reason-text] (log/info :msg "WS Closed:" :reason reason-text))}})

(def service {:env :prod
              ::http/routes routes
              ::http/resource-path "/public"
              ::http/type :jetty
              ::http/container-options {:context-configurator #(ws/add-ws-endpoints % ws-paths)}
              ::http/port 8080})

