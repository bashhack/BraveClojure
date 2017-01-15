(ns playsync.core
  (:require [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout]]))

(defn -main [& args]
  (def echo-chan (chan))
  (go (println (<! echo-chan)))
  (>!! echo-chan "ketchup"))
;; => true
;; => ketchup

;; Here, we created a channel called `echo-chan` using the `chan` function.
;; Channels communicate "messages," and you can "put" on and "take" messages
;; off a channel. Processes "wait" for the completion of put and take --
;; these are the events that processes respond to.

;; There are two basic rules when it comes to channels: (1) when putting or
;; taking a message on a channel - do nothing until the put or take is
;; successful, and (2) when the put or take succeeds, continue executing.

;; On the second line of the block, we use `go` to create a new process.
;; Everything within the `go` block runs on a separate thread. Go blocks run
;; your processes on a thread pool that contains threads equal to two plus
;; the number of cores on your machine, resulting in better performance as
;; you avoid the penalty of creating threads for each new process.

;; The next line of code, `(println (<! echo-chan))`, is saying "when I
;; take a message from `echo-chan`, print it." The `<!` in the expression
;; is the "take" function. It "listens" to the channel you give it as an
;; argument, and the process it belongs to waits until another process
;; puts a message on the channel. When `<!` retrieves a value, the value
;; is returned and the `println` expression is executed.

;; The expression `(>!! echo-chan "ketchup")` "puts" the string "ketchup" on
;; the channel `echo-chan` and returns `true`. Putting a message on the channel
;; is a blocking action until another process takes the message.

;; We can run into trouble with channels (creating an indefinite block):
;; `(>!! (chan) "mustard")`
;; Here, we've created a new channel, put something on it, but there's no
;; process listening (take/taking == "listen") to that channel. Processes
;; wait to receive messages, but they also wait for messages they put on
;; a channel to be taken.
