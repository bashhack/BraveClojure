;;;; Introduction to Clojure Data Structres

;; NOTE: Everything is immutable!!!

; ------------------------------------------------------------------------------
; Numbers

;; Examples
666 ; => 666 => int
6.66 ; => 6.66 => float
66/6 ; => 11 => ratio

;; NOTE: Clojure gets the full support for JVM primitive values here...
;; (http://clojure.org/reference/data_structures#Numbers)

; ------------------------------------------------------------------------------
; Strings

;; Examples
"Hello, Cleveland - are you ready to rock?!"
;;'Not a string' ; NOTE: Strings require us to use double-quotes

;; NOTE: Also, Clojure does NOT have string interpolation! The only method for
;; for concatenation is via the 'str' function

; ------------------------------------------------------------------------------
; Maps

;; NOTE (to self): Maps are most similar to dictionaries, or objects, wherein we
;; have key and value pairs

{} ; => empty map

{:last-name "Mustaine"
 :band "Megadeth"}

{"eddie" eruption} ; => 'eddie' is a key as a string, associated with the function 'eruption'

{:van-halen {:guitar "Eddie" :vocals "David" :drums "Alex" :bass "Anthony"}} ; => we can nest maps

;; NOTE: Map values can be of ANY type!!

;; To create a map, we can use the built-in 'hash-map' function from the std lib
(hash-map :key1 "value1" :key2 "value2")
; => {:key1 "value1" :key2 "value2"}

;; To get values from a map, we can use the built-in 'get' function from the std lib
(get {:key1 "value1" :key2 "value2"} :key2)
; => "value2"

(get {:van-hagar "not-really-van-halen" :van-halen {:guitar "Eddie" :vocals "David" :drums "Alex" :bass "Anthony"}} :van-halen)
; => {:guitar "Eddie" :vocals "David" :drums "Alex" :bass "Anthony"}

;; NOTE: If get doesn't find the key, the function will return 'nil' - or, whatever value assigned as default
(get {:mamas-and-papas :bee-gees :crosby-still-nash-young} :metal)
; => nil
(get {:brown :red :green :yellow :pink} :black "The answer is none. None more black.")
; => "The answer is none. None more black."

(get-in {:nested 0 :map {:a-value "something"}} [:map :a-value])
; => "something"

({:who-is-bad-to-the-bone "George Thoroughgood"} :who-is-bad-to-the-bone)
; => "George Thoroughgood

; ------------------------------------------------------------------------------
; Keywords

;; NOTE (to self): I've been overthinking 'keywords' in Clojure,
;; wondering if they were sometimes acting as symbols - however,
;; it appears that they really are just signifiers for map keys

;; Per the Clojure documentation, keywords are:

;; "symbolic identifiers that evaluate to themselves, providing very fast equality tests"

;; Whereas, symbols are:

;; "identifiers that are normally used to refer to something else. They can be used
;; in programs to refer to function parameters, let bindings, class names and global vars..."

;; Further, we might think of using keywords as lightwieght "constant strings" for the keys of a hash-map
;; or the dispatch values of a multimethod

;; Symbols, then, are generally used to name variables and functions and it's less common to
;; manipulate them as objects directyl except in macros.

;; Examples
:doom
:black
:death
:speed
:progressive
:symphonic

;; As we saw with the built-in 'get' method, a keyword can be used to lookup a value in a data structure
(get {:bat "meatloaf" :out "mashed potatoes" :of "green peas" :hell "ketchup"} :bat)
; => "meatloaf"

;; However, we can write this more succinctly because we're using keywords:
(:bat {:bat "meatloaf" :out "mashed potatoes" :of "green peas" :hell "ketchup"})
  ; => "meatloaf"

;; We could also provide a default value, just like we did with the 'get' method (on line 57)
(:ozzy {:bat "meatloaf" :out "mashed potatoes" :of "green peas" :hell "ketchup"} "Wrong bat")
; => "Wrong bat"

;; NOTE (to self): Try to utilizie the keyword as a function method over the 'get,' where appropriate!

; ------------------------------------------------------------------------------
; Vectors

;; A vector is similar to an array, in that it's a 0-indexed data structure

;; As with all other Clojure data structures, vectors, too, are immutable and persistent

[6 6 6]

(get [6 6 6] 12) ; => nil (no element at index 12)
(get [6 6 6] 0) ; => 6 (element found at 0th index)
(get-in [6 "six" {:most-metal-number {:num 666}}] [2 :most-metal-number :num])
; => {:most-metal-number {:num 666}}

;; NOTE: In the above, I combined index-based positioning with the 'get-in' method
;; which I discovered was applicable to both maps and vectors (that is, any nested
;; associative data structure). In reviewing the docs, it seems that the 'list'
;; data structure would not work, because it is not associative. Using the nested
;; properties of a vector, I supplied my argument as a vector containing '2' to grab the
;; element at the 2-index, and then supplied two keywords (':most-metal-number' and
;; ':num') to get the inner value of the nested hash-map at the 2-index

;; To create a vector, use the 'vector' function, passing n-values
(vector "like" "a" "rainbow" "in" "the")
; => ["like" "a" "rainbow" "in" "the"]

;; To add additional elements to a vector (IMPORTANT: added to the end!!!), use 'conj' function
(conj ["like" "a" "rainbow" "in" "the"] "dark")
; => [ "like" "a" "rainbow" "in" "the" "dark"]

; ------------------------------------------------------------------------------
; Lists

;; Lists are similar to vectors, however their performance characterists differ, such that:

;; 1) You cannot use the 'get' method to retrieve elements, you instead use 'nth'

'(6 6 6) ; => the single-quote here is significant when creating a list literal
(nth '(6 6 6) 2)
; => 6 (at 2-index)

;; NOTE: Using nth is slower than 'get' because Clojure traverses all n elements to get to target nth value

;; 2) Vectors are great for quick, indexed random access, lists provide linear access via sequential scan

;; 3) Lists are easier to modify at head and/or tail and are less expensive to modify than vectors at the
;; expense of the O(n) time required vs a vectors effectively constant time of O(1)

;; 4) Like vectors, lists can be operated on by the 'conj' function, but elements are inserted at the start

(clojure.string/join (conj '("yes I'm a cannonball") "Well I'm an animal, "))
; => "Well I'm an animal, yes I'm a cannonball"


;; NOTE (to self):
;; The question: When do I use a list and when do I use a vector?
;; The answer:
;; If I need to easily add items to the beginning of the sequence, or if I am writing a macro
;; I should use a list. Otherwise, I should be using a vector.


; ------------------------------------------------------------------------------
; Sets

;; Sets are collections of unique values. Clojure has two kinds of sets:
;; hash sets (more common)
;; sorted sets

;; Examples
#{"I'm made of metal, my circuits gleam" 666 :judas-priest}
(hash-set 6 6 6)
; => #{6}
;; NOTE: Sets are unique values, so if an attempt to create a set with repeating values
;; were done, a set with only a single instance of the repeated value would be created

(conj #{:most-metal-number :second-most-metal-number} :most-metal-number)
; => #{:most-metal-number :second-most-metal-number}

;; To create a set, we can use the 'set' function:
(set [6 3 9]) ; => or, using a list literal (set '(6 3 9))
; => #{6 3 9}

;; An important tool in working with sets is checking membership, we can do this
;; with 'get' or using the keyword (just like we were able to do with maps, lists and vectors)

;; Maps
(get {:rock "For those about to..." :on "...my mark, get set, rock!"} :on)
(:on {:rock "For those about to..." :on "...my mark, get set, rock!"})
({:rock "For those about to..." :on "...my mark, get set, rock!"} :on)
; => all return "...my mark, get set, rock!"

;; Vectors
(get [{:rock "for those about to..."}] 0) ; => {:rock "for those about to..."}
([6 666 6] 1) ; => 666
(nth [666 666 6] 2) ; => 6

;; Lists
(nth '(666 666 6) 0) ; => 666

;; While we could use the methods above on our set, there's some questionable
;; return values possible ... looking at you, 'nil'...so it's best to use the
;; 'contains?' function which will like 'empty?' or 'nil?' return a boolean or a keyword

(contains? #{:slayer :anthrax :janes-addiction} :mandolin) ; => false, obviously, because mandolins aren't metal enough \m/

(:amon-amarth #{:amon-amarth :glittertind :korpiklaani :tyr}) ; => :amon-amarth

(get #{:punk :metal} :metal) ; => :metal
