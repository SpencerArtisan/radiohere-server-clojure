(ns radiohere.rest
  (:require [clj-http.client]
            [com.stuartsierra.component :as component]))

(defrecord RestApi [root-url]
  component/Lifecycle
  (start [this]
         (println "Starting Rest Api ")
         this)
  (stop [this]
        (println "Stopping Rest Api ")
        this))

(defn get-noun [rest-api noun]
   (let [url (format (:root-url rest-api) noun)]
     (:body (clj-http.client/get url {:as :json}))))

(def geo-url "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=AIzaSyBHWZu9leG19S5HNPn37dPzqxOtaEW9OeU")
(get-noun (->RestApi geo-url) "N5 2QT")


;(get-json (format geo-url "n52QT"))
