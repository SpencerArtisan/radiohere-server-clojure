(ns radiohere.routes)

(defn respond-home [request]
  {:status 200 :body "Radiohere"})

(def routes
  #{["/" :get respond-home :route-name :home]})

