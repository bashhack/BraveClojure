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

;; NOTE: Just like in JavaScript, function expressions (a function that returns a function)
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

;; Special forms, unlike function calls, 'don't always evaluate all of their operands'
;; You cannot use special forms as arguments; in general, special forms implement core Clojure
;; functionality - can be thought of as core utilities:
;;
;; - def (def symbol init?)
;; - if (if test then else?)
;; - do (do exprs*)
;; - let (let [bindings*] exprs*)
;; - quote (quote form)
;; - var (var symbol)
;; - fn (fn name? [params*] exprs*)
;; - fn (fn name? ([params*] exprs*)+)
;; - fn (fn name? [params*] condition-map? exprs*)
;; - fn (fn name? ([params*] condition-map? exprs*)+)
;; - loop (loop [bindings*] exprs*)
;; - recur (recur exprs*)
;; - throw (throw expr)
;; - try (try expr* catch-clause* finally-clause?)
;; - monitor-enter (monitor-enter x)
;; - monitor-exit (monitor-exit x)
;; - dot(.) => Java interop
;; - new => Java interop
;; - set! => Java interop
;; - :as => destructuring
;; - :or => destructuring
;; - :keys => destructuring


;; Example of special form usage:
(def metal true)
(if metal
  (println "Headbangers Ball was the greatest metal show ever made!")
  (println "The Real World Seattle was the worst metal show ever made!"))



;; Macros, though similar to special forms, in that they don't always evaluate their operands
;; and can't be passed as arguments. They are like functions: they take arguments
;; and return a value. They work on Clojure data structures, just like functions do.
;; They are evaluated in between the reader and the evaluator - so a macro can manipulate
;; the data structures that the reader spits out and transform them before they are
;; passed to the evaluator.

;; Structure of a Function:
(defn metal-roar
  "Returns a guttural Viking yell that summons the metal gods from atop their mighty Marshall stacks"
  [region]
  (str "" region "?! I can't hear you - are you re-e-e-e-ea-ea-ea-eaaaa-d-y !"
       (if (= region"Norway")
         "ååååååååååååååååååggghhhhhøøøøøøøøøøøø--ø-ø-ø-ø-ø-ø-ø-ø-ø!"
         "aaaaaaaaaaaaaaaaaaggghhhhhoooooooooooo--o-o-o-o-o-o-o-o-o!")))

(metal-roar "Norway")
(metal-roar "Brazil")
; => "Norway?! I can't hear you - are you re-e-e-e-ea-ea-ea-eaaaa-d-y! ååååååååååååååååååggghhhhhøøøøøøøøøøøø--ø-ø-ø-ø-ø-ø-ø-ø-ø!"
; => "Brazil?! I can't hear you - are you re-e-e-e-ea-ea-ea-eaaaa-d-y! aaaaaaaaaaaaaaaaaaggghhhhhoooooooooooo--o-o-o-o-o-o-o-o-o!"

;; The structure broken down:
;; 1) 'defn'
;; 2) function name
;; 3) A docstring describing the function (optional, but recommended)
;; 4) Parameters listed in brackets
;; 5) Function body

;; 3) (Continued) The docstring for a function can be viewed in the REPL,
;; using the following syntax: '(doc ' fn-name ')'

;; 4) (Continued) Clojure functions can be defined with zero or more parameters,
;; the values passed are called 'arguments' and the args can be of any type.
;; The number of parameters is the function's 'arity.'
;;
;; Functions arguments have the ability to be overloaded ('arity overloading'),
;; which allows us to provide default values for arguments.

;; Example of multi-arity:
(defn ikea-or-metal-band
  "A function that return eithers a metal band name or the name of an Ikea furniture piece"
  ([]
   (str "You won't believe how heavy the new flygtur metal band is out of Helsinki, they're called Bjursta"))
  ([name]
   (str "You won't believe how heavy the new joxtorp metal band is out of Helsinki, they're called " name))
  ([name genre]
   (str "You won't believe how heavy the new " genre " metal band is out of Helsinki, they're called " name)))

;; Example of arity-overloading:
(defn elitist-metal-fan
  "A function that returns a hipster talking about metal"
  ([name genre]
    (str "I only listen to limited edition vinyl pressings of bootleg cassette recordings of the " genre " metal band called " name))
  ([name]
   (elitist-metal-fan name "babymetal")))

(elitist-metal-fan "Oanvänd") ; => here, we are providing a value for name only, and genre param will be default
; => "I only listen to limited edition vinyl pressings of bootleg cassette recordings of the babymetal metal band called Oanvänd"

(elitist-metal-fan "Grönkulla" "dagstorp") ; => here, we are providing name and genre...and should not see 'babymetal'
; => "I only listen to limited edition vinyl pressings of bootleg cassette recordings of the dagstorp metal band called Grönkulla"

;; Example of variable-arity using a 'rest parameter' ('put the rest of these args in a list with the given name'), we use '&' for the rest param
(defn metal-greeting
  [metal-fan]
  (str "Oy! Great corpse paint, " metal-fan ", really great homage to Hellhammer!!!"))

(defn death-metal-bouncer
  [& metal-fans]
  (map metal-greeting metal-fans))

(death-metal-bouncer "Billy" "Joan" "Bobby-Lee" "Rufus")
; => ("Oy! Great corpse paint, Billy, really great homage to Hellhammer!!!"
; =>  "Oy! Great corpse paint, Joan, really great homage to Hellhammer!!!"
; =>  "Oy! Great corpse paint, Bobby-Lee, really great homage to Hellhammer!!!"
; =>  "Oy! Great corpse paint, Rufus, really great homage to Hellhammer!!!")

;; More variable-arity function examples:
(defn favorite-metal
  [name & bands]
  (str "Hi, I'm " name ", and here are a few of my favorite metal bands: "
       (clojure.string/join ", " bands)))

(favorite-metal "Marc" "Leprous" "Emperor" "Haken" "Ulver")
; => "Hi, I'm Marc, here are a few of my favorite metal bands: Leprous, Emperor, Haken, Ulver"

;; Destructing:
;; NOTE (to self): Essentially, destructuring allows us to bind names to values in a collection
(defn get-first-metal-item
  [[first-item]]
  first-item)

(get-first-metal-item ["Wintersun" "Aluminum" "Iron" "Zinc"])
; => "Wintersun"

(defn get-series-of-metal-items
  [[first-item second-item & other-items]]
  (println (str "The first element in the metal collection is: " first-item))
  (println (str "The second element in the metal collection is: " second-item))
  (println (str "The rest of the items in the collection are: "
                (clojure.string/join ", " other-items))))

(get-series-of-metal-items ["Pantera" "Opeth" "copper" "tin" "steel" "brass"])
; => The first element in the metal collection is: Pantera
; => The second element in the metal collection is: Opeth
; => The rest of the items in the collection are: Meshuggah, Testament, Celtic Frost, Mastodon
; => nil

;; We can also destructure maps, in the following way:
(defn components-of-a-good-show
  [{stage-props :stage-props lighting-effects :lighting-effects mascot :mascot}]
  (println (str "A good metal show has " stage-props " and " lighting-effects " and a " mascot)))

(components-of-a-good-show {:stage-props "goats" :lighting-effects "flames" :mascot "giant skeleton"})

;; We can use a more consise syntax by using the ':keys' keyword (preferred)
(defn components-of-a-good-show
  [{:keys [stage-props lighting-effects mascot]}]
  (println (str "A good metal show has " stage-props " and " lighting-effects " and a " mascot)))

(components-of-a-good-show {:stage-props "Viking ship" :lighting-effects "red candles" :mascot "inflatable devil"})

;; Using the ':as' keyword, we retain the original map arg
(defn components-of-a-good-show
  [{:keys [stage-props lighting-effects mascot] :as show-rider}]
  (println (str "A good metal show has " stage-props " and " lighting-effects " and a " mascot))

  ;; Send map to function that generates tour manager's shopping list
  (generate-shopping-list show-rider))

(components-of-a-good-show {:stage-props "coffins" :lighting-effects "explosions" :mascot "rabid bat"})

; ------------------------------------------------------------------------------
; Anonymous Functions

;; Basic Forms (there are two primary methods of creating anonymous functions):

;; Form 1:
((fn [x] (* x 6 6)) 6) ; => 216

(map (fn [singer] (str "Hi, I'm " singer " and I'm here to rock!"))
     ["Ronnie James Dio" "Ihsahn" "Ian Gillan" "James Hetfield"])
; => ("Hi, I'm Ronnie James Dio and I'm here to rock!"
; =>  "Hi, I'm Ihsahn and I'm here to rock!"
; =>  "Hi, I'm Ian Gillan and I'm here to rock!"
; =>  "Hi, I'm James Hetfield and I'm here to rock!")

(def generate-metal-number
  (fn
    [x]
    (if (= x 666)
      (println (str "Yes! The most metal number is: " x))
      (println (str "The most metal number is 666, not " x)))))

(generate-metal-number 333)
; => The most metal number is 666, not 333
; => nil

;; But, Clojure offers us a more concise syntax to use:

;; Form 2:
(#(* % 6 6) 6) ; => 216

(map #(str "Welcome to the " % ", we got fun and games")
     ["jungle" "Nordic fjords" "high-alpine forest" "Australian bush"])

; => ("Welcome to the jungle, we got fun and games"
; =>  "Welcome to the Nordic fjords, we got fun and games"
; =>  "Welcome to the high-alpine forest, we got fun and games"
; =>  "Welcome to the Australian bush, we got fun and games")

(#(str %1 " and " %2 " go together like peanut butter and jelly") "metal" "punk")
; = > "metal and punk go together like peanut butter and jelly"

(#(identity %&) 666 "666" {:num 666})
; => (666 "666" {:num 666})

;; NOTE (to self): If the anonymous function is going to be longer and more complex,
;; use '(fn)' form otherwise use '#()' form

; ------------------------------------------------------------------------------
; Returning Functions

;; Returned functions are closures (just like in JavaScript!), meaning
;; they can access all the variables that were in scope when the
;; function was created

; Example:
(defn metal-poem-generator
  "Generates a heavy metal poem"
  [rose-color violet-color]
  #(str "Roses are " rose-color " violets are " violet-color " I love " % " and Turkish doom metal"))

(def love-poem (metal-poem-generator "black" "black"))

(love-poem "you")
