
;;;; ---------------------------------------------------------------------------
;;;; -------------- Boot, the Fancy Clojure Build Framework --------------------
;;;; ---------------------------------------------------------------------------

;; Boot is an alternative to Leiningen that provides the same functionality.

;; For the best resources on Boot, check here:
;; https://github.com/boot-clj/boot/
;; https:/github.com/boot-clj/boot/wiki/

; -----------------------------------------------------------------------------
; Boot's Abstractions

;; "Lisped-up lovechild of Git and Unix"

; -----------------------------------------------------------------------------
; Tasks

;; Right off the bat, this reminds me of Gulp/Grunt tasks.... We define tasks
;; using `deftask`:

(deftask taskname
  "Task description"
  [task args]
  (taskbody))

;; To continue:

(deftask fire
  "Announces that something is on fire"
  [t thing THING str "The thing that's on fire"
   p pluralize bool "Whether to pluralize"]
  (let [verb (if pluralize "are" "is")]
    (println "My" thing verb "on fire!")))

;; boot fire -t heart
; => My heart is on fire!

;; boot fire -t logs -p
; => My logs are on fire!

; -----------------------------------------------------------------------------
; The REPL

;; Like Leiningen, Boot comes with a built-in task for the REPL: `boot repl`

; -----------------------------------------------------------------------------
; Composition and Coordination

;; Boot lets us compose tasks, allowing us to succinctly write and perform
;; execution of multiple commands. As an example, here is how we might
;; accomplish a Ruby Rake invocation:

rake db:create d{:tag :a, :attrs {:href "db:seed"}, :content ["b:migra"]}te db:seed

; -----------------------------------------------------------------------------
; Handlers and Middleware

;; Boot avoids tricky mutable and brittle state by treating tasks as "middleware
;; factories."

;; Middleware tends to take a handler as its first arg, and return a handler.

; -----------------------------------------------------------------------------
; Tasks Are Middleware Factories

(deftask what
  "Specify a thing"
  [t thing THING str "An object"
   p pluralize bool "Whether to pluralize"]
  (fn middleware [next-handler]
    (fn handler [fileset]
      (next-handler (merge fileset {:thing thing :pluralize pluralize})))))

(deftask fire
  "Announce a thing is on fire"
  []
  (fn middleware [next-handler]
    (fn handler [fileset]
      (let [verb (if (:pluralize fileset) "are" "is")]
        (println "My" (:thing fileset) verb "on fire!")
        fileset))))

;; We would run this by typing in the CLI:

boot what -t "pants" -p - fire

;; Or, in the Boot REPL:

(boot (what :thing "pants" :pluralize true) (fire))

; -----------------------------------------------------------------------------
; Filesets

;; When we say that middleware is for creating "domain-specific" function
;; pipelines - we are saying that each handler expects to receive domain-
;; specific data and returns domain-specific data.

;; In Boot, each handler receives and returns a "fileset." This is an abstraction
;; which lets you treat files on your filesystem as immutable data.
