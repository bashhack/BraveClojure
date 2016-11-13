
;;;; ---------------------------------------------------------------------------
;;;; ---------------------------  Writing Macros  ------------------------------
;;;; ---------------------------------------------------------------------------

;; Our goal in this chapter should be to understand macros in detail, learning
;; the tools we use to write them: `quote`, `syntax quote` `unquote`, `unquote
;; splicing`, and `gensym`

; ------------------------------------------------------------------------------
; Macros Are Essential

;; Don't be afraid of macros - they are used in Clojure even for fundamental
;; operations, they're not just for esoteric use cases, they are a valid and
;; practical tool in our Clojure toolbelt. As we learn more about them,
;; we'll discover how useful macros are extending the language itself to fit
;; the needs of our particular problem domain.

;; Example of built-in macro `when`:

(when boolean-expression
  expression-1
  expression-2
  expression-3
  ...
  expression-x)

(macroexpand '(when boolean-expression
                expression-1
                expression-2
                expression-3
                ...
                expression-x))
; => (if boolean-expression
; =>   (do expression-1
; =>       expression-2
; =>       expression-3))

; ------------------------------------------------------------------------------
; Anatomy of a Macro

;; Macros have a name, an optional doc string, an arg list, and a body

;; NOTE: The body will almost always return a list, and this makes sense because
;;       macros are a way of transforming a data structure into a form Clojure
;;       can evaluate, and Clojure uses lists to represent function calls,
;;       special form calls, and macro calls.

;; Let's go back to our `infix` macro:

(defmacro infix
  "Use this macro when you want to use infix notation rather than Polish notation"
  [infixed]
  (list (second infixed) (first infixed) (last infixed)))

(infix (1 + 1))
; => 2

(macroexpand '(infix (1 + 1)))
; => (+ 1 1)

;; We can also use argument destructing in macro definitions:

(defmacro infix-2
  [[operand1 op operand2]]
  (list op operand1 operand2))

;; We can also create multiple arity macros, in fact the fundamental built-in
;; Boolean operations `and` and `or` are actually macros:

(defmacro and
  "Evaluates exprs one at a time, from left to right. If a form returns
  logical false (nil or false), and returns that value and doesn't evaluate
  any of the other expressions, otherwise it returns the value of the last expr.
  (and) returns true."
  {:added "1.0"}
  ([] true)
  ([x] x)
  ([x & next]
   '(let [and# ~x]
      (if and# (and ~@next) and#))))

; ------------------------------------------------------------------------------
; Building Lists for Evaluation

;; An important gotcha in writing macros is that as need to be careful about
;; the difference between a "symbol" and its "value"

(defmacro my-incorrect-macro
  [expression]
  (list let [result expression]
        (list println result)
        result))

;; The result here is actually an error, because the macro body tries to get
;; the value that the symbol let refers to, whereas we want to return the symbol
;; `let` itself.

;; We're also tyring to get the value of `result`, which is unbound, and we're
;; also trying to get the value of `println` instead of returning its symbol

;; Here's our fixed version:

(defmacro my-correct-macro
  [expression]
  (list 'let ['result expression]
        (list 'println 'result)
        'result))

;; Here, we're quoting each symbol we want to use as a symbol by prefixing
;; it with a single quote character, `'`

(+ 1 2)
; => 3

(quote (+ 1 2))
; => (+ 1 2)

'(+ 1 2)
; => (+ 1 2)

;; Here's the built-in `when` macro (NOTE: Notice that it implements quotes):

(defmacro when
  "Evaluates test. If logical true, evaluates body in an implicit do."
  {:added "1.0"}
  [test & body]
  (list 'if test (cons 'do body)))

(macroexpand '(when (the-cows-come :home)
                (call me :pappy)
                (slap me :silly)))
; => (if (the-cows-come :home)
; =>   (do (call me :pappy)
; =>       (slap me :silly)))

;; Here's another built-in macro `unless`:

(defmacro unless
  "Inverted 'if'"
  [test & branches]
  (conj (reverse branches) test 'if))

(macroexpand '(unless (done-been slapped? me)
                      (slap me :silly)
                      (say "I reckon that'll learn me")))
; => (if (done-been slapped? me)
; =>   (say "I reckon that'll learn me")
; =>   (slap me :silly))

; ------------------------------------------------------------------------------
; Syntax Quoting

;; Syntax quoting returns unevaluated data structures, similar to normal quoting.
;; There are differences, though:

;; 1) syntax quoting returns the fully qualified symbols (i.e., with namespaces)

;; ...with normal quoting:
'+
; => +

;; (normal quoting with a namespace...)

'clojure.core/+
; => clojure.core/+

;; ...now, with syntax quoting:

`+
; => clojure.core/+

'(+ 1 2)
; => (+ 1 2)

`(+ 1 2)
; => (clojure.core/+ 1 2)

;; 2) syntax quoting allows you to unquote forms using `~`

`(+ 1 ~(inc 1))
; => (clojure/core/+ 1 2)

`(+ 1 (inc 1))
; => (clojure/core+ 1 (clojure.core/inc 1))

;; We can think of syntax quoting almost like string interpolation, and this
;; can help us write clean, concise code. Here, let's compare a quoted and
;; syntax-quoted example:

(list '+ 1 (inc 1))
; => (+ 1 2)

`(+ 1 ~(inc 1))
; => (clojure.core/+ 1 2)

; ------------------------------------------------------------------------------
; Using Syntax Quoting in a Macro

;; Let's rewrite the following macro using syntax quoting:

(defmacro code-critic
  "Phrases are courtesy of Hermes Conrad from Futurama"
  [bad good]
  (list 'do
        (list 'println
              "Great squid of Madrid, this is bad code:"
              (list 'quote bad))
        (list 'println
              "Sweet gorilla of Manila, this is good code:"
              (list 'quote good))))

(code-critic (1 + 1) (+ 1 1))
; => Great squid of Madrid, this is bad code: (1 + 1)
; => Sweet gorilla of Manila, this is good code: (+ 1 1)

(defmacro code-critic
  "Phrases are courtesy of Hermes Conrad from Futurama"
  [bad good]
  `(do (println "Great squid of Madrid, this is bad code:"
                (quote ~bad))
       (println "Sweet gorilla of Manila, this is good code:"
                (quote ~good))))

;; NOTE: Remember that most of the time, your macros will be returning lists,
;; and we can build up that list to be returned by using either the `list`
;; function or syntax-quoting - the latter being a clean, concise way
;; to present our code for easy visual parsing

;; NOTE: Also remember, to return multiple forms for Clojure to evaluate, make
;; sure to wrap them in a `do`

; ------------------------------------------------------------------------------
; Refactoring a Macro and Unquote Splicing

(defn criticize-code
  [criticism code]
  `(println ~criticism (quote ~code)))

(defmacro code-critic
  [bad good]
  `(do
     ~(criticize-code
       "Cursed bacteria of Liberia, this is bad code:"
       bad)
     ~(criticize-code
       "Sweet sacred boa of Western and Eastern Samoa, this is good code:"
       good)))

(code-critic (1 + 1) (+ 1 1))

;; This is okay, but there's two nearly identical calles to `criticize-code`...
;; how can we clean that up a bit?

(defmacro code-critic
  [bad good]
  `(do ~(map #(apply criticize-code %)
             [["Great squid of Liberia, this is bad code:" bad]
              ["Sweet gorilla of Manila, this is good code:" good]])))

;; Houston, we've got a problem! This code isn't running correctly - `map`
;; returns a list, and so this `code-critic` function returns a list of
;; `println` expressions rather than the result of each `println` call.

;; Let's use unquote splicing `~@` to fix this and return the expected result:

`(+ ~(list 1 2 3))
; => (clojure.core/+ (1 2 3))

`(+ ~@(list 1 2 3))
; => (clojure.core/+ 1 2 3)

;; Unquote splicing unwraps a seqable data structure, placing its contents
;; directly within the enclosing syntax-quoted data structure.

(defmacro code-critic
  [{:keys [good bad]}]
  `(do ~@(map #(apply criticize-code %)
              [["Swwet lion of Zion, this is bad code:" bad]
               ["Great cow of Moscow, this is good code:" good]])))

(code-critic (1 + 1) (+ 1 1))
; => Sweet lion of Zion, this is bad code: (1 + 1)
; => Great cow of Moscow, this is good code: (+ 1 1)

; ------------------------------------------------------------------------------
; Things to Watch Out For

;; 1) Variable Capture - this can occur when a macro introduces a binding that
;;    overwrites an existing binding, for example:

(def message "Good job!")

(defmacro with-mischief
  [& stuff-to-do]
  (concat (list 'let ['message "Oh, big deal!"])
          stuff-to-do))

(with-mischief
  (println "Here's how I feel about that thing you did: " message))
; => Here's how I feel about that thing you did: Oh, big deal!

;; NOTE: In the above example, we're really dealing with lexical scoping issues

;; Had we used syntax quoting (which is designed to prevent us from accidentally
;; capturing variables within macros), we would have seen an exception:

(def message "Good job!")

(defmacro with-mischief
  [& stuff-to-do]
  `(let [message "Oh, big deal!"]
     ~@stuff-to-do))

(with-mischief
  (println "Here's how I feel about that thing you did: " message))
; => Exception: Can't let qualified name: user/message

;; Okay, so what do we do when we want to introduce `let` bindings in our macro?
;; We use the `gensym` function, which produces unique symbols on
;; each successive call:

(gensym)
; => G__1250

(gensym)
; => G__1253

(gensym 'message)
; => message1256

(gensym 'message)
; => message1259

(def message "Good job!")

(defmacro without-mischief
  [& stuff-to-do]
  (let [macro-message (gensym 'message)]
    `(let [~macro-message "Oh, big deal!"]
       ~@stuff-to-do
       (println "I still need to say: " ~macro-message))))

(without-mischief
 (println "Here's how I feel about that thing you did: " message))
; => Here's how I feel about that thing you did: Good job!
; => I still need to say: Oh, big deal!

;; This example avoids variable capture by using `gensym` to create
;; a new, unique symbol that then gets bound to `macro-message`. Within
;; the syntax-quoted `let` expression, `macro-message` is unquoted,
;; resolving to the gensym'd symbol. This gensym'd symbol is distinct
;; from any symbols within `stuff-to-do`.

;; This pattern is actually so common, we can use an auto-gensym.
;; Auto-gensyms are more concise and convenient ways to use gensyms:

`(blarg# blarg#)
; => (blarg__1260__auto__ blarg__1260__auto__)

`(let [name# "Larry Potter"] name#)
; => (clojure.core/let [name__1263__auto__ "Larry Potter"] name__1263__auto__)

;; 2) Double Evaluation - this occurs when a form passed to a macro as an
;;    argument gets evaluated more than once

(defmacro report
  [to-try]
  `(if ~to-try
     (println (quote ~to-try) "was successful:" ~to-try)
     (println (quote ~to-try) "was not successful:" ~to-try)))

(report (do (Thread/sleep 1000) (+ 1 1)))

;; Oops - this is going to be bad! When that macro gets evaluated, it becomes:

(if (do (Thread/sleep 1000) (+ 1 1))
  (println '(do (Thread/sleep 1000) (+ 1 1))
           "was successful:"
           (do (Thread/sleep 1000) (+ 1 1)))
  (println '(do (Thread/sleep 1000) (+ 1 1))
           "was not successful:"
           (do (Thread/sleep 1000) (+ 1 1))))

;; We can avoid this duplication by using gensyms (specifically, auto-gensyms):

(defmacro report
  [to-try]
  `(let [result# ~to-try]
     (if result#
       (println (quote ~to-try) "was successful:" result#)
       (println (quote ~to-try) "was not successful:" result#))))

;; 3) Macros All the Way Down - macros are powerful, but we must be aware that
;;    you can end up in the position of having to write more and more macros
;;    in order to get anything done - this is because macro expansion happens
;;    before evaluation

;; Let's imagine that instead of multiple calls to report:

(report (= 1 1))
; => (= 1 1) was successful: true

(report (= 1 2))
; => (= 1 2) was not successful: false

;; Let's transform this to an iterative function:

(doseq [code ['(= 1 1) '(= 1 2)]]
  (report code))
; => code was successful: (= 1 1)
; => code was successful: (= 1 2)

; => (if
; =>     code
; =>   (clojure.core/println 'code "was successful:" code)
; =>   (clojure.core/println 'code "was not successful:" code))

(defmacro doseq-macro
  [macroname & args]
  `(do
     ~@(map (fn [arg] (list macroname arg)) args)))

(doseq-macro report (= 1 1) (= 1 2))
; => (= 1 1) was successful: true
; => (= 1 2) was not successful: false

;; NOTE: If you were to find yourself in this situation, it's time to
;;       rethink your approach! You'll be stuck writing more and more
;;       macros, sometimes this is alright - but ... it should be the
;;       exception to the rule.

; ------------------------------------------------------------------------------
; Brews for the Brave and True

;; Validation Functions

;; In this example, we'll be dealing with validating records/maps of email
;; addresses:

(def order-details
  {:name "Yngwie Malmsteen"
   :email "shredder_ferrarigmail.com"})

;; Obviously, this email address is missing a "@" symbol, so our code should
;; catch this and provide a warning:

(validate order-details order-details-validations)
; => {:email ["Your email address doesn't look like an email address."]}

;; Let's create a scaffold for our validations:

(def order-details-validations
  {:name
   ["Please enter a name" not-empty]

   :email
   ["Please enter an email address" not-empty

    "Your email address doesn't look like an email address"
    #(or (empty? %) (re-seq #"@" %))]})

;; Now, we need to write a `validate` function, which will be comprised
;; of two functions: (1) a function that applies validations to a single
;; field and (2) another to accumulate those error messages into a final
;; map of error messages.

(defn error-messages-for
  "Return a seq of error messages"
  [to-validate message-validator-pairs]
  (map first (filter #(not ((second %) to-validate))
                     (partition 2 message-validator-pairs))))

;; To break this down:
;; 1) first argument `to-validate` is the field we want to validate
;; 2) second argument `message-validator-pairs` should be a seq with
;;    an even number of elements
;; 3) this seq then gets grouped into pairs with the `partition` function,
;;    the first element should be an error message, and the second should
;;    be a function (mirroring the pairs in `order-details-validations`)
;; 4) the `error-messages-for` function works then by filtering out all
;;    error message and validation pairs where the validation function
;;    returns `true` when applied to `to-validate`

(error-messages-for "" ["Please enter a name" not-empty])
; => ("Please enter a name")

;; Now for the `validate` function:

(defn validate
  "Returns a mpa with a vector of errors for each key"
  [to-validate validations]
  (reduce (fn [errors validation]
            ;; errors => {}
            ;; validation =>
            ;;   {:name
            ;;    ["Please enter a name" not-empty]
            ;;    :email
            ;;    ["Please enter an email address" not-empty
            ;;     "Your email address doesn't look like an email address"
            ;;     #(or (empty? %) (re-seq #"@" %))]}
            (let [[fieldname validation-check-groups] validation
                  ;; fieldname => :name
                  ;; validation-check-groups => ["Please enter a name" not-empty]
                  ;; [:name ["Please enter a name" not-empty]]
                  value (get to-validate fieldname)
                  ;; value => (get {:name "Yngwie Malmsteen" :email "shredder_ferrarigmail.com"} :name)
                  ;; value => "Yngwie Malmsteen"
                  error-messages (error-messages-for value validation-check-groups)]
                  ;; (error-messages-for "Yngwie Malmsteen" ["Please enter a name" not-empty])
                  ;; nil
              (if (empty? error-messages)
                errors
                ;; => returns last value of {}, could have content, of course
                (assoc errors fieldname error-messages))))
                ;; => {:name "Please enter a name"}
          {}
          validations))

(validate order-details order-details-validations)
; => {:email ("Your email address doesn't look like an email address")}

;; NOTE TO SELF: Heck yes! Working through this actually made tons of sense -
;; able to draw parallels to JS reduction code - just thrilled really!

;; An `if-valid` Function

;; Now that we have a `validate` function working we can do something like:

(let [errors (validate order-details order-details-validation)]
  (if (empty? errors)
    (println :success)
    (println :failure errors)))

;; We might attempt to create an abstraction around this pattern by writing:

(defn if-valid
  [record validations success-code failure-code]
  (let [errors (validate order-details order-details-validation)]
    (if (empty? errors)
      success-code
      failure-code)))

;; However, this won't work! `success-code` and `failure-code` will be evaluated
;; each time...a macro will work much better here:

(if-valid order-details order-details-validations errors
          (render :success)
          (render :failure errors))

(defmacro if-valid
  "Handle validation more concisely"
  [to-validate validations errors-name & then-else]
  `(let [~errors-name (validate ~to-validate ~validations)]
     (if (empty? ~errors-name)
       ~@then-else)))

(macroexpand
 '(if-valid order-details order-details-validations my-error-name
            (println :success)
            (println :failure my-error-name)))

(let*
    [my-error-name (user/validate order-details order-details-validations)]
  (if (clojure.core/empty? my-error-name)
    (println :success)
    (println :failure my-error-name)))

; ------------------------------------------------------------------------------
; Summary

;; - We studied macros, forms which are like functions: have arguments,
;;   a docstring, and a body
;; - They can utilize destructuring, rest args, and can be recursive
;; - Macros will almost always return lists
;; - Sometimes you'll use `list` and `seq` for simple macros, most of the time
;;   you'll use the syntax quote, which allows for safe macros
;; - We must not forget that when writing macros that there is a fundamental
;;   difference between symbols and values - and because macros are expanded
;;   before code is evaluated, macros therefore don't have access to the results
;;   of evaluation
;; - There are some gotchas to be aware of, including double evaluation and
;;   variable capture - however, we can work around this by the use of `let`
;;   expressions and gensyms
