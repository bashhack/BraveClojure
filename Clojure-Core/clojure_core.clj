; Clojure Core Library - Exploring the `clojure.core`, in detail

; Ref: https://clojuredocs.org/core-library

; ------------------------------------------------------------------------------
; Thinking About Sequence and Abstraction

;; One of the many benefits of Clojure is that it allows us to use the concept:
;; 'programming to abstractions' - specifically, programming using the sequence
;; and collection abstractions. Further, this opens the door to the evaluation
;; of 'lazy sequences.'

;; When we talk about programming to abstractions, we're really referring to
;; the idea of an object, or data-structure, being an instance of an abstraction.
;; If that object responds to "the core sequence operations (the functions 'first',
;; 'rest', and 'cons')" we know it will also work with our mainstay higher-order
;; functions ('map', 'filter', 'reduce', etc). In the words of Rich Hickey, the
;; creator of Clojure, "having an open, and large, set of functions operate upon
;; an open, and small, set of extensible abstractions is the key to algorithmic
;; reuse and library interoperability".

;; We can also refer back to the forward of Abelson and Sussman's SICP:
;; "The simple structure and natural applicability of lists are reflected in
;; functions that are amazingly nonidiosyncratic. In Pascal the plethora of
;; declarable data structures induces a specialization within functions that
;; inhibits and penalizes casual cooperation. It is better to have 100 functions
;; operate on one data structure than to have 10 functions operate on 10 data structures."

;; But...let's talk about sequence - that is, what is a sequence?

;; We might say that a sequence is a 'collection of elements organized in
;; linear order, as opposed to an unordered collection or a graph without
;; a before-and-after relationship between its nodes.'

;; Going back to our Clojure basics, the following data-structures all implement
;; the sequence abstraction: lists, maps, vectors, and sets

;; Given this principle:
(defn metalize-your-startup
  [startup-service]
  (str
   "If you love Twitter? You'll love Footsteps of Odin, an agile company "
   "that is reimagining " startup-service " for fans of doom and speed metal."))

; Vector
(map metalize-your-startup ["Scrapbooking" "Woodworking"])

; List
(map metalize-your-startup '("Crocheting" "Competitive Hot Dog Eating"))

; Set
(map metalize-your-startup #{"Gardening" "Flyfishing"})

; Map (map f) (map f coll) (map f c1 c2) (map f c1 c2 c3) (map f c1 c2 c3 & colls)

(map #(metalize-your-startup (second %)) {:unusual-hobby "Extreme Ironing"})

;; Taking a look at the three core functions that define a sequence, we'll
;; build up a working understanding of these by using them to build the
;; higher-order function 'map' with them. Along the way, we'll contrast
;; a sequence against the JavaScript implementation of a linked list.

;; Remember, "all Clojure asks is 'can I 'first', 'rest', and 'cons' it?'
;; If the answer is yes, you can use the seq library with that data structure.

var node1 = {
  value: 'first',
  next: node2
};

var node2 = {
  value: 'middle',
  next: node3
};

var node3 = {
  value: 'last',
  next: null
};

var first = function (node) {
  return node.value;
};

var rest = function (node) {
  return node.next;
};

var cons = function (newValue, node) {
  return {
    value: newValue,
    next: node
  };
};

first(node1);
; //=> "first"

first(rest(node1));
; //=> "middle"

first(rest(rest(node1)));
; //=> "last"

var node0 = cons("new first", node1);
first(node0);
; //=> "new first"

first(rest(node0));
; //=> "first"

;; Implementing map using 'first', 'rest', and 'cons':
var map = function (list, transformFn) {
  if (list === null) {
    return null;
  } else {
    return cons(transformFn(first(list)), map(rest(list), transformFn));
  }
}

var arrayFirst = function (array) {
  return array [0]
};

var arrayRest = function (array) {
  var sliced = array.slice (1, array.length);
  if (sliced.length === 0) {
    return null;
  } else {
    return sliced;
  }
};

var arrayCons = function (newValue, array) {
  return [newValue].concat(array);
};

var list = ["Putrid Nightmare", "Darkness of the Beast"];
map(list, function (val) { return val + " mapped!"})
; //=> ["Putrid Nightmare mapped!", "Darkness of the Beast mapped!"]

; Enough JavaScript for now! Back to Clojure! :)

(seq '(1 2 3))
; => (1 2 3)

(seq [1 2 3])
; => (1 2 3)

(seq #{1 2 3})
; => (1 2 3)

(seq {:name "Lemmy" :occupation "Rock and Roll Demigod"})
; => ([:name "Lemmy"] [:occupation "Rock and Roll Demigod"])

;; In the above examples, we apply seq to an argument (i.e., a data-structure)
;; and our return value looks and behaves like a list

(into {} (seq {:a 1 :b 2 :c 3}))
; => {:a 1 :b 2 :c 3}

;; Applying the into function with two args (an empty data-structure, and an
;; application of seq against a populated data-structure) will undo the effect
;; of seq (i.e., taking the "listed" map and returning the original map structure)

; ------------------------------------------------------------------------------
; Seq Function Examples

; Map

;; We've already looked at map in-depth, but map can also be extended to handle
;; the following:
;; (1) taking multiple collections as arguments
;; (2) taking a collection of functions as an argument

(map str ["a" "b" "c"] ["A" "B" "C"])
; => ("aA" "bB" "cC")

;; When we pass multiple arguments to map, our transform function must be capable
;; of taking arguments equal to the number we supply.

;; Here, behind the scenes, it's as if:
(list (str "a" "A") (str "b" "B") (str "c" "C"))

;; Another example of passing multiple collections:
(def nigel-tufnels-volume [11 11 11 11 11])
(def everyone-elses-volume [10 10 10 10 10])
(defn unify-amp-volume-data
  [nigel others]
  {:nigel-tufnel nigel
   :everyone-else others})

(map unify-amp-volume-data nigel-tufnels-volume everyone-elses-volume)
;; ({:nigel-tufnel 11, :everyone-else 10}
;;  {:nigel-tufnel 11, :everyone-else 10}
;;  {:nigel-tufnel 11, :everyone-else 10}
;;  {:nigel-tufnel 11, :everyone-else 10}
;;  {:nigel-tufnel 11, :everyone-else 10})

;; An example of operating on a single collection with multiple functions
(def sum #(reduce +  %))
(def avg #(/ (sum %) (count %)))
(defn stats
  [numbers]
  (map #(% numbers) [sum count avg]))

(stats [6 6 6])
; => (18 3 6)

;; Finally, a very common usage of map is to "retrieve the value associated with
;; a keyword from a collection of map data structures...because keywords can be
;; used as functions, you can do this succinctly"

(def metal-stage-names
  [{:alias "Alice Cooper" :real "Vincent Damon Furnier"}
   {:alias "Dio" :real "Ronald James Padavona"}
   {:alias "Slash" :real "Saul Hudson"}
   {:alias "Quorthon" :real "Thomas Borje Forsberg"}])

(map :real metal-stage-names)
; => ("Vincent Damon Furnier" "Ronald James Padavona" "Saul Hudson" "Thomas Borje Forsberg")

; Reduce (reduce f coll) (reduce f val coll)

;; As we saw before, reduce processes each element in a sequence
;; and builds a result, but we can use reduce to other ends, as well.

;; Here, we transform a map's values, producing a new map with
;; the same keys but with updated values

;; Instead of writing this (NOTE: (assoc map key val):
(assoc (assoc {} :max (inc 30))
       :min (inc 10))

;; We can write this:
(reduce (fn [new-map [key val]]
          (assoc new-map key (inc val)))
        {}
        {:max 30 :min 10})
; => {:max 31, :min 11}

;; Another use of reduce can be filtering out keys from a map based on their value:
(reduce (fn [new-map [key val]]
          (if (val > 4)
            (assoc new-map key val)
            new-map))
        {}
        {:human 4.1
         :critter 3.9})
; => {:human 4.1}

; ------------------------------------------------------------------------------
; TODO: (1) Implement `map` (2) `filter` and (3) `some` using `reduce`
; ------------------------------------------------------------------------------

; Take (take n) (take n coll)
; Take-While (take-while pred) (take-while pred coll)
; Drop (drop n) (drop n coll)
; Drop-While (drop-while pred) (drop-while pred coll)

(take 2 [6 6 6])
; => (6 6)

(drop 2 [6 6 6])
; => (6)

(def rock-journal
  [{:month 1 :day 6 :band "The Velvet Underground" :venue "CBGB"}
   {:month 1 :day 6 :band "Misfits" :venue "CBGB"}
   {:month 2 :day 6 :band "Alice In Chains" :venue  "Crocodile Cafe"}
   {:month 2 :day 6 :band "Neil Young" :venue "Fillmore Auditorium West"}
   {:month 3 :day 6 :band "Guns N' Roses" :venue "The Troubadour"}
   {:month 3 :day 6 :band "Motley Crue" :venue "Whisky A Go Go"}
   {:month 4 :day 6 :band "R.E.M" :venue "The 40 Watt Club"}
   {:month 4 :day 6 :band "The Sex Pistols" :venue "The 100 Club"}])

(take-while #(< (:month %) 3) rock-journal)
; => ({:month 1, :day 6, :band "The Velvet Underground", :venue "CBGB"}
; =>  {:month 1, :day 6, :band "Misfits", :venue "CBGB"}
; =>  {:month 2, :day 6, :band "Alice In Chains", :venue "Crocodile Cafe"}
; =>  {:month 2, :day 6, :band "Neil Young", :venue "Fillmore Auditorium West"})

(drop-while #(< (:month %) 3) rock-journal)
; => ({:month 3 :day 6 :band "Guns N' Roses" :venue "The Troubadour"}
; =>  {:month 3 :day 6 :band "Motley Crue" :venue "Whisky A Go Go"}
; =>  {:month 4 :day 6 :band "R.E.M" :venue "The 40 Watt Club"}
; =>  {:month 4 :day 6 :band "The Sex Pistols" :venue "The 100 Club"})

(take-while #(< (:month %) 4)
            (drop-while #(< (:month %) 2) rock-journal))
; => ({:month 2 :day 6 :band "Alice In Chains" :venue  "Crocodile Cafe"}
; =>  {:month 2 :day 6 :band "Neil Young" :venue "Fillmore Auditorium West"}
; =>  {:month 3 :day 6 :band "Guns N' Roses" :venue "The Troubadour"}
; =>  {:month 3 :day 6 :band "Motley Crue" :venue "Whisky A Go Go"})

; Filter (pred) (pred coll)
; Some (pred coll)

(def rock-journal
  [{:month 1 :day 6 :band "The Velvet Underground" :venue "CBGB"}
   {:month 1 :day 6 :band "Misfits" :venue "CBGB"}
   {:month 2 :day 6 :band "Alice In Chains" :venue  "Crocodile Cafe"}
   {:month 2 :day 6 :band "Neil Young" :venue "Fillmore Auditorium West"}
   {:month 3 :day 6 :band "Guns N' Roses" :venue "The Troubadour"}
   {:month 3 :day 6 :band "Motley Crue" :venue "Whisky A Go Go"}
   {:month 4 :day 6 :band "R.E.M" :venue "The 40 Watt Club"}
   {:month 4 :day 6 :band "The Sex Pistols" :venue "The 100 Club"}])

(filter #(= (:venue %) "CBGB") rock-journal)
; => ({:month 1 :day 6 :band "The Velvet Underground" : venue "CBGB"}
; =>  {:month 1 :day 6 :band "Misfits" :venue "CBGB"})

(take-while #(> (:month %) 3) rock-journal)
; =>  {:month 4 :day 6 :band "R.E.M" :venue "The 40 Watt Club"}
; =>  {:month 4 :day 6 :band "The Sex Pistols" :venue "The 100 Club"})

;; NOTE: filter is useful, but remember it processes all the data -
;; take-while on the other hand doesn't have to process all data
;; before returning a value

;; Closely related to filter is the some function, which returns the first
;; truthy value (any value not false or nil) returned by a predicate (
;; i.e., filter criteria) function


(def rock-journal
  [{:month 1 :day 6 :band "The Velvet Underground" :venue "CBGB"}
   {:month 1 :day 6 :band "Misfits" :venue "CBGB"}
   {:month 2 :day 6 :band "Alice In Chains" :venue  "Crocodile Cafe"}
   {:month 2 :day 6 :band "Neil Young" :venue "Fillmore Auditorium West"}
   {:month 3 :day 6 :band "Guns N' Roses" :venue "The Troubadour"}
   {:month 3 :day 6 :band "Motley Crue" :venue "Whisky A Go Go"}
   {:month 4 :day 6 :band "R.E.M" :venue "The 40 Watt Club"}
   {:month 4 :day 6 :band "The Sex Pistols" :venue "The 100 Club"}])

(some #(> (:month %) 5) rock-journal)
; => nil

(some #(> (:month %) 3) rock-journal)
; => true

(def rock-journal
  [{:month 1 :day 6 :band "The Velvet Underground" :venue "CBGB"}
   {:month 1 :day 6 :band "Misfits" :venue "CBGB"}
   {:month 2 :day 6 :band "Alice In Chains" :venue  "Crocodile Cafe"}
   {:month 2 :day 6 :band "Neil Young" :venue "Fillmore Auditorium West"}
   {:month 3 :day 6 :band "Guns N' Roses" :venue "The Troubadour"}
   {:month 3 :day 6 :band "Motley Crue" :venue "Whisky A Go Go"}
   {:month 4 :day 6 :band "R.E.M" :venue "The 40 Watt Club"}
   {:month 4 :day 6 :band "The Sex Pistols" :venue "The 100 Club"}])

(some #(and (> (:month %) 2) %) rock-journal)
; => {:month 3, :day 6, :band "Guns N' Roses," :venue "The Troubadour"}

; Sort (sort coll) (sort comp coll)
; Sort-By (sort-by keyfn coll) (sort-by keyfn comp coll)

(sort [666 641 695])
; => (641 666 695)

(sort-by count ["acdc" "bestial warlust" "pegazus" "wolfmother"])
; => ("acdc" "pegazus" "wolfmother" "bestial warlust")

; Concat (concat) (concat x) (concat x y) (concat x y  & zs)

(concat [6] [6 6] [666])
; => (6 6 6 666)

(into [] (concat [6] [6] [6]))
; => [6 6 6]

;; NOTE: concat returns a lazy sequence representing the concatenation
;; of the elements in the supplied collections


; ------------------------------------------------------------------------------
; Lazy Sequences

;; A lazy sequence (or lazy seq) is a seq 'whose members aren't computed until
;; you try to access them. Computing a seq's members is called realizing
;; the seq. Deferring the computation until the moment it's needed makes your
;; programs more efficient, and it has the surprising benefit of allowing
;; you to construct infinite sequences.'

; Demonstrating Lazy Seq Efficiency

;; Task:
;; It's the year 2022.
;; We are in Mendig, Germany attending the Rock am Ring music festival.
;; This show is for metalheads, but there's an imposter lurking about -
;; a polka enthusiast!
;; We need to find the polka fan in the crowd of 1 million hard rockers.

;; Here's how we might do it:
(def concert-audience
  {0 {:has-tattoos? false :plays-accordian? false :name "Angus Lars Anthrax"}
   1 {:has-tattoos? false :plays-accordian? false :name "Judas Jon Johannson"}
   2 {:has-tattoos? true :plays-accordian? true :name "Ernst Van Streuselmeyer"}
   3 {:has-tattoos? true :plays-accordian? false :name "Margot Gunnarschmitt"}})

(defn metal-fan-details
  [social-security-number]
  ;; (Thread/sleep 1000)
  (get concert-audience social-security-number))

(defn polka-enthusiast?
  [record]
  (and (:has-tattoos? record)
       (:plays-accordian? record)
       record))

(defn identify-polka-enthusiast
  [social-security-numbers]
  (first (filter polka-enthusiast?
                 (map metal-fan-details social-security-numbers))))

;; Let's find out how much time it might take to compute our code:
(time (metal-fan-details 0))
; => "Elapsed time: 1005.303124 msecs"
; => {:has-tattoos? false, :plays-accordian? false, :name "Angus Lars Anthrax"}

;; Here, we haven't technically accessed the mapped element, so this
;; should almost immediately return a value:
(time (def mapped-metal-fan-details (map metal-fan-details (range 0 1000000))))
; => "Elapsed time: 0.09592 msecs"
; => #'user/mapped-metal-fan-details

;; What just happened?
;; Well, 'range returns a lazy seq consisting of the integers from 0 to 999,999.
;; Then, map returns a lazy seq that is associated with the name
;; mapped-metal-fan-details. Because map didn't actually apply metal-fan-details
;; to any of the elements returned by range' the operation is near instant.

;; This illustrates a truth about lazy seqs, that they are made up of two parts:
;; (1) - the 'recipe' for how to realize the elements of a sequence, and
;; (2) - the elements that have been realized so far

;; In our example above, mapped-metal-fan-details is unrealized. Now, let's
;; realize mapped-metal-fan-details by accessing one of its members.

(time (first mapped-metal-fan-details))
; => "Elapsed time: 32131.590707 msecs"
; => {:has-tattoos? false, :plays-accordian? false, :name "Angus Lars Anthrax"}

;; Why did it take 32 seconds, rather than 1 second?
;; 'Clojure chunks its lazy sequences, which just means that whenever
;; Clojure has to realize an element, it preemptively realizes some of the next
;; elements as well. In this example, you wanted only the very first element
;; of mapped-metal-fan-details, but Clojure went ahead and prepared the next 31
;; as well. Clojure does this because it almost always results in better
;; performance.'


;; Now, let's call it again:
(time (first mapped-metal-fan-details))
; => "Elapsed time: 0.174635 msecs"
; => {:has-tattoos? false, :plays-accordian? false, :name "Angus Lars Anthrax"}

;; Wow! The second time we access the first element of mapped-metal-fan-details
;; it's almost immediate.

(time (identify-polka-enthusiast (range 0 1000000)))

; ------------------------------------------------------------------------------
; Infinite Sequences

;; Finally! - One of the best aspects of a lazy seq is our ability to work with
;; and construct infinite sequences. One of the ways we can construct these
;; infinite seqs is calling upon the 'repeat' function - creating a seq
;; where every member is the arg passed to it.

(concat (take 6 (repeat "oy")) ["'Cause I'm T.N.T. I'm dynamite!"])
; => ("oy" "oy" "oy" "oy" "oy" "oy" "'Cause I'm T.N.T. I'm dynamite!")

(take 3 (repeatedly (fn [] (rand-int 10))))
; => (9 2 7)

(defn even-numbers
  ([] (even-numbers 0))
  ([n] (cons n (lazy-seq (even-numbers (+ n 2))))))

(take 10 (even-numbers))
; => (0 2 4 6 8 10 12 14 16 18)

;; cons vs conj (I confuse these being so new, so I needed a way to memorize):

;; cons => 's' => adds x to the 's'tart of a seq
(cons 0 '(2 4 6))
; => (0 2 4 6)

;; conj => 'j' => 'j'oins the existing seq and some xs
(conj '(2 4 6) 8 10)
; => (10 8 2 4 6)

(conj [2 4 6] 8 10)
; => [2 4 6 8 10]

;; Note: Depending on the type, `conj` will con`join` the new xs to the seq
;; in varying order, as shown above

; ------------------------------------------------------------------------------
; The Collection Abstraction

;; Like the sequence abstraction we've been working with - there's another
;; closely related abstraction known as the 'collection abstraction.' All of
;; Clojure's core data structures take part in both abstractions.

;; Just as we understood the sequence collection to be about operating on
;; members individually, the collection abstraction is about operating on
;; the data structure as a whole.

;; Some examples of these 'whole collection' concerns include:
;; - Count (count coll)
;; - Empty? (empty? coll)
;; - Every? (every? pred coll)
;; - Into (into to from) (into to xform form)
;; - Conj (conj coll x) (conj coll x & xs)
;; ...et cetera...

(empty? [])
; => true

(empty? ["metal"])
; => false

;; Let's explore 'into' and 'conj' in more depth!

; into

;; Into is doing the work of transforming, because so many seq
;; functions return a seq rather than any original data structure,
;; into is a great utility function that transforms the return value
;; back into the original value

(map identity {:favorite-guitar "Flying V"})
; => ([:favorite-guitar "Flying V"])

(into {} (map identity {:favorite-guitar "Flying V"}))
; => {:favorite-guitar "Flying V"}

;; We don't have to work with just maps, we can use other structures, as well:

(map identity [:favorite-guitar-pedal :favorite-amplifier])
; => (:favorite-guitar-pedal :favorite-amplifier)

(into [] (map identity [:favorite-guitar-pedal :favorite-amplifier]))
; => [:favorite-guitar-pedal :favorite-amplifier]

;; Heck, we can even transform one data structure into another, entirely.
;; Here, we'll map over a vector returning a seq and then transform
;; this returned seq value into a set

(map identity [:favorite-guitar-lick :favorite-guitar-lick])

(into #{} (map identity [:favorite-guitar-lick :favorite-guitar-lick]))

;; We can also add to a non-empty data structure:

(into {:favorite-musical-scale "Phrygian"} [[:favorite-guitar-solo "Black Star"]])
; => {:favorite-musical-scale "Phrygian" :favorite-guitar-solo "Black Star"}

(into ["Gibson"] '("Fender" "Ibanez"))
; => ["Gibson" "Fender" "Ibanez"]

(into {:favorite-metal-city "Kopervik, Norway"} {:favorite-metal-pet "Dragon"
                                                 :favorite-metal-moment "1980 Winter Olympics"})
; => {:favorite-metal-city "Kopervik, Norway"
; =>  :favorite-metal-pet "Dragon"
; =>  :favorite-metal-moment "1980 Winter Olympics"}

; conj

;; We already looked at conj, but it's worth a second pass:

(conj [6] [6])
; => [6 [6]]

(into [6] [6])
; => [6 6]

(conj [6] 6)
; => [6 6]

;; Notice here that conj takes a scalar value, whereas into takes a collection

(conj [6] 6 6)
; => [6 6 6]

(conj {:time "the witching hour"} [:place "the halls of valhalla"])
; => {:place "the halls of valhalla" :time "the witching hour"}

;; There are obvious similarities here, so much so that we can write:
(defn my-conj
  [target & additions]
  (into target additions))

(my-conj [6] 6)
; => [6 6]

;; "This kind of pattern isn't that uncommon. You'll often see two functions
;; that do the same thing, except one takes a rest param ('conj') and one takes
;; a seqable data structure ('into')"

; ------------------------------------------------------------------------------
; Function Functions

;; Let's explore functions that accept functions and return functions as values!
;; This is very familiar to me, as a JavaScript dev, but I'm excited to see how
;; Clojure handles these day-to-day necessities.

; Apply (apply f args)
; (apply f x args) (apply f x y args) (apply f x y z args) (apply f a b c d & args)

;; "'apply' explodes a seqable data structure so it can be passed to a function
;; that expects a rest parameter."

;; Ex. max (max x) (max x y) (max x y & more)
(max 0 1 2)
; => 2

(max [0 1 2])
; => [0 1 2]

(apply max [0 1 2])
; => 2

;; As before, where we created conj using into - here, we can create into
;; by combining conj and apply

(defn my-into
  [target additions]
  (apply conj target additions))

(my-into [0] [1 2 3])
; => [0 1 2 3]

;; This is, again, equivalent to:
(conj [0] 1 2 3)

; Partial
; (partial f) (partial f arg1) (partial f arg1 arg2)
; (partial f arg1 arg2 arg3) (partial f arg1 arg2 arg3 & more)

;; "Takes a function f and fewer than the normal arguments to f, and returns
;; a fn that takes a variable number of additional args.
;; When called, the return function calls f with args + additional args.

;; The common example used to illustrate this across languages:
(def add-metal-num (partial + 666))
(add-metal-num 10)
; => 676
(add-metal-num 5)
; => 671

(def add-missing-band-members
  (partial conj ["Lead Singer" "Guitarist" "Bassist" "Drummer"]))

(add-missing-band-members "Keyboardist" "Technical Tambourine Artisan")
; => ["Lead Singer" "Guitarist" "Bassist" "Drummer" "Keyboardist" "Technical Tambourine Artisan"]

;; Partial function application can be tricky to visualize/understand, let's
;; break it down further by implementing it ourselves:

(defn my-partial
  [partialized-fn & args]
  (fn [& more-args]
    (apply partialized-fn (into args more-args))))

(def add10 (my-partial + 10))
(add10 3)
; => 13

;; What's happening behind the scenes can be shown like this:
(fn [& more-args]
  (apply + (into [10] more-args)))

;; We want to use partials when we are repeating "the same combination of
;; function and arguments in many different contexts."

(defn logger
  [log-level message]
  (condp = log-level
    :warn (clojure.string/lower-case message)
    :emergency (clojure.string/upper-case message)))

(def warn (partial logger :warn))

(warn "Are you ready to rock?")
; "are you ready to rock?"

;; These are identical: (warn "Are you ready to rock?") (logger :warn "Are you ready to rock?")

;; Sometimes, we can make use of simple function decorators to increase the
;; readabilty and clarity of our code.

; Complement (complement f)
; "Takes a fn f and returns a fn that takes the same args as f, with
; the same effects, if any, and returns the opposite truth value

;; Earlier, we identified the stray polka enthusiast in the crowd,
;; how would we find the metalheads - with and without complement?

;; Without...
(defn identify-metalheads
  [social-security-numbers]
  (filter #(not (polka-enthusiast? %))
          (map metal-fan-details social-security-numbers)))

;; Now, with...
(def not-polka-enthusiast? (complement polka-enthusiast?))
(defn identify-metalheads
  [social-security-numbers]
  (filter not-polka-enthusiast?
          (map metal-fan-details social-security-numbers)))

;; Here's how we might implement our own complement:

(defn my-complement
  [fun]
  (fn [& args]
    (not (apply fun args))))

(def my-pos? (my-complement neg?))

(my-pos? 1)
; => true

(my-pos? -1)
; => false

; ------------------------------------------------------------------------------
; A Data Analysis Program for the Big Government Entity

;; See directory 'Big-Government-Data-Collection" for project
