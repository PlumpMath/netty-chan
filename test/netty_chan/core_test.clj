(ns netty-chan.core-test
  (:require [clojure.test :refer :all]
            [netty-chan.core :refer :all]
            [clojure.core.async :refer [go-loop >! <!]]))

(defn echo-handler
  [ctx ch]
  (go-loop [msg (<! ch)]
    (>! ch msg)
    (recur (<! ch))))

(defn start-server
  []
  (tcp-server 9998 echo-handler))
