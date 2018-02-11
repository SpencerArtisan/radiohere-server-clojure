(ns radiohere.soundcloud-test
  (:require [clojure.test :refer [deftest is]]
            [radiohere.soundcloud :as sc]))


(deftest test-extract-track
  (is (= (sc/extract-track {:title "a title" :stream_url "a url"})
         {:title "a title" :streamUrl (str "a url?client_id=" sc/soundcloud-api-key)})))


