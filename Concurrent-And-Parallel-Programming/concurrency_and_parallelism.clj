
;;;; ---------------------------------------------------------------------------
;;;; ------------------ Concurrent and Parallel Programming --------------------
;;;; ---------------------------------------------------------------------------

; ------------------------------------------------------------------------------
; Concurrency and Parallelism Concepts

;; Concurrency - managing more than one task at the same time
;; ...we could separate concurrent actions into those that occur by:
;; Interleaving - execution of tasks by switching between tasks
;; Parallelism - execution of more than one task at the same time

;; NOTE: Parallelism is a subclass of concurrency

;; It should be no surprise that Clojure provides plenty of tools to help us
;; achieve parallelism easily. Generally speaking, computers accomplish
;; parallelism by executing tasks on multiple processors.

;; One of the important distinctions to be made when we discuss concurrency is
;; distinguishing parallelism from distribution. Distributed computing
;; is a special type of parallel computing where the processors handling
;; the work are spread across many computers over a network. Clojure does allow
;; you to perform distributed programming via libraries, but here we're only
;; going to be dealing with parallelism on cohabitating processors.
;; For more on distributed programming in Clojure, check out Kyle Kingsbury's
;; 'Call Me Maybe' series at https://aphyr.com/

; ------------------------------------------------------------------------------
; Blocking and Asynchronous Tasks

;; One of the most common use cases for concurrent programming is for blocking
;; operations. Blocking tasks generally include common I/O operations, like
;; reading a file or waiting for HTTP requests to finish.

;; When tasks must be executed in sequence, only starting the next after the
;; completion of the previous, we say these tasks are being executed
;; synchronously.

;; When the execution of a the next task in a sequence of tasks can continue
;; without the completion of a previous task, we say these tasks are being
;; executed asynchronously.

; ------------------------------------------------------------------------------
; Concurrent Programming and Parallel Programming

;; Both concurrent and parallel programming are techniques for decomposing a
;; task into subtasks, we'll use these terms interchangeably because
;; the risks are nearly identical for both. To better understand how
;; concurrency and parallelism work in Clojure, we need to dive into the
;; JVM.

; ==============================================================================
; Clojure Implementation: JVM Threads

;; NOTE: In Clojure, we think of our normal, serial code as a sequence of tasks.
;;       We then indicate that tasks can be performed concurrently by placing
;;       them on JVM threads.

; ------------------------------------------------------------------------------
; What's a Thread?

;; A thread is a subprogram - a program can have many threads, each thread
;; executing a set of instructions while sharing access to the program state.

;; Think of a thread as a piece of physical thread that strings together
;; a sequence of instructions. The processor executes these instructions
;; in order.

;; An example of a single-core processor executing a single-threaded program:

;; thread                    instructions
;; |           ____________________|____________________
;; |           |    |    |    |    |    |    |    |    |
;; |> ------- [ ]--[ ]--[ ]--[ ]--[ ]--[ ]--[ ]--[ ]--[ ]
;;           \   /
;;            \ /
;;             v
;;             |___ processor 'consuming' an instruction

;; A thread can spawn a new thread to execute tasks concurrently.

;; In a single-processor system, the processor switches back and forth between
;; the threads using interleaving. Although the processor executes the
;; instructions on each thread in order, it makes no guarantee about when it
;; will switch back and forth between threads.

;; In a multi-core system, a thread can be assigned to each core:

;; thread
;; |
;; |
;; |                  - [B1] - [B2] - [B3] --- <
;; |                      \      \      \
;; |                  __ [B1] _ [B2] _ [B3] __
;; |                 |
;; |> ______ [A1] ___|__ [A2] _ [A3] _ [A4] __
;;            |           /      /      /
;;         - [A1] ----- [A2] - [A3] - [A4] - <

;; As with interleaving on a single-core, there is still no guarantee for the
;; overall execution order, so the program is nondeterministic.

; ------------------------------------------------------------------------------
; The Three Goblins: Reference Cells, Mutual Exclusion, and Deadlock

;; 1) The reference cell problem is when two threads can read and write to the
;;    same location, and the value at the location depends on the order of the
;;    reads and writes.

;; 2) The mutual exclusion problem describes the negative impact that can occur
;;    when there is no way for a thread to have exclusive write access to the
;;    file, ending up with potentionally garbled write instructions.

;; 3) The deadlock problem occurs when a process or thread enters a waiting
;;    state because a requested system resource is held by another waiting
;;    process, which in turn is waiting for another resource held by another
;;    waiting process.

;; These goblins can be tamed, however, using the tools at our disposal....

; ------------------------------------------------------------------------------
; Futures, Delays, and Promises

;; These lightweight tools for concurrent programming help us manage the
;; challenges of concurrency.

; ----------------
; Futures

;; In Clojure, you can use futures to define a task and place it on another
;; thread without requiring the result immediately - we can create a future
;; with the `future` macro.

(future (Thread/sleep 4000)
        (println "I'll print after 4 seconds"))
(println "I'll print immediately")

;; Normally, `Thread/sleep` is a blocking action, however, `future` creates
;; a new thread, passing expressions to it, allowing the other threads to
;; continue, unblocked.

;; `future` returns a reference value that you can use to request the result.
;; To do this, we dereference the future, using either the `deref` function
;; or the `@` reader macro.

(let [result (future (println "this prints once")
                     (+ 1 1))]
  (println "deref: " (deref result))
  (println "@: " @result))
; => "this prints once"
; => deref: 2
; => @: 2

;; We can also pass `deref` a number of milliseconds to wait along with the
;; value to return if the `deref` times out:

(deref (future (Thread/sleep 1000) 0) 10 5)
; => 5

;; Finally, we can investigate a future using `realized?` to see if it's done:

(realized? (future (Thread/sleep 1000)))
; => false

(let [f (future)]
  @f
  (realized? f))
; => true

;; In short, `future` gives us a basic, simple way to add some concurrency to
;; our programs, and on their own they can help make our programs more flexible
;; and efficient.
;; When we dereference a future, we are indicating that the result is required
;; right now(!) and that evaluation should stop until the result is obtained.

;; We'll see this allows us to work with the mutual exclusion problem.

;; A possible use case for `future` is writing to a log file asynchronously.

; ----------------
; Delays

;; Delays allow us to define a task without needing to execute it or require
;; the result immediately. It's easy to create a `delay`:

(def jackson-5-delay
  (delay (let [message "Just call my name and I'll be there"]
           (println "First deref:" message)
           message)))

;; NOTE: Nothing is printed...we haven't asked the `let` form to be evaluated.

(force jackson-5-delay)
; => First deref: Just call my name and I'll be there
; => "Just call my name and I'll be there"

@jackson-5-delay
; => "Just call my name and I'll be there"

;; NOTE: Dereferencing will return the message without printing executing
;;       the println expression.

;; One way we might utilize a delay is to fire off a statement the first time
;; one future out of a group of related futures finishes.

(def gimli-headshots ["serious.jpg" "fun.jpg" "playful.jpg"])

(defn email-user
  [email-address]
  (println "Sending headshot notification to" email-address))

(defn upload-document
  "Needs to be implemented"
  [headshot]
  true)

(let [notify (delay (email-user "and-my-axe@gmail.com"))]
  (doseq [headshot gimli-headshots]
    (future (upload-document headshot)
            (force notify))))

;; Here, the delay is evaluated the first time one of the futures created by
;; the `doseq` form evaluates `(force notify)`. Even though `(force notify)`
;; will be evaluated three times, the delay body is executed only once.

;; As one can see, this type of interplay between `delay` and `future` helps
;; us avoid mutual exclusion issues - helping to ensure that only one thread can
;; access a particular resource at a time.
;; In the above example, we're saving the email server resource.

; ----------------
; Promises

;; Promises allow us to express that we expect a result without having to
;; define the task that will produce it or when the task should even run.

;; We create promises using the `promise` keyword, and deliver a result to them
;; using the `deliver` keyword. As with `future`, we can obtain a result by
;; dereferencing.

(def my-promise (promise))
(deliver my-promise (+ 1 2))
@my-promise
; => 3

(def yak-butter-international
  {:store "Yak Butter International"
   :price 90
   :smoothness 90})
(def butter-than-nothing
  {:store "Butter Than Nothing"
   :price 150
   :smoothness 83})
;; This is the one that meets our reqs...
(def baby-got-yak
  {:store "Baby Got Yak"
   :price 94
   :smoothness 99})

(defn mock-api-call
  [result]
  (Thread/sleep 1000)
  result)

(defn satisfactory?
  "If the butter meets the reqs, return the butter, else return false"
  [butter]
  (and (<= (:price butter) 100)
       (>= (:smoothness butter) 97)
       butter))

(time (some (comp satisfactory? mock-api-call)
            [yak-butter-international butter-than-nothing baby-got-yak]))
; => "Elapsed time: 3012.023453 msecs"
; => {:store "Baby Got Yak", :smoothness 99, :price 94}

;; To improve the performance of the operation, we can incorporate futures:

(time
 (let [butter-promise (promise)]
   (doseq [butter [yak-butter-international butter-than-nothing baby-got-yak]]
     (future (if-let [satisfactory-butter (satisfactory? (mock-api-call butter))]
               (deliver butter-promise satisfactory-butter))))
   (println "And the winner is:" @butter-promise)))
; => And the winner is: {:store Baby GOt Yak, :price 94, :smoothness 99}
; => "Elapsed time: 1006.651729 msecs"

;; This type of pattern helps us avoid the reference cell problem, because
;; promises can be written to only once, we maintain consistent state amid
;; the nondeterministic reads and writes.

;; One of the great stengths of promises are that they provide a way to register
;; callbacks, achieving similar functionality to what are used to in JavaScript.

(let [ferengi-wisdom-promise (promise)]
  (future (println "Here's some Ferengi wisdom:" @ferengi-wisdom-promise))
  (Thread/sleep 100)
  (deliver ferengi-wisdom-promise "Whisper your way to success."))
; => Here's some Ferengi wisdom: Whisper your way to success.

;; This example creates a future that begins executing immediately. However, the
;; future's thread is blocking because it's waiting for the deref'd value
;; of `ferengi-wisdom-promise`. After 100 milliseconds, you deliver the value
;; and the `println` statement in the future runs.

; ------------------------------------------------------------------------------
; Rolling Your Own Queue

;; [        ]
;;     |  ... split task into serial portion and concurrent portion
;;     v
;; [  |     ]

;; ...so instead of running task serially

;; [ ]
;; [ ]
;; [ ]
;; [ | | ] ...we've reduced the overall time for all tasks complete

;; In creating our queue, we'll be using Thread/sleep quite a bit...
;; so, let's use a macro to abstract this action concisely:

(defmacro wait
  "Sleep `timeout` seconds before evaluating body"
  [timeout & body]
  `(do (Thread/sleep ~timeout) ~@body))

(let [saying3 (promise)]
  (future (deliver saying3 (wait 100 "Cheerio!")))
  @(let [saying2 (promise)]
     (future (deliver saying2 (wait 400 "Pip pip!")))
     @(let [saying1 (promise)]
        (future (deliver saying1 (wait 200 "'Ello, gov'na!")))
        (println @saying1)
        saying1)
     (println @saying2)
     saying2)
  (println @saying3)
  saying3)

;; Now, obviously this would be a terrible way to write our code!
;; Instead, we want our queue function to operate something like:

(->
 (enqueue saying (wait 200 "'Ello, gov'na!") (println @saying))
 (enqueue saying (wait 400 "Pip pip!") (println @saying))
 (enqueue saying (wait 100 "Cheerio!") (println @saying)))

(defmacro enqueue
  ([q concurrent-promise-name concurrent serialized]
   `(let [~concurrent-promise-name (promise)]
      (future (deliver ~concurrent-promise-name ~concurrent))
      (deref ~q)
      ~serialized
      ~concurrent-promise-name))
  ([concurrent-promise-name concurrent serialized]
   `(enqueue (future) ~concurrent-promise-name ~concurrent ~serialized)))

;; 1) The macro `enqueue` is a two arity function, in order to supply a
;;    default value
;; 2) In the first arity, there is a parameter `q` called from the second
;;    arity with value `(future)`
;; 3) Within the first arity let, the macro returns a form that creates
;;    a promise, delivers its value in a future, dereferences whatever form
;;    is supplied for `q`, evaluates the serialized code, and returns the
;;    promise.
;; 4) `q` will usually be a nested `let` expression return by another call
;;    to `enqueue` - if no value is supplied for `q`, the macro supplies
;;    a future so that the `deref` doesn't cause an exception

(time @(-> (enqueue saying (wait 200 "'Ello, gov'na!") (println @saying))
           (enqueue saying (wait 400 "Pip pip!") (println @saying))
           (enqueue saying (wait 100 "Cheerio!") (println @saying))))
; => 'Ello, gov'na!
; => Pip pip!
; => Cheerio!
; => "Elapsed time: 401.635 msecs"
