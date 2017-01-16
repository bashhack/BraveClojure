
;;;; ---------------------------------------------------------------------------
;;;; --------------- Creating and Extending Abstractions with ------------------
;;;; ------------------ Multimethods, Protocols, and Records -------------------
;;;; ---------------------------------------------------------------------------

;; In programming, when we talk about "abstractions" we are referring to a
;; collection of operations, and "data types" implement abstractions.
;; The more a programming language lets one think and write in terms of
;; abstractions, the more productive/expressive you will be.

;; Though Clojure is written in terms of abstractions, we can also create and
;; implement our very own abstractions via multimethods, protocols, and records.

; -----------------------------------------------------------------------------
; Polymorphism

;; "Polymorphism" is when we associate an operation name with more than one
;; algorithm. For example, the algorithm for performing `conj` on a list is
;; different from the one for vectors, but we use the same name to illustrate
;; that they implement the same concept - "add an element to this data
;; structure."

; -----------------------------------------------------------------------------
; Multimethods

;; Multimethods give you a direct, flexible way to introduce polymorphism
;; into your code. We can associate a name with multiple implementations
;; by setting up a "dispatching function," which creates "dispatching values"
;; that are used to determine which "method" to use.

(ns were-creatures)
(defmulti full-moon-behavior (fn [were-creature] (:were-type were-creature)))
(defmethod full-moon-behavior :wolf
  [were-creature]
  (str (:name were-creature) " will howl and murder"))
(defmethod full-moon-behavior :simmons
  [were-creature]
  (str (:name were-creature) " will encourage people and sweat to the oldies"))

(full-moon-behavior {:were-type :wolf
                     :name "Rachel from next door"})
; => "Rachel from next door will howl and murder"
(full-moon-behavior {:name "Andy the baker"
                     :were-type :simmons})
; => "Andy the baker will encourage people and sweat to the oldies"

;; In the above example, `(fn [were-creature] (:were-type were-creature))` is
;; the dispatching function. It is immediately run on the arguments, the result
;; then passed as the dispatching value. The value is evaluated in order to
;; determine which method definition to use.

;; We could use `nil` as the dispatch value, as well:

(defmethod full-moon-behavior nil
  [were-creature]
  (str (:name were-creature) " will stay at home and eat ice cream"))

(full-moon-behavior {:were-type nil
                     :name "Martin the nurse"})
; => "Martin the nurse will stay at home and eat ice cream"

;; We can also use a default method to use if no other methods match by using
;; `:default` as the dispatch value:

(defmethod full-moon-behavior :default
  [were-creature]
  (str (:name were-creature) " will stay up all night fantasy footballing"))

(full-moon-behavior {:were-type :office-worker
                     :name "Jimmy from sales"})
; => "Jimmy from sales will stay up all night fantasy footballing"

;; NOTE: We can always add new methods for multimethods by extending via
;;       new dispatch values.

(ns random-namespace
  (:require [were-creatures]))
(defmethod were-creatures/full-moon-behavior :bill-murray
  [were-creature]
  (str (:name were-creature) " will be the most likeable celebrity"))

(were-creatures/full-moon-behavior {:name "Laura the intern"
                                    :were-type :bill-murray})
; => "Laura the intern will be the most likeable celebrity"

;; Multimethods can return arbitrary values for any or all of their args:

(ns user)
(defmulti types (fn [x y] [(class x) (class y)]))
(defmethod types [java.lang.String java.lang.String]
  [x y]
  "Two strings!")

(types "String 1" "String 2")
; => "Two strings!"

; -----------------------------------------------------------------------------
; Protocols

;; "Protocols" are optimized for type dispatch - and, as such, they are more
;; efficient than multimethods.

;; A multimethod is just one polymorphic operation, whereas a protocol is a
;; collection of one or more polymorphic operations.
;; Protocol methods are dispatched based on the type of the first arg, as shown:

(ns data-psychology)
(defprotocol Psychodynamics
  "Plumb the depths of your data types"
  (thoughts [x] "The data type's innermost thoughts")
  (feelings-about [x] [x y] "Feelings about self or other"))

;; Above, `thoughts` and `feelings-about` are our "method signatures."

;; Defining a protocol as we have done above is defining an abstraction without
;; defining how the abstraction is implemented. In order to fix this in-between
;; state, we need to implement the `Psychodynamics` protocol:

(extend-type java.lang.String
  Psychodynamics
  (thoughts [x] (str x " thinks, 'Truly the character defines the data type'"))
  (feelings-about
    ([x] (str x " is longing for a simpler way of life"))
    ([x y] (str x " is envious of " y "'s simpler way of life"))))

(thoughts "blorb")
; => "blorb thinks, 'Truly the character defines the data type'"

(feelings-about "schmorb")
; => "schmorb is longing for a simpler way of life"

(feelings-about "schmorb" 2)
; => "schmorb is envious of 2's simpler way of life"

;; Since every type in Java (and hence, Clojure) is a descendant of
;; `java.lang.Object` we can provide default implementations like:

(extend-type java.lang.Object
  Psychodynamics
  (thoughts [x] "Maybe the Internet is just a vector for toxoplasmosis")
  (feelings-about
    ([x] "meh")
    ([x y] (str "meh about " y))))

(thoughts 3)
; => "Maybe the Internet is just a vector for toxoplasmosis"

(feelings-about 3)
; => "meh"

(feelings-about 3 "blorb")
; => "meh about blorb"

;; This is all fine and well, but we can simplify the sytax instead of multiple
;; calls to `extend-type`:

(extend-protocol Psychodynamics
  java.lang.String
  (thoughts [x] "Truly, the character defines the data type")
  (feelings-about
    ([x] "longing for a simpler way of life")
    ([x y] (str "envious of " y "'s simpler way of life")))

  java.lang.Object
  (thoughts [x] "Maybe the Internet is just a vector for toxoplasmosis")
  (feelings-about
    ([x] "meh")
    ([x y] (str "meh about " y))))

; -----------------------------------------------------------------------------
; Records

;; Records are custom, maplike data types (um...Haskell, anyone??).

;; Like maps, records associate keys with values, are immutable, and you can
;; look up their values the same way - however - they are different in that we
;; specify "fields" for records.

;; We use fields as slots for data, and using them is like specifying which keys
;; a data structure should have. Records also differ from maps in that we can
;; extend them to implement protocols.

(ns were-records)
(defrecord WereWolf [name title]) ; the fields here are "name" and "title"

;; We can create instances of this record in three ways:

(WereWolf. "David" "London Tourist")
(->WereWolf "Jacob" "Lead Shirt Discarder")
(map->WereWolf {:name "Lucian" :title "CEO of Melodrama"})

;; We can import records like this:

(ns monster-mash
  (:import [were_records WereWolf])) ; NOTE: the underscore here vs dash
(WereWolf. "David" "London Tourist")

;; To lookup record values, we could do the following:

(def jacob (-> WereWolf "Jacob" "Lead Shirt Discarder"))
(.name jacob)
; => "Jacob"

(:name jacob)
; => "Jacob"

(get jacob :name)
; => "Jacob"

;; We can use any function that we might use on a map on a record, as well:

(assoc jacob :title "Lead Third Wheel")
; => #were_records.WereWolf{:name "Jacob", :title "Lead Third Wheel"}

(dissoc jacob :title)
; => {:name "Jacob"} <- that's not a were_records.WereWolf

;; Notice that in the `dissoc` example, we just get back a map
;; as a result, not a record.

;; IMPORTANT:
;; (1) accessing map values is slower than accessing record values
;; (2) when creating a new record type, we can extend it to implement
;; a protocol, similar to how we extended a type using `extend-type` before

(defprotocol WereCreature
  (full-moon-behavior [x]))

(defrecord WereWolf [name title]
  WereCreature
  (full-moon-behavior [x]
    (str name " will howl and murder")))

(full-moon-behavior (map->WereWolf {:name "Lucian" :title "CEO of Melodrama"}))
; => "Lucian will howl and murder"

;; Now, the big question lingering for us is:
;; You should consider using records if you find yourself creating maps with the
;; same fields over and over. This should indicate that that set of data
;; represents information in your app domain, and your code will communicate
;; its purpose better if you give a name based on the concept you're trying to
;; model. Further, record access is more performant than map access, so your
;; program will benefit from being more efficient. Additionally, if you want to
;; use protocols, you'll need to create a record.

;; For more info on working with abstractions and data types, explore `deftype`,
;; `reify` and `proxy` here: http://clojure.org/datatypes/
