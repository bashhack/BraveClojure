
;;;; ---------------------------------------------------------------------------
;;;; --------------- Concurrent Processes With `core.async` --------------------
;;;; ---------------------------------------------------------------------------

;; Clojure's `core.async` library allows us to create multiple independent
;; processes within a single program. We'll cover this style of programming
;; in-depth and learn how to actually write code that takes advantage of
;; its benefits.

;; We'll learn how:

;; 1) channels (like Rob Pike's Go language) communicate between independent
;; processes created by go blocks and `thread`,
;; 2) how Clojure manages threads with parking and blocking,
;; 3) how to use `alts!!`
;; 4) how to create a more straightforward method of writing queues
;; 5) how to avoid callback hell with proces pipelines

; ------------------------------------------------------------------------------
; Gettings Started with Processes

;; The `core.async` library is built around the idea of processes, a concurrent
;; unit of logic that responds to events. Processes are analogous to our
;; understanding of the world: entities interact with and respond to each other
;; independently without a central control mechanism behind the scenes.

;; This notion is a little different from our view of concurrency as we've seen
;; it so far, where we defined tasks as extensions of the main thread of control
;; (`pmap`) and where we created tasks that have no interest in communicating
;; (`future`).

;; When we think of the process itself, we should be thinking:
;; Can I define a given thing's essence as the set of the events it
;; recognizes and how it responds?

;; NOTE: My core.async project is in the directory titled `playsync`

; ------------------------------------------------------------------------------
; Buffering

;; Our example used in the `playsync` project contained two processes - one we
;; created with `go` and the REPL process. These processes don't have explicit
;; knowledge of each other, and they act independently.

;; Managing our processes becomes easier with buffers, we can specify the type
;; of buffer, invoking a simple buffer, `sliding-buffer`, or `dropping-buffer`.

;; A Simple Buffer
(def echo-buffer (chan 2))
(>!! echo-buffer "ketchup")
; => true
(>!! echo-buffer "ketchup")
; => true
(>!! echo-buffer "ketchup")
; => This blocks because the channel buffer is full

;; A `sliding-buffer` will drop values in a first-in, first-out fashion

;; A `dropping-buffer` will discard values in a last-in, first-out fashion

;; Neither of these buffers will ever cause `>!!` to block.

; ------------------------------------------------------------------------------
; Blocking and Parking

; -----------------------------------------------
;      | Inside `go` block  | Outside `go` block
; -----------------------------------------------
; put  |     >! or >!!      |        >!!
; -----------------------------------------------
; take |     <! or <!!      |        <!!
; -----------------------------------------------

;; Because `go` blocks use a thrad pool with a fixed sie, you can create 1K
;; `go` processes but use only a handful of threads:

(def hi-chan (chan))
(doseq [n (range 1000)]
  (go (>! hi-chan (str "hi " n))))

;; How is this possible? Because of how processes "wait" - "put" waits until
;; another process does a "take" on the same channel and vice versa - in the
;; example, 1K processes are waiting for another process to take from `hi-chan`.

;; When we talk about "waiting" we have to types: "parking" and "blocking"

;; We are familiar with blocking as this is when a thread stops execution until
;; a task is complete. We think of blocking being related to some kind of
;; I/O operation. The thread is alive, but does no work, so you have to create
;; a new thread to continue working - we learned how to accomplish this with
;; `future` in Chapter 9.

;; Parking frees up the thread so it can keep doing work. So, let's imagine you
;; have one thread and two processes, Process A and Process B. Process A runs
;; on the thread and then waits for a put or take. Clojure moves A off the thread
;; and moves B onto the thread. If B starts waiting and A's put or take has
;; finished, Clojure moves B off and puts A back on.

;; In this way, parking allows instructions from multiple processes to
;; interleave on a single thread, similar to the way that using multiple threads
;; allows interleaving on a single core.

;; NOTE: While the implementation of parking isn't key, it is ONLY possible
;;       within `go` blocks, and it's only possible when you use `>!` and `<!`,
;;       which we call "parking put" and "parking take". `>!!` and `<!!` are
;;       "blocking put" and "blocking take".

; ------------------------------------------------------------------------------
; `thread`

;; When we do need to use blocking instead of parking, perhaps when a process
;; will take a long time before putting or taking, we should use `thread`:

(thread (println (<!! echo-chan)))
(>!! echo-chan "mustard")
; => true
; => "mustard"

;; `thread` acts almost exactly like `future`: it creates a new thread and
;; executes a process on that thread. However, unlike `future`, which returns
;; an object that you can deref, `thread` returns a channel. When `thread`'s
;; process stops, the process return values is put on the channel that `thread`
;; returns:

(let [t (thread "chili")]
  (<!! t))
; => "chili"

;; You should use `thread` instead of a go block when performing a long-running
;; task so you don't clog your thread pool.

; ------------------------------------------------------------------------------
; Hot Dog Machine

;; NOTE: Code for the project is in the directory title `hotdogmachine`

; ------------------------------------------------------------------------------
; `alts!!`

;; The function `alts!!` lets us use the result of the first successful channel
;; operation among a collection of operations.

;; Let's recreate our delays and futures headshot program using `alts!!`:

(defn upload
  [headshot c]
  (go (Thread/sleep (rand 100))
      (>! c heashot)))

(let [c1 (chan)
      c2 (chan)
      c3 (chan)]
  (upload "pic1.jpg" c1)
  (upload "pic2.jpg" c2)
  (upload "pic3.jpg" c3)
  (let [[headshot channel] (alts!! [c1 c2 c3])]
    (println "Sending headshot notification for" headshot)))

;; Here, the `alts!!` function acts like: "Try to do a blocking take on each of
;; the channels in the vector simultaneously. As soon as a take succeeds, return
;; a vector whose first element is the value taken and whose second element is
;; the winning channel.

;; We can also provide `alts!!` with a timeout channel, which waits the
;; specified number of ms and then closes. It's a succinct way of putting a time
;; limit on concurrent operations:

(let [c1 (chan)]
  (upload "pic1.jpg" c1)
  (let [[headshot channel] (alts!! [c1 (timeout 20)])]
    (if headshot
      (println "Sending headshot notification for" headshot)
      (println "Timed out!"))))
; => Timed out!

;; We can also use `alts!!` to specify put operations:

(let [c1 (chan)
      c2 (chan)]
  (go (<! c2))
  (let [[value channel] (alts!! [c1 [c2 "put!"]])]
    (println value)
    (= channel c2)))
; => true
; => true

;; NOTE: Just like take and put, there is a variant for placement
;;       in `go` blocks, `alts!`

; ------------------------------------------------------------------------------
; Queues

;; Just as we wrote our macro to queue futures, we could use processes to do
;; this in a more straightforward manner.

;; Here, we'll grab a collection of random quotes from a website and write them
;; to a single file. We want to ensure that only one quote is written at a time,
;; this preventing our text from being interleaved. To accomplish this, we need
;; a queue:

;; NOTE: `spit` (spit f content & options) - the opposite of `slurp`, opens f
;;       with writer, writes content, then closes f

(defn append-to-file
  "Write a string to the end of a file"
  [filename s]
  (spit filename s :append true))

(defn format-quote
  "Delineate the beginning and end of a quote because it's convenient"
  [quote]
  (str "=== BEGIN QUOTE ===\n" quote "=== END QUOTE ===\n\n"))

(defn random-quote
  "Retrieve a random quote and format it"
  []
  (format-quote (slurp "http://www.braveclojure.com/random-quote")))

(defn snag-quotes
  [filename num-quotes]
  (let [c (chan)]
    (go (while true (append-to-file filename (<! c))))
    (dotimes [n num-quotes] (go (>! c (random-quote))))))

(snag-quotes "quotes" 2)

; ------------------------------------------------------------------------------
; Pipelines

;; In a language without channels, we need to still express "when x happens,
;; do y" but we must do so using `callbacks`.

;; It's all to easy to create dependencies among all the layers of callbacks
;; that aren't exactly easy to detect - this we, of course, call "callback hell".
;; They also end up sharing state, making it difficult to reason about the
;; overall state of the system as the callbacks get triggered.

;; We can manage this by creating a process pipeline, where each unit of logic
;; is in an isolated process, and all communication between these units
;; occurs through explicit input and output channels.

;; In the following, let's create three infinitely looping processes connected
;; through channels, passing the out of one process to the in of the next:

(defn upper-caser
  [in]
  (let [out (chan)]
    (go (while true (>! out (clojure.string/upper-case (<! in)))))
    out))

(defn reverser
  [in]
  (let [out (chan)]
    (go (while true (>! out (clojure.string/reverse (<! in)))))
    out))

(defn printer
  [in]
  (go (while true (println (<! in)))))

(def in-chan (chan))
(def upper-caser-out (upper-caser in-chan))
(def reverser-out (reverser upper-caser-out))
(printer reverser-out)

(>!! in-chan "redrum")
; => MURDER

(>!! in-chan "repaid")
; => DIAPER

; -----------------------------------------------------------------------------
; Additional Resources
; -----------------------------------------------------------------------------

;; Clojure's core.async library is based on Go's concurrency model, which is
;; based on work by Tony Hoare in "Communicating Sequential Processes" and is
;; available at:
;; http://www.usingcsp.com/

;; Rob Pike, co-creater of Go, has a great talk about concurrency:
;; https://www.youtube.com/watch?v=f6kdp27TYZs
