# radiohere

A music discovery app.

## Overview

The back-end is clojure websockets using Pedestal.  It provides information on gigs from songkick and sample songs from soundcloud.

## Setup

To get an interactive development environment run:

    lein repl

To run server

    (require 'radiohere.system)
    (reloaded.repl/go)

To check it is working

    localhost:8080

To apply code changes

    (reloaded.repl/reset)

