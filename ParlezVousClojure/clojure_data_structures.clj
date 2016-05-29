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

; -------------------------------------------------------------------------------
; Keywords
