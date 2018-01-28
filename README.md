# radiohere

A music discovery app.

## Overview

The back-end is clojure websockets using Pedestal.  It provides information on gigs from songkick and sample songs from soundcloud.

## Setup

To get an interactive development environment run:

    lein repl

To run server

   (def serv (run-dev))

To check it is working

   localhost:8080

To apply code changes

   (require 'radiohere.service :reload)
   (require 'radiohere.songkick :reload)


