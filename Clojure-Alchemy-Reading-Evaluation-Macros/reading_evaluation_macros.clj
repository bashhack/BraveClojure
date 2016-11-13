
;;;; ---------------------------------------------------------------------------
;;;; -----------------   Reading, Evaluation, and Macros  ----------------------
;;;; ---------------------------------------------------------------------------

;;; Let's talk about `macros` - a tool Clojure provides which allows us to
;;; transform arbitrary expressions into valid Clojure code, essentially
;;; giving us a method of extending the language to fit our needs.

(defmacro backwards
  [form]
  (reverse form))

(backwards (" backwards" " am" "I" str))
; => "I am backwards"

;;; NOTE: Here, our macro allows us to successfully provide  an 'incorrect'
;;;       syntax for the `str` funtion form - our correct version would look
;;;       like this: `(str "I" " am" " backwards")`

;;; In effect, macros allow us to create rich DSLs (domain specific languages)

;;; Couching a discussion of the power of macros, etc., we'll explore the
;;; Clojure evaluation model in detail:  the reader, the evaluator, and the
;;; the macro expander

; ------------------------------------------------------------------------------
; An Overview of Clojure's Evaluation Model

;; Clojure (like all Lisps) has a unique evaluation model - a two-part system:

;; 1) first, it "reads" textual source code, creating Clojure data structures
;; 2) then, these data structures are "evaluated," wherein Clojure traverses
;;    the data data structures, performing actions like function application
;;    or var lookup depending on the structure

;; As an example:
(+ 1 2)
; => Clojure reads the text "(+ 1 2)"
; => Then, the result is a list data structure whose first element is a "+"
; => symbol, followed by the numbers 1 and 2
; => Then, this data structure is passed to Clojure's evaluator, which looks
; => up the function corresponding to the symbol and applies that function
; => to the arguments 1 and 2

;; Languages like Clojure that have this relationship between the source,
;; data, and evaluation are called "homoiconic." Homoiconic languages are
;; powerful because they allow us to think about our code as a set of
;; data structures that you can manipulate programmatically.

;; Programming languages require a compiler or interpreter for translating
;; the code we write into something else: be it assembly, another language, etc.
;; It is at this time that the compiler constructs an "abstract syntax tree".
;; This is a data strcture that represents our program, it is the input to
;; the "evaluator," the function(s) that traverses the tree to produce
;; the machine code, etc.

;; Where Clojure takes a detour from most languages is that the AST is
;; typically not accessible within the programming language, but in Clojure
;; instead of evaluating an AST that's inaccessible, Lisps evaluate native
;; data structures. The AST is structured using Clojure lists and the nodes
;; are CLojure values.

;; Lists are ideal for tree structures - the first element of a list is
;; treated as the root, and each subsequent element is treated as a branch.
;; To create a nested tree, you can just use nested lists:

(+ 1 2)
; =>     +
; =>   __|__
; =>  |     |
; =>  1     2

(+ 1 (* 6 7))
; =>     +
; =>   __|__
; =>  |     |
; =>  1     *
; =>      __|__
; =>     |     |
; =>     6     7

;; Here's what the model for Clojure's evaluation process looks like:

; => (+ 1 (* 6 7))

;; Text goes to the reader...

;; READER

;; ...which pops out a happy little Clojure list

; =>     +
; =>   __|__
; =>  |     |
; =>  1     *
; =>      __|__
; =>     |     |
; =>     6     7

;; This goes to the evaluator...

;; EVALUATOR

;; ...which returns a value

; => 43

;; NOTE: The evaluator doesn't care where its input comes from, as a result
;;       you can send your program's data structures directly to the Clojure
;;       evaluator with `eval`

(def addition-list (list + 1 2))
(eval addition-list)
; => 3

(eval (concat addition-list [10]))
; => 13

(eval (list 'def 'lucky-number (concat addition-list [10])))
; => #'user/lucky-number

lucky-number
; => 13

; =>     +
; =>   __|__
; =>  |  |  |
; =>  1  2  10
; => (concat addition-list [10])

; =>           def
; =>       _____|_____
; =>      |           |
; => lucky-number     +
; =>                __|__
; =>               |  |  |
; =>               1  2  10
; => (list 'def 'lucky-number (concat addition-list [10]))

; ------------------------------------------------------------------------------
; The Reader

;; The reader converts the textual source code in the file or the REPL
;; into Clojure data structures.

;; To understand reading, let's look at how text in the REPL is treated:

user=> (str "To understand what recursion is," " you must first understand recursion.")

;; Upon clicking 'Enter,' the text strings go to the reader, Clojure reads
;; the stream of characters and internally produces the corresponding
;; data structures. It then evaluates the data structures and prints the
;; textual representation of the result:

"To understand what recursion is, you must first understand recursion."

;; You can interact with the reader directly by using `read-string`:

(read-string "(+ 1 2)")
; => (+ 1 2)

(list? (read-string "(+ 1 2)"))
; => true

(conj (read-string "(+ 1 2)") :zagglewag)
; => (:zagglewag + 1 2)

;; NOTE: As a reminder, `conj` on a list appends to the beginning of the list,
;;       whereas `conj` on a vector appends to the end of the vector

;; The reader uses a set of rules for transforming text into data structures,
;; known as the "reader macro" - this allows us to represent data structures in
;; more compact ways because they take an abbreviated reader form and expand
;; it into a full form.

(read-string "'(a b c)")
; => (quote (a b c))

(read-string "@var")
; => (clojure.core/deref var)

(read-string "; ignore!\n(+ 1 2)")
; => (+ 1 2)

; ------------------------------------------------------------------------------
; The Evaluator

;; We can think of Clojure's evaluator as a function that takes a data structure
;; as an argument, processes the data structure using rules based on the data
;; structure's type, and returns a result

;; Whenever Clojure evaluates data structures that aren't a list or a symbol,
;; the result from the evaluator is the data structure itself:

true
; => true

false
; => false

{}
; => {}

:huzzah
; => :huzzah

;; Clojure uses "symbols" to name functions, macros, data, and evaluates them
;; by resolving them - a process by which Clojure traverses any bindings
;; you've created and then looks up the symbol's entry in a namespace mapping

;; We can use macros to manipulate the data structures that Clojure evaluates:

(read-string "(1 + 1)")
; => (1 + 1)

(eval (read-string "(1 + 1)"))
; => ClassCastException java.lang.Long cannot be cast to clojure.lang.IFn

(let [infix (read-string "(1 + 1)")]
  (list (second infix) (first infix) (last infix)))
; => (+ 1 1)

(eval
 (let [infix (read-string "(1 + 1)")]
   (list (second infix) (first infix) (last infix))))
; => 2

;; This is pretty cool, for sure - but a little awkward! Macros could help
;; tidy this up because they work much like functions:

(defmacro ignore-last-operand
  [function-call]
  (butlast function-call))

(ignore-last-operand (+ 1 2 10))
; => 3

(ignore-last-operand (+ 1 2 (println "look at me!!!")))
; => 3

;; The data structure returned by a function is not evaluated, but the data
;; structure returned by a macro is - the process of determining the return
;; value of a macro is called "macro expansion"

(macroexpand '(ignore-last-operand (+ 1 2 10)))
; => (+ 1 2)

(macroexpand '(ignore-last-operand (+ 1 2 (println "look at me!!!"))))
; => (+ 1 2)

;; Let's make a macro for infix:

(defmacro infix
  [infixed]
  (list (second infixed)
        (first infixed)
        (last infixed)))

(infix (1 + 2))
; => 3

;; We can imagine this whole process like an intermediate step added between
;; the reading and evaluation: a "macro expansion" phase

;; One of the macros we encounter frequently is the `->` macro:

;; Take the following:

(defn read-resource
  "Read a resource into a string"
  [path]
  (read-string (slurp (clojure.java.io/resource path))))

;; We understand this nested function call by finding the innermost form,
;; and working our way outward, right to left, to see how the result of
;; each function is passed to another function - something we do often
;; in Lisp but also something non-Lisp programmers might find strange

;; We can use the built-in macro `->` (also known as the "threaded" or
;; "stabby" macro), to transform the function into a more familiar form,
;; one that reads left to right, top to bottom:

(defn read-resource
  [path]
  (-> path
      clojure.java.io/resource
      slurp
      read-string))

;; Here, we've used our macro to perform "syntactic abstraction"
