# netty-chan

core.async network channels powered by Netty.

## Usage

Write network handler with the following signature:

```clojure
(defn handler [ctx ch])
```

The two args are:

- ctx: Netty `ChannelHandlerContext` object
- ch: `core.async` channel object for reading/writing messages. The
  message is a Netty `ByteBuf` object or custom message object decoded
  with given decoder (not supported yet but this feature is planned)

Use `tcp-server` to start a TCP server with given handler:

```clojure
(tcp-server port handler)
```

## Echo Server Example

```clojure
(require '[netty-chan.core :refer :all])
(require '[clojure.core.async :refer [go-loop >! <!]])

(defn echo-handler
  [ctx ch]
  (go-loop [msg (<! ch)]
    (>! ch msg)
    (recur (<! ch))))

(defn start-server
  []
  (tcp-server 9998 echo-handler))
```

Call `start-server` in a REPL and use telnet to test:

```
[jerry:~/dev/personal/netty-chan]$ telnet localhost 9998
Trying ::1...
Connected to jerry-laptop.
Escape character is '^]'.
9800
9800
hello
hello
foobar
foobar
你好
你好
```

## TODO

- Encoder/Decoder support
- UDP support

## License

Copyright © 2014 Jerry Peng

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
