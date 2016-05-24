;;;; Introduction to Clojure Syntax

; -----------------------------------------------------------------------------
; Forms/Expressions


;; Data Structures ;; Operations

;; Examples of literal expressions (all just forms, FYI)
666; => a long (evil) number

"Finland has more metals bands per capita than any other country, pretty hardcore, right?\n" ; => a string

["just" "like" "witches" "at" "black" "masses"] ; => a vector of strings

;; Doing meaningful things with forms, of course, requires us to perform operations
;; Basic syntax of s-exp would be: (operator operand1 operand2 ... operandn)

;; Examples of operations:
(+ 6 6 6) ; => 18

(str "The Horsemen are drawing nearer, on leather steeds they ride") ; => "The Horsemen are drawing nearer, on leather steeds they ride"


; ------------------------------------------------------------------------------
; Logic Flow


;; ------------------------ If -------------------------------------------------
;;; Follows the format:
;; (if boolean-form
;;   then-form
;;   optional-else-form)

(if true
  "Yngwie 'The Force from the North' Malmsteen is better than Kirk Hammett"
  "No, Kirk 'The Sandman' Hammett is better!")
; => "Yngwie 'The Force from the North' Malmsteen is better than Kirk Hammett"

(if false
 "Yngwie 'The Force from the North' Malmsteen is better than Kirk Hammett"
 "No, Kirk 'The Sandman' Hammett is better!")
; => "No, Kirk 'The Sandman' Hammett is better!"

(if false
  "By Odin's El- uh, I mean axe!")
; => nil
;; In the above example, we have omitted the else block, so a boolean form that evals to false returns nil

;; -------------------------- Do -----------------------------------------------
;;; Follows the format:
;; (if boolean-form
;;   (do (operator operand1 operand2 ... operandn)
;;       "some literal or another s-expression")
;;   (do (operator operand1 operand2 ... operandn)
;;       "some literal or another s-expression"))

(if true
  (do (println "On the sixth day, Thor created the Gibson Les Paul")
      "Huzzah!")
  (do (str "There was no rest on the seventh day, for there was metal to shred.")
      (println "\m/")))
; => On the sixth day, Thor created the Gibson Les Paul
; => "Huzzah!"

;; ------------------------- When ----------------------------------------------
;;; Follows the format:
;; (when boolean-form
;;   then-form)
;; NOTE: 'When' is like 'if' and 'do' combined, but without an 'else', use when
;; you always want to return 'nil' on false eval

(when true
  (println "Thunderstruck!")
  "You've been thunderstruck")
; => Thunderstruck!
; => "You've been thunderstruck"

;; ------ Nil / true / false / Truthiness / Equality / Boolean Expression ------
;;; Nil
;; nil represents 'no value'
;; nil? is an operator/func which tests if operand is 'no value'
(nil? 1) ; => false

(nil? nil) ; => true

;;; true/false
;; NOTE: 'nil' and 'false' represent falsey values, all other values
;; are logically truthy

(if 666
  "Mmm, the taste of pigeon in the morning really wakes me up...")
; => "Mmm, the taste of pigeon in the morning really wakes me up..."

(if nil
  "Yeah, I always bring earplugs when Dokken plays!"
  "Nah, I like to feel the power of Dokken unobstructed!")
; => "Nah, I like to feel the power of Dokken unobstructed!"

;;; Equality
;; '=' is the sole equality operator in Clojure (Side Note: As a JS/Python dev, thank god!)

(= 1 1) ; => true

(= nil nil) ; => true

(= 1 2) ; => false

;;; or/and
;; 'or' returns the first truthy value or the last value
;; 'and' returns the first falsey value or, if no values are falsey, the last truthy value

(or false nil :uli_jon_roth :ritchie_blackmore) ; => :uli_jon_roth

(or (= 0 1) (= "yes" "no")) ; => false (in this case, it was the last value

(or nil) ; => here again, the last value is returned, nil

(and :rolling_stones :led_zeppelin) ; => :led_zeppelin (the last truthy value)

(and :poison false :winger :van_halen) ; => false (first falsey value)

; -----------------------------------------------------------------------------
; def


;; The 'def' keyword is a binding mechanism, that is, it binds a name to a value(s)
;; NOTE: In Clojure, we use the term 'bind' to signify that this is essentially
;; an action which is creating a constant - as opposed to, say, JS var assignment
;; where the concept of mutability/state is inherent (i.e., I can assign var thing = 1
;; at the top of a file, but end up assigning thing to some other value(s) later,
;; resulting in a situation where I might have console.log(thing); => "banana").

;; Bad
;; Why: Introduces state/mutability
(def axe_of_choice :fender)
(def which_to_play "I feel the need to shred, what do I choose? ")
(if (= axe_of_choice :fender)
  (def which_to_play (str which_to_play "This is metal - take that surf guitar and smash it!"))
  (def which_to_play (str which_to_play "Yeah, that's right - now get out there and give em hell!")))
; => "I feel the need to shred, what do I choose? Yeah, that's right now get out there and give em hell!"

;; Good
;; Why: Is functional, can be reasoned about without much trouble, gets expected result
(def which_to_play
  [axe_of_choice]
  (str "I feel the need to shred, what do I choose? "
       (if (= axe_of_choice :fender)
         "This is metal - take that surf guitar and smash it!"
         "Yeah, that's right - now get out there and give em hell!")))

(which_to_play :gibson)
; => "I feel the need to shred, what do I choose? Yeah, that's right - now get out there and give em hell!"

