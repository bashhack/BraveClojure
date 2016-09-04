
;;;; ---------------------------------------------------------------------------
;;;; ----------------------- Organizing Your Project ---------------------------
;;;; ---------------------------------------------------------------------------

;;; Topics:
;;; - What `def` does
;;; - What namespaces are and how to use them
;;; - The relationship between namespaces and the filesystem
;;; - How to use `refer`, `alias`, `require`, `use` and `ns`
;;; - How to organize Clojure projects using the filesystem

;;;; ---------------------------------------------------------------------------
;;;; A Clojure Project Is Like A Library...
;;;; ---------------------------------------------------------------------------

;;; ...where Clojure functions as a vast collection of objects on shelves
;;; (from data structures to functions), where `namespaces` are a system that
;;; bridges `symbols` to `vars`. In this way, we can provide human-readable
;;; name to the inner workings of the Clojure language.

;;; Namespaces themselves are objects, such that:
;;; `namespace` :: `clojure.lang.Namespace`
;;; Because namespaces are objects, we can interact with them like any other
;;; data structure:

(ns-name *ns*)
; => user

;;; Above, we invoke the current namespace with `*ns*` and we get its name
;;; using the function `ns-name`

;;; REMEMBER: We are always in a namespace!

;;; What about these `symbols` we are talking about - well, we've been using
;;; symbols the entire time!

;;; For example, when we type:
(map inc [1 2])
(if (> (rand) 0.6))
(str 'Hello, ' name)
;;; `map`, `inc`, `if`, `>`, `str` are all symbols - a Clojre data structure -
;;; and when we write a symbol, Clojure finds the corresponding `var` in the
;;; current namespace and returns that object back - in these cases, a function
;;; that `map` or `>` or `str` refers to.

;;; We can use the symbol without its actual reference by quoting the symbol:
'map
'inc
'str
; etc...
