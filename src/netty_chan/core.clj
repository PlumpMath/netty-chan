(ns netty-chan.core
  (:require [clojure.core.async :as async :refer [chan go go-loop >! <!]])
  (:import (io.netty.channel ChannelHandlerContext
                             ChannelInboundHandlerAdapter
                             ChannelFuture
                             ChannelInitializer
                             ChannelOption
                             EventLoopGroup)
           (io.netty.channel.nio NioEventLoopGroup)
           (io.netty.channel.socket.nio NioServerSocketChannel)
           (io.netty.bootstrap ServerBootstrap)
           (io.netty.util ReferenceCountUtil)))

(defn- wrap-handler
  "Wrap a handler function and return a `ChannelInboundHandlerAdapter`"
  [f]
  (let [ch (chan)]
    (println "Creating handler")
    (proxy [ChannelInboundHandlerAdapter] []
      (channelActive [ctx]
        (println "Channel initialized")
        (f ch ctx)
        (go-loop []
          (let [msg (<! ch)]
            (println "Writing message")
            (.writeAndFlush ctx msg)
            (recur))))
      (channelRead [ctx msg]
        (try
          (println "Reading message")
          (go (>! ch msg))))
      (exceptionCaught [ctx cause]
        (.printStackTrace cause)
        (.close ctx)))))


(defn tcp-server
  "Start a TCP server with given handler"
  [port handler & {:keys [encoder decoder backlog]
                   :or {backlog 128}}]
  (let [boss-group (NioEventLoopGroup.)
        worker-group (NioEventLoopGroup.)
        channel-initialzer (proxy [ChannelInitializer] []
                             (initChannel [netty-chan]
                               (println "Initializing channel")
                               (-> netty-chan
                                   .pipeline
                                   (.addLast "netty-chann-handler"
                                             (wrap-handler handler)))))]
    (try
      (let [b (doto (ServerBootstrap.)
                (.group boss-group worker-group)
                (.channel NioServerSocketChannel)
                (.childHandler channel-initialzer)
                (.option ChannelOption/SO_BACKLOG (int backlog))
                (.childOption ChannelOption/SO_KEEPALIVE true))]
        (println "Server bootstrap created.")
        (let [future (-> b (.bind (int port)) .sync)]
          (println "Server started.")
          (-> future .channel .closeFuture .sync)))
      (finally
        (println "Shutting down...")
        (.shutdownGracefully worker-group)
        (.shutdownGracefully boss-group)))))
