; Copyright 2013 Relevance, Inc.
; Copyright 2014-2016 Cognitect, Inc.

; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0)
; which can be found in the file epl-v10.html at the root of this distribution.
;
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
;
; You must not remove this notice, or any other, from this software.

(defproject radiohere "0.5.1"
  :description "Sample of web sockets with Jetty"
  :url "http://pedestal.io/samples/index"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [io.pedestal/pedestal.service "0.5.1"]
                 [org.clojure/core.async "0.2.391"]

                 ;; Remove this line and uncomment one of the next lines to
                 ;; use Immutant or Tomcat instead of Jetty:
                 [io.pedestal/pedestal.jetty "0.5.1"]
                 [org.clojure/data.json "0.2.6"]
                 ;; [io.pedestal/pedestal.immutant "0.5.1"]
                 ;; [io.pedestal/pedestal.tomcat "0.5.1"]

                 [clj-http "3.4.1"]
                 [environ "1.0.0"]

                 [ch.qos.logback/logback-classic "1.1.7" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "1.7.21"]
                 [org.slf4j/jcl-over-slf4j "1.7.21"]
                 [org.slf4j/log4j-over-slf4j "1.7.21"]]
  :min-lein-version "2.0.0"
  :resource-paths ["config", "resources"]
  :pedantic? :abort
  :profiles {:dev {:aliases {"run-dev" ["trampoline" "run" "-m" "radiohere.server/run-dev"]}}
             :uberjar {:aot [radiohere.server]}}
  :main ^{:skip-aot true} radiohere.server)
