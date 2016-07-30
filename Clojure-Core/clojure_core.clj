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
  (Thread/sleep 1000)
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

(time (identify-polka-enthusiast (range 0 1000000)))
