
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

;;; We can create a new namespace in one of three ways:

(create-ns 'metal.nu)
; => #namespace[metal.nu]

;;; IMPORTANT: We're likely to never really use or find much benefit in
;;; `create-ns`, much like the nu-metal genre itself. The function creates
;;; a new name space and does not move into it.

(in-ns 'metal.funk)
; => #namespace[metal.funk]

;;; Unlike the function `create-ns`, the function `in-ns` creates and then
;;; moves into the newly created namespace.

;;; Finally, there's the `ns` macro which is the msot widely used of the three
;;; tools, but we'll cover that later....

(in-ns 'metal.pirate)
(def ales ["stout" "porter" "cider"])
(in-ns 'metal.speed)

ales
; => CompilerException java.lang.RuntimeException: Unable to resolve symbol:
;    ales in this context, compiling:(*cider-repl localhost*:1:7308)

metal.pirate/ales
; => ["stout" "porter" "cider"]

;;; When we want to reference objects in other namespaces, it can be a pain to
;;; write the fully qualified name every time, so thankfully we have tools:
;;; `refer` and `alias`

(in-ns 'metal.pirate)
(def ales ["stout" "porter" "cider"])
(def meats ["bear" "narwhal" "polar bear"])
(in-ns 'metal.speed)
(clojure.core/refer 'metal.pirate)

meats
; => ["bear" "narwhal" "polar bear"]

ales
; => ["stout" "porter" "cider"]

;;; We can also use filters like `:only`, `:exclude`, and `:rename`

(clojure.core/refer 'metal.pirate :only ['ales])

ales
; => ["stout" "porter" "cider"]

meats
; => CompilerException java.lang.RuntimeException: Unable to resolve symbol:
;    meats in this context, compiling:(*cider-repl localhost*:1:7308)

(clojure.core/refer 'metal.pirate :exclude ['ales])

ales
; => CompilerException java.lang.RuntimeException: Unable to resolve symbol:
;    ales in this context, compiling:(*cider-repl localhost*:1:7308)

meats
; => ["beat" "narwhal" "polar bear"]

(clojure.core/refer 'metal.pirate :rename {'ales 'tasty-ales})

ales
; => CompilerException java.lang.RuntimeException: Unable to resolve symbol:
;    ales in this context, compiling:(*cider-repl localhost*:1:7308)

tasty-ales
; => ["stout" "porter" "cider"]

;;; Well, wait a second...this isn't really what we want is it? Having to yet
;;; again refer to the fully qualified names - thankfully a call to
;;; `clojure.core/refer-clojure` will allow us to only write `refer`

(in-ns 'metal.death)
(clojure.core/refer-clojure)
(refer 'metal.speed)

ales
; => ["stout" "porter" "cider"]

meats
; => ["beat" "narwhal" "polar bear"]

;;; Boy, wouldn't it be nice if we didn't have to write `metal.death` or
;;; `metal.funk` each time we refered to the namespace? Yeah, it would!

;;; This is where `alias` comes to the rescue:

(in-ns 'metal.speed)
(clojure.core/alias 'pirate 'metal.pirate)

pirate/ales
; => ["stout" "porter" "cider"]

;;;; ---------------------------------------------------------------------------
;;;; Organizing A Real Project
;;;; ---------------------------------------------------------------------------

;;; NOTE: My test project to practice configuring a new Clojure project can
;;; be found under the project directory 'Hunt-For-The-Thief-Who-Stole-Rock'

;;; The basic project directory structure after running `lein new app ...`
;;; looks like this:

;;; .gitignore
;;; doc/
;;;   intro.md
;;; project.clj
;;; README.md
;;; LICENSE
;;; CHANGELOG.md
;;; resources/
;;; src/
;;;   hunt_for_the_thief_who_stole_rock/
;;;     core.clj
;;; test/
;;;   hunt_for_the_thief_who_stole_rock/
;;;     core_test.clj

;;; At the top of core.clj, we see:
(ns hunt-for-the-thief-who-stole-rock.core
  (:gen-class))

;;; This is the direct usage of the `ns` macro we referred to before...
;;; Here, a namespace is created with the fully qualified name
;;; #hunt-for-the-thief-who-stole-rock/core (hunt-for-the-thief-who-stol-rock.core)

;;; IMPORTANT:
;;; The name to the left of the `.` in a namespace corresponds to a directory!
;;;
;;; So, 'hunt-for-the-thief-who-stole-rock.core' is:
;;;
;;; hunt_for_the_thief_who_stole_rock/
;;;   core.clj
;;;
;;; Let's create another namespace:
;;; hunt-for-the-thief-who-stole-rock.visualization.svg
;;;
;;; hunt_for_the_thief_who_stole_rock/
;;;   visualize/
;;;     svg.clj

;;; Having added code to our svg.clj, and required it in core.clj, after
;;; running `lein run` we now see the nREPL print out:
;;; 60.17,24.94 48.15,17.11 48.14,11.58 50.45,30.52

;;; Let's change our require statement from:
(require 'hunt-for-the-thief-who-stole-rock.visualization.svg)

;;; ...to...
(:require [hunt-for-the-thief-who-stole-rock.visualization.svg
           :as svg
           :refer [points]])
;;; ...or...
(:require [hunt-for-the-thief-who-stole-rock.visualization.svg :as svg])
;;; NOTE: We would then use svg/points in our main function

;;; Project continues, see final in directory....

;;;; ---------------------------------------------------------------------------
;;;; ---------------------------------------------------------------------------
;;;; ---------------------------------------------------------------------------
