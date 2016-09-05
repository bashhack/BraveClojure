
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

;;;; ---------------------------------------------------------------------------
;;;; Working with the `def` Keyword
;;;; ---------------------------------------------------------------------------

;;; Our primary method of storing the obejcts we reference is via `def` - even
;;; other utilities like `defn` are still using `def` at their core.
;;; Let's use `def` again for practice:

(def metal-genres ["grindcore" "stoner rock" "gothic symphonic metal"])
; => #'user/metal-genres

metal-genres
; => ["grindcore" "stoner rock" "gothic symphonic metal"]

;;; (1) Here, we update the current namespace's map with the association between
;;; the symbol we have created with the name `metal-genres` and the var,
;;; the vector `["grindcore" "stoner rock" "gothic symphonic metal"]`

;;; (2) Clojure finds a place to store the var

;;; (3) The vector is stored

;;; (4) An address to the symbol is written on the var

;;; (5) We return the var `#'user/metal-genres`

;;; The process above is called `interning` a var. We can view the entire map
;;; of symbols-to-interned-vars using the method `ns-interns`:

(ns-interns *ns*)
; => {metal-genres #'user/metal-genres}

(get (ns-interns *ns*) 'metal-genres)
; => #'user/metal-genres

;;; Using `ns-map` will return the map that the current namespace uses when
;;; returning a var from a symbol name...the output, as the author stated, was
;;; large...having this method available immediately reminded me of how I might
;;; do the same in a Python script using the `dir` method: `dir(__name__)`

;;; To get the var associated with the symbol from our map, we can use `deref`:

(deref #'user/metal-genres)
; => ["grindcore" "stoner rocker" "gothic symphonic metal"]

;;; It's important to remember is that (deref #'user/metal-genres) is equal to:

metal-genres
; => ["grindcore" "stoner rock" "gothic symphonic metal"]

;;; Let's get crazy for a bit!

(def metal-genres ["deathcore" "folk metal" "goblin metal"])
; => #'user/metal-genres

metal-genres
; => ["deathcore" "folk metal" "goblin metal"]

;;; Wait - hold up...we just overwrote the first vector - we've just created
;;; a name collision conflict!

;;;; ---------------------------------------------------------------------------
;;;; Creating and Switching Betwen Namespaces
;;;; ---------------------------------------------------------------------------
