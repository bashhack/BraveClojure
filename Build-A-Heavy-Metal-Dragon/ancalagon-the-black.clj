;; Build a bitchin' heavy metal dragon, we'll call him Ancalagon the Black

; Building a model, using def to bind the a value (vector of maps)
(def asym-dragon-body-parts [{:name "brain-case" :size 5}
                             {:name "horn-core" :size 4}
                             {:name "left-wing-radius" :size 3}
                             {:name "left-phalanges" :size 1}
                             {:name "left-metacarpus" :size 3}
                             {:name "sternum" :size 3}
                             {:name "back" :size 10}
                             {:name "chest" :size 10}
                             {:name "left-wing-humerus" :size 3}
                             {:name "left-rear-hallux" :size 1}
                             {:name "left-metatarsus" :size 2}
                             {:name "neural-spines" :size 1}
                             {:name "chevron" :size 3}
                             {:name "caudal-spade" :size 6}
                             {:name "left-wing-phalanges" :size 1}
                             {:name "left-rear-knee-joint" :size 3}])

(defn matching-part
  "Returns a matching body part, if string pattern condition is met"
  [part]
  {:name (clojure.string/replace (:name part) #"^left-" "right-")
   :size (:size part)})

(defn create-symmetrical-body-parts
  "Expects a seq of maps that have/describe
  asymmetrical body parts with name and size keywords"
  [asym-body-parts]
  (loop [remaining-asym-parts asym-body-parts
         final-body-parts     []]
  (if (empty? remaining-asym-parts)
    final-body-parts
    (let [[part & remaining] remaining-asym-parts]
    (recur remaining
           (into final-body-parts
                 (set [part (matching-part part)])))))))

(create-symmetrical-body-parts asym-dragon-body-parts)

;; Result of calling create-symmetrical-body-parts, passing the asym-dragon-body-parts as an arg
;;  [
;;    {:name "brain-case", :size 5}
;;    {:name "horn-core", :size 4}
;;    {:name "left-wing-radius", :size 3}
;;    {:name "right-wing-radius", :size 3}
;;    {:name "right-phalanges", :size 1}
;;    {:name "left-phalanges", :size 1}
;;    {:name "right-metacarpus", :size 3}
;;    {:name "left-metacarpus", :size 3}
;;    {:name "sternum", :size 3}
;;    {:name "back", :size 10}
;;    {:name "chest", :size 10}
;;    {:name "left-wing-humerus", :size 3}
;;    {:name "right-wing-humerus", :size 3}
;;    {:name "left-rear-hallux", :size 1}
;;    {:name "right-rear-hallux", :size 1}
;;    {:name "right-metatarsus", :size 2}
;;    {:name "left-metatarsus", :size 2}
;;    {:name "neural-spines", :size 1}
;;    {:name "chevron", :size 3}
;;    {:name "caudal-spade", :size 6}
;;    {:name "right-wing-phalanges", :size 1}
;;    {:name "left-wing-phalanges", :size 1}
;;    {:name "left-rear-knee-joint", :size 3}
;;    {:name "right-rear-knee-joint", :size 3}
;;  ]

;; Knowledge Nuggets...
; ------------------------------------------------------------------------------
; `let` keyword

; The 'let` keyword seems define a 'lexically-scoped` binding (local to the enclosing parens(func)
; I immediately contrast this with the 'def' keyword binding,
; which seems to indicate a globally-scoped binding

; Examples:
(let [x "Sonata Arctica"]
  x)
; => "Sonata Arctica"

(def metal-regions
  ["Norway" "Sweden" "Denmark" "Finland" "Iceland" "Faroe Islands"])
(let [scandanavian-metal
      (take 3 metal-regions)]
  scandanavian-metal)
; => ("Norway" "Sweden" "Denmark")

; The key thing to remember is that `let` is really creating a new scope:
(def x 666)
(let [x 0] x) ; => 0

; We can also reference existing bindings:
(def x 666)
(let [x (inc x)] x) ; => 667

; We can also still use rest params:
(let [[norovegr & rest-of-scandinavia-proper-and-sometimes-included-countries] metal-regions]
  [norovegr rest-of-scandinavia-proper-and-sometimes-included-countries])
; => ["Norway" ("Sweden" "Denmark" "Finland" "Iceland" "Faroe Islands")]

; Taking these properties of `let` bindings, let's return to the code above:
(let [[part & remaining] remaining-asym-parts]
  ; within the new let scope, let part be the associated/bound
  ; with the first element of the remaining-asym-parts,
  ; and let remaining be associated with the rest of the elements
  (recur remaining
         (into final-body-parts
               (set [part (matching-part part)]))))

; Breaking this down, further, the action within the recurring function is:
; 1) calling the `set` function (remember, set is a unique collection of elements
; (i.e., #{}) to create a set data structure consisting of `part` and its matching part
; 2) calling the `into` function to add that set to the vector `final-body-parts`
; 3) We use `set` here because we need a unique collection, given that `set` and `matching-part part`
; will sometimes return the same value

; Importantly: the use of the `let` becomes a sort of convenience method, allowing Ex. A
; to avoid the more verbose syntax in the functionally equivalent Ex. B

; Ex. A
; (let [[part & remaining] remaining-asym-parts]
;   (recur remaining
;          (into final-body-parts
;                (set [part (matching-part part)]))))

; Ex. B
; (recur (rest remaining-asym-parts)
;        (into final-body-parts
;              (set [(first remaining-asym-parts) (matching-part (first remaining-asym-parts))])))

; Using `let` is a way we can simplify our code by giving us
; local names for the complex data we're bound to work with

; ------------------------------------------------------------------------------
; `loop` keyword

(loop [iteration 0]
  (println (str "Iteration " iteration))
  (if (> iteration 3)
    (println "Goodbye!")
    (recur (inc iteration))))

; The loop keyword (as in the main `create-symmetrical-body-parts` function),
; gives a quick and readable method for recursion - avoiding something like this:
(defn recursive-counter
  ([]
   (recursive-counter 0))
  ([iteration]
   (println (str "Iteration " iteration))
   (if (> iteration 3)
     (println "Goodbye!")
     (recursive-counter (inc iteration)))))
(recursive-counter)

; (both functions return the same values:
; => Iteration 0
; => Iteration 1
; => Iteration 2
; => Iteration 3
; => Iteration 4
; => Goodbye!

; NOTE: `loop` method has much better performance, as well!

; ------------------------------------------------------------------------------
; Regex

; In Clojure, regexes are created by using a hash mark and then placing the
; expression on which to match directly afterwards
#"regular-expression"

(re-find #"^dragon-" "dragon-fury")
; => "dragon-"

(re-find #"^ritual-of-fire-" "ritual-of-the-moon")
; => nil

; ------------------------------------------------------------------------------
; An In-Depth Overview of the Code

(def asym-dragon-body-parts [{:name "brain-case" :size 5}
                             {:name "horn-core" :size 4}
                             {:name "left-wing-radius" :size 3}
                             {:name "left-phalanges" :size 1}
                             {:name "left-metacarpus" :size 3}
                             {:name "sternum" :size 3}
                             {:name "back" :size 10}
                             {:name "chest" :size 10}
                             {:name "left-wing-humerus" :size 3}
                             {:name "left-rear-hallux" :size 1}
                             {:name "left-metatarsus" :size 2}
                             {:name "neural-spines" :size 1}
                             {:name "chevron" :size 3}
                             {:name "caudal-spade" :size 6}
                             {:name "left-wing-phalanges" :size 1}
                             {:name "left-rear-knee-joint" :size 3}])

(defn matching-part
  "Returns a matching body part, if string pattern condition is met"
  [part]
  {:name (clojure.string/replace (:name part) #"^left-" "right-")
   :size (:size part)})

(defn create-symmetrical-body-parts ; <= (1) =>
  "Expects a seq of maps that have/describe
  asymmetrical body parts with name and size keywords"
  [asym-body-parts]
  (loop [remaining-asym-parts asym-body-parts ; <= (2) =>
         final-body-parts     []]
  (if (empty? remaining-asym-parts) ; <= (3) =>
    final-body-parts
    (let [[part & remaining] remaining-asym-parts] ; <= (4) =>
    (recur remaining ; <= (5) =>
           (into final-body-parts
                 (set [part (matching-part part)])))))))

(create-symmetrical-body-parts asym-dragon-body-parts)


; (1) The function relies on a typical FP technique/pattern
; of taking a sequence and continuously splitting the sequence
; into its head and tail (here, using the rest param to do so).
; The head is then processed, adds to it some result, and using
; recursion continues the process with the tail
;
; (2) Here, we begin begin our loop - binding the full sequence of
; asym-body-parts initially to the remaining-asym-parts, we will
; later bind remaining-asym-parts to the tail of asym-body-parts.
; We also bind final-body-parts to an empty vector.
;
; (3) If remaining-asym-parts is empty, we've processed the tail
; of the sequence and can return the result (final-body-parts).
; Otherwise, (4) we split the list into its head (part) and tail
; (remaining).
;
; (5) We recur with remaining, a list that gets shorter by
; by one element each iteration of the loop, and the into expression,
; which builds our vector of symmetrical parts
;
; NOTE: In short, we can say that this pattern of working with data
; is a pattern where we `process each element in a sequence and
; build a result` ... this is common enough that there is a built-in
; and of course, is also a higher-order in other languages like JavaScript,
; and it's called `reduce`:
;
; ;; sum with reduce
(reduce + [6 6 6]) ; => 18
; (+ (+ 6 6) 6)
;
; We could create our own reduce like this:
(defn my-reduce
  ([f initial coll]
   (loop [result    initial
          remaining coll]
     (if (empty? remaining)
       result
       (recur (f result (first remaining)) (rest remaining)))))
  ([f [head & tail]]
   (my-reduce f head tail)))

; With our new knowledge of reduce, we could rewrite our main process:
(defn better-create-symmetrical-body-parts
  "Expects a seq of maps that have/describe
  asymmetrical body parts with name and size keywords"
  [asym-body-parts]
  (reduce (fn [final-body-parts part]
            (into final-body-parts (set [part (matching-part part)])))
          []
          asym-body-parts))

; ------------------------------------------------------------------------------
; Adding functionality...

(defn hit
  [asym-body-parts]
  (let [sym-parts          (better-create-symmetrical-body-parts asym-body-parts)
        body-part-size-sum (reduce + (map :size sym-parts))
        target             (rand body-part-size-sum)]
    (println (str "Body-part-size-sum:" body-part-size-sum) (str "Target is:" target))
    (loop [[part & remaining] sym-parts
           accumulated-size   (:size part)]
      (println (str "Accumulated-size is: " accumulated-size))
      (if (> accumulated-size target)
        part
        (recur remaining (+ accumulated-size (:size (first remaining))))))))

(hit asym-dragon-body-parts)
