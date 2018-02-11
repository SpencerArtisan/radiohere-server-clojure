(ns radiohere.soundcloud
  (:require [clj-http.client]))

(def soundcloud-api-key "ab2cd50270f2b1097c169d43f06a3d17")
(def soundcloud-url (str "http://api.soundcloud.com/tracks.json?q=%s&client_id=" soundcloud-api-key "&limit=80"))

(defn get-json
  [url]
  (:body (clj-http.client/get url {:as :json})))
(comment (get-json (format soundcloud-url "malkmus")))

(defn extract-track [track]
    {:title (:title track)
     :streamUrl (str (:stream_url track) "?client_id=" soundcloud-api-key)})

(defn find-tracks [keyword]
  (let [url (format soundcloud-url keyword)
        tracks (get-json url)
        mapped-tracks (map extract-track tracks)]
    (println "finding tracks from keyword " keyword  " found " (count mapped-tracks))
    (identity mapped-tracks)))
;(find-tracks "Malkmus")

