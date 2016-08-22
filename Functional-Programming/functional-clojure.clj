
;;;; ---------------------------------------------------------------------------
;;;; ---------------------   Functional Programming   --------------------------
;;;; ---------------------------------------------------------------------------

;;; Topics:
;;; - Pure Functions (what they are / why they are useful)
;;; - Immutable Data Structures (why they are superior to their mutable cousins)
;;; - Separation of data and functions (discover its power and flexibility)
;;; - Program to small sets of data abstraction (write better code, faster)

;;;; ---------------------------------------------------------------------------
;;;; Pure Functions
;;;; ---------------------------------------------------------------------------

;;; Not surprisingly, all the functions we've explored so far (except `println`
;;; and `rand`) have been pure functions. So, what is a pure function?
;;; (1) - It always returns the same result if given the same args. When true,
;;;       we call this property 'referential transparency'
;;; (2) - It can't cause any side effects. That is, the function can't make
;;;       any changes that are observable outside the function itself

;;; The benefits of pure functions are many, but primarily they make our code
;;; easier to reason about, provide isolation such that they are unable to
;;; impact other parts of the system, and they are consistent. This all
;;; translates to code that is easier to maintain, has great readability,
;;; and has fewer bugs.

(defn metal-wisdom
  ;; The function depends on a string, which is immutable,
  ;; meaning this function is referentially transparent
  [words]
  (str words ", as it's the first step toward being a rock god"))

(metal-wisdom "Rub your stomach and pat your head")
; => "Rub your stomach and pat your head, as it's the first step toward being a rock god"

(defn how-metal-is-it
  []
  (if (> (rand) 0.6)
    "It's freaking metal!"
    "Nah, it's not very metal at all"))

;;; Just as the function above uses a random number generator, making it
;;; impure, so too is a function made impure if there is file I/O happening:

(defn read-file
  ;; NOTE: THis function is NOT referentially transparent
  [filename]
  (analysis (slurp filename)))

(defn analysis
  ;; NOTE: This function is referentially transparent
  [text]
  (str "Character count: " (count text)))

;;; What about that whole no side effects business? Let's see that in action!
;;; First, in JavaScript:

var mutableObject = { emotion: "Happy!" };

var mutator = function (obj) { obj.emotion = "Depressed" };

mutator (mutableObject);

console.log(mutableObject.emotion); // => "Depressed"

;;; Let's get real about side effects...yes, for a program to do something
;;; there are going to be side effects! People sometimes knock FP for its
;;; fixation with side effects, asking how one gets anything done. What is
;;; overlooked by those unfamiliar with the core concepts of FP is that
;;; we're not saying side effects can't or don't exists - we're cautioning
;;; against their burden. To be aware of and limit exposure to side effects
;;; is our goal, pure and simple.

;;;; ---------------------------------------------------------------------------
;;;; Immutable Data Structures
;;;; ---------------------------------------------------------------------------

;;; Immutable data structures make sure our code won't have side effects - but
;;; how on earth will we get things done, you ask?

;;; In JavaScript we often write code as shown in the next two examples:

;//  var guitarists = getAllGuitarists();
;//  var totalSolos = 0;
;//  var ii = guitarists.length;

;//  for (var i = 0; i < ii; i++) {
;//    totalSolos += guitarists[i].solosPlayed;
;//  };

;;; Or...

;//  var guitarists = getAllGuitarists();
;//  var totalSolos = [];
;//  var ii = guitarists.length;

;//  for (var i = 0; i < ii; i++) {
;//    if (guitarists[i].didPlaySolo) {
;//      totalSolos.push(guitarists[i].soloName);
;//    };
;//  };

;;; Here, we have outside variables acting on the body of the inner function
;;; and so would be introducing side effects, to avoid this we can utilize
;;; the strategy of recursion.

(defn sum
  ([vals]
   (sum vals 0))
  ([vals accumulating-total]
   (if (empty? vals)
     accumulating-total
     (sum (rest vals) (+ (first vals) accumulating-total)))))

(sum [39 5 1])
; => (sum [39 5 1]) ; single-arity body calls two-arity body
; => (sum [39 5 1] 0)
; => (sum [5 1] 39)
; => (sum [1] 44)
; => (sum [] 45) ; base case is reached, so return accumulating-total
; => 45

;;; We have no tail call optimization in Clojure, we tend to use `recur`:

(defn sum
  ([vals]
   (sum vals 0))
  ([vals accumulating-total]
   (if (empty? vals)
     accumulating-total
     (recur (rest vals) (+ (first vals) accumulating-total)))))

;;; NOTE: One might worry that you'd be creating a ton of intermediate
;;; values - in JavaScript we might encounter a stack overflow. In Clojure,
;;; we avoid this because all immutable data structures are implemented
;;; using "structural sharing". For more info:
;;; http://hypirion.com/musings/understanding-persistent-vector-pt-1.

;;; When thinking functionally, we often say our code is at its best
;;; when favoring composability over inheritance, and using composition
;;; instead of attribute mutation.

(require '[clojure.string :as string])
(defn metalize
  [text]
  (string/replace (string/trim text) #"country" "metal"))

(metalize "Man, there's nothing better than a cold beer and some country music.   ")
; => "Man, there's nothing better than a cold beer and some metal music."

;;;; ---------------------------------------------------------------------------
;;;; Fun With Pure Functions
;;;; ---------------------------------------------------------------------------

;;; Comp (comp) (comp f) (comp f g) (comp f g & fs)
;;; "Takes a set of functions and returns a fn that is the composition
;;; of those fns. The return fn takes a variable number of args, applies
;;; the rightmost of fns to the args, the next fn (right-to-left)
;;; to the result, etc."

;; A simple example:
((comp inc *) 2 3)
; => 7 (NOTE: Applies * to args first, then applies inc to the value returned from the result of 2 * 3

(def lord-of-metal
  {:name "Ivan the Shred"
   :attributes {:propensity-to-melt-faces-with-solos 10
                :can-apply-corpse-paint-while-shredding 2
                :is-able-to-summon-demon-with-power-chord 7}})
(def face-melting-musicianship (comp :propensity-to-melt-faces-with-solos :attributes))
(def distracted-makeup-application (comp :can-apply-corpse-paint-while-shredding :attributes))
(def mystical-chord-chops (comp :is-able-to-summon-demon-with-power-chord :attributes))

(face-melting-musicianship lord-of-metal)
; => 10

(distracted-makeup-application lord-of-metal)
; => 2

(mystical-chord-chops lord-of-metal)
; => 7

;;; Another example (first without comp and then with)
(defn power-of-summoned-metal-demon
  [metal-god]
  (int (inc (/ (mystical-chord-chops metal-god) 2))))

(power-of-summoned-metal-demon lord-of-metal)
; => 4

;;; This is okay, but comp makes this even more concise:
(def power-of-summoned-metal-demon-comp (comp int inc #(/ % 2) face-melting-musicianship))
(power-of-summoned-metal-demon-comp lord-of-metal)
; => 6

;;; Under the hood, compose works like this (here, a two arg example):
(defn two-comp
  [f g]
  (fn [& args]
    (f (apply g args))))

;;; Memoize (memoize f)
;;; "Returns a memoized version of a referentially transparent function. The
;;; memoized version of the function keeps a cache of the mapping from arguments
;;; to results and, when calls with the same arguments are repeated often, has
;;; higher performance at the expense of higher memory use."

;;; The advantage of memoization is that we can store the argument passed to
;;; a function and the return value of the function. This type of 'caching'
;;; results in subsequent calls to the same function with the same arguments
;;; returning the result immediately. For functions which are computationally
;;; intensive, this can be a huge performance gain!

;;; Non-Memoized:
(def sleepy-identity
  "Returns a value after 1 second"
  [x]
  (Thread/sleep 1000)
  x)

(sleepy-identity "Mr. Malmsteen")
; => "Mr. Malmsteen" after 1 second

(sleepy-identity "Mr. Malmsteen")
; => "Mr. Malmsteen" after 1 second

;;; Memoized:
(def memo-sleepy-identity (memoize sleepy-identity))
(memo-sleepy-identity "Mr. Malmsteen")
; => "Mr. Malmsteen" after 1 second

(memo-sleepy-identity "Mr. Malmsteen")
; => "Mr. Malmsteen" immediately
