
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
