(ns radiohere.songkick
  (:require [clj-http.client]
            [radiohere.soundcloud :as soundcloud]
            [clojure.data.json :as json]
))

(def geo-url "https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=AIzaSyBHWZu9leG19S5HNPn37dPzqxOtaEW9OeU")
(def songkick-api-key "hZ33FKGXTbn0VeVh")
(def metro-area-url (str "http://api.songkick.com/api/3.0/search/locations.json?location=geo:%s,%s&apikey=" songkick-api-key))
(def gig-url (str "http://api.songkick.com/api/3.0/metro_areas/%s/calendar.json?page=0&apikey=" songkick-api-key))
(def earth-radius 3958.75)
(def meter-conversion 1609)

(defn distinct-by [f coll]
  (let [groups (group-by f coll)]
    (map #(first (groups %)) (distinct (map f coll)))))

(defn kmBetween [lat1 lon1 lat2 lon2]
  (if (some nil? [lat1 lon1 lat2 lon2])
    999
  (let [dLat (Math/toRadians (- lat2 lat1))
        dLon (Math/toRadians (- lon2 lon1))
        a (+ (* (Math/sin (/ dLat 2)) 
                (Math/sin (/ dLat 2)))
             (* (Math/cos (Math/toRadians lat1)) 
                (Math/cos (Math/toRadians lat2)) 
                (Math/sin (/ dLon 2))
                (Math/sin (/ dLon 2))))
        c (* 2 (Math/atan2 (Math/sqrt a) (Math/sqrt (- 1 a))))
        dist (* earth-radius c)]
    (/ (* dist meter-conversion) 1000.0))))
(kmBetween 51.554004 -0.0907729999 51.5404778 -0.088469399999)
(kmBetween 1 2 3 nil)

(defn get-json
  [url]
  (:body (clj-http.client/get url {:as :json})))
(get-json (format geo-url "n52QT"))
(get-json (format metro-area-url 40.69 -73.99))
(get-json (format gig-url "24426"))

(defn find-lat-long [address]
  (let [url (format geo-url address)
        body (get-json url)]
    (get-in body [:results 0 :geometry :location])))
(find-lat-long "tower isle, jamaica")
(find-lat-long "ZZZZZZZZZZZZZZZZZZZZ")

(defn extract-area [location]
  (let [ area (get-in location [:metroArea :displayName])
         id (get-in location [:metroArea :id])
         lat (get-in location [:metroArea :lat])
         lon (get-in location [:metroArea :lng])
         result {:area area :id id :lat lat :lon lon}]
;    (println result)
    (identity result)))

(defn find-metro-area [lat lon kmAway]
  (let [body (get-json (format metro-area-url lat lon))
        locations (get-in body [:resultsPage :results :location])]
    (filter #(< (:kmAway %) kmAway)
      (map #(assoc % :kmAway (kmBetween (:lat %) (:lon %) lat lon)) 
           (distinct-by :id (map extract-area locations))))))
;(find-metro-area 40.6949815 -73.9982914 100)

(defn find-metro-area-from-address [address kmAway]
  (let [latlon (find-lat-long address)]
    (find-metro-area (:lat latlon) (:lng latlon) kmAway)))
;(find-metro-area-from-address "London" 20)

(defn extract-gig [gig]
  (let [venueName (get-in gig [:venue :displayName])
        lat (get-in gig [:venue :lat])
        lon (get-in gig [:venue :lng])
        date "2018-02-24"
        artist (get-in gig [:performance 0 :displayName])]
    {:venueName venueName
     :date date
     :artist artist
     :lat lat
     :lon lon
     :tracks []
     }))

(defn find-gigs-by-area [metro-area]
  (let [url (format gig-url metro-area)
        body (get-json url)
        gigs (get-in body [:resultsPage :results :event])
        mapped-gigs (map extract-gig gigs)]
    (println "finding gigs in area" metro-area " found " (count mapped-gigs))
    (identity mapped-gigs)))
;(find-gigs-by-area 24426)

(defn find-gigs-by-address [address kmAway callback]
  (println "Finding gigs within " kmAway " of " address)
  (let [{lat :lat lon :lng} (find-lat-long address)
        metro-areas (find-metro-area-from-address address 30)
        gigs (mapcat #(find-gigs-by-area (:id %)) metro-areas)
        gigs-with-distance (map #(assoc % :distance (kmBetween (:lat %) (:lon %) lat lon)) gigs)
        close-gigs (filter #(< (:distance %) kmAway) gigs-with-distance)
        gigs-with-tracks (map #(assoc % :tracks (soundcloud/find-tracks (:artist %))) close-gigs)]
        (doall (map callback gigs-with-tracks))))
;(find-gigs-by-address "N5 2QT" 3 #(println %))

(defn find-gigs-by-keyword [keyword callback]
  (println "Finding songs using keyword " keyword)
  (let [gig {:artist keyword :tracks (soundcloud/find-tracks keyword)}]
    (callback gig)))
;(find-gigs-by-keyword "Malkmus" println)

