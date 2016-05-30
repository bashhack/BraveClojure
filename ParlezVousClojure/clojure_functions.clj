; ------------------------------------------------------------------------------
; Functions
; ------------------------------------------------------------------------------

; ------------------------------------------------------------------------------
; Calling Functions

;; We've seen many examples of calling functions, like these...
(+ 6 6 6)
(- 6 6 6)
(* 6 6 6)
;; ...
(first [6 6 6])
(last [6 6 6])
(get [6 6 6] 0)
;; ... or any other function we've covered, of course...

(or + -) ; =>  #object[clojure.core$_PLUS_ 0x2d3f2884 "clojure.core$_PLUS_@2d3f2884"]
((or + -) 6 6 6) ; => 18
((and (= 6 6) +) 6 6 6) ; => 18
((first [+ 0]) 6 6 6) ; => 18

(6 6 6)
; => ClassCastException java.lang.Long cannot be cast to clojure.lang.IFn  user/eval1322 (form-init3568529770152210505.clj:1)
("metal" 6 6 6)
; => ClassCastException java.lang.String cannot be cast to clojure.lang.IFn  user/eval1316 (form-init3568529770152210505.clj:1)

;; Just like in JavaScript, function expressions (a function that returns a function)
;; can take any number of arguments, including other functions. Functions that
;; can take functions as arguments or return another function are called
;; higher-order functions. The presence of higher-order functions in a programming
;; language is indicative of the language supporting first-class functions
;; (that is we can treat functions as values)

;; Examples
(inc 665) ; => 666

(map inc [5 5 5]) ; => (6 6 6)

(+ (inc 599) (/ 396 (- 9 3))) ; => 666

; ------------------------------------------------------------------------------
; Function calls, Macro calls, and Special Forms








;
