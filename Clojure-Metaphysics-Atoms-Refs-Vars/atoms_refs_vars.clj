
;;;; ---------------------------------------------------------------------------
;;;; ------------------ Concurrent and Parallel Programming --------------------
;;;; ---------------------------------------------------------------------------

;; Rich Hickey has designed Clojure to address the problems of mutable state,
;; in fact, Clojure embodies a clear conception of state that makes it
;; inherently safer for concurrency than most other programming languages.

;; Why?

;; To answer this, we must explore Clojure's underlying metaphysics, which we'll
;; do in comparison to that of object-oriented (OOP) languages. Learning about
;; the philosophy behind Clojure will help us handle the remaining concurrency
;; tools, the `atom` `ref` and `var` reference types (Clojure has one additional
;; reference type, `agents` which we don't cover). Each of these allow us to
;; safely perform state-modifying operations concurrently.

;; To illustrate the difference between Clojure and OO languages, we'll model
;; a zombie, a cuddle zombie.

;; Let's code it up in Ruby:

class CuddleZombie
  # attr_accessor is just a shorthand way for creating getters and
  # setters for the listed instance variables
  attr_accessor :cuddle_hunger_level, :percent_deteriorated

  def initialize (cuddle_hunger_level = 1, percent_deteriorated = 0)
    self.cuddle_hunger_level = cuddle_hunger_level
    self.percent_deteriorated = percent_deteriorated
  end
end

fred = CuddleZombie.new(2, 3)
fred.cuddle_hunger_level # => 2
fred.percent_deteriorated # => 3

fred.cuddle_hunger_level = 3
fred.cuddle_hunger_level # => 3

if fred.percent_deteriorated >= 50
  Thread.new { database_logger.log(fred.cuddle_hunger_level) }
end

;; The problem here is that another thread could change `fred` before the write
;; actually takes place.

;; Further, in order for us to change the `cuddle_hunger_level` and
;; `percent_deteriorated` simultaneously, you have to be extra careful!
;; It's possible that the value of `fred` is being operated upon/viewed
;; in an inconsistent state, because another thread might `read` the `fred`
;; object in between the two changes:

fred.cuddle_hunger_level = fred.cuddle_hunger_level + 1
# At this time, another thread could read fred's attributes and
# "perceive" fred in an inconsistent state unless you use a mutex
fred.percent_deteriorated = fred.percent_deteriorated + 1

;; Of course, this is another version of the mutual exclusion problem. In OOP
;; languages, we can work around this problem with a mutex, ensuring that
;; only one thread can access a resource at a time.

;; Objects are never truly stable, yet we build our programs with them as
;; the basis - this conforms to our intuitive sense of the world, in a sense.

;; Object in OOP also are the ones doing things, they act on each other,
;; serving as the engines of state change.

; ------------------------------------------------------------------------------
; Clojure Metaphysics

;; In Clojure, we would say that we never meet the same cuddle zombie twice.
;; Our metaphysics as Clojurists and functional programmers informs us that
;; a cuddle zombie is not a discrete thing that exists in the world apart
;; from its mutations, it's really just a succession of values.

;; We use this term, "value," frequently in Clojure. Values are "atomic," in the
;; sense that they form a single irreducible unit in a larger system. They are
;; indivisble, unchanging, stable entities.

;; Numbers are values, for example, as it wouldn't make sense for a number like
;; `15` to mutate into some other number. When we add and subtract, we do not
;; change `15`, we just wind up with another number.

;; Clojure's data structures are also values because they're immutable -
;; remember, when you perform an action like `assoc` on a map, we are not
;; losing the original map, we derive a new map.

;; We simply apply a process to a value which produces a new value.

;; In OOP, we think of "identity" as something inherent to a changing
;; object - but in Clojure, identity is something we impose on a series
;; of unchanging values produced by a process over time. We use "names"
;; to designate identities, when we reference our object `fred` we
;; are referring to a series of individual states f1, f2, f3, and so on.
;; From this viewpoint, there's no such thing as mutable state. There
;; is only state as the value of an identity at a point in time.

;; That is, change only ever occurs when (1) a process generates a new value
;; and/or (2) we choose to associate the identity with the new value.

;; To handle this sort of change, Clojure uses "reference types." These help
;; us manage identities in Clojure and through using them, you can name
;; an identity and retrieve its state. The simplest of these is the "atom."

; ------------------------------------------------------------------------------
; Atoms

;; The "atom" reference allows us to endow a succession of related values with
;; an identity, here's how to create one:

(def fred (atom {:cuddle-hunger-level 0
                 :percent-deteriorated 0}))

@fred
; => {:cuddle-hunger-level 0, :percent-deteriorated 0}

;; Unlike futures, delays, and promises, dereferencing an atom will never block.

;; To log the state of our zombie (free of the danger we saw in the Ruby example
;; where the object data could change while trying to log it), we would write:

(let [zombie-state @fred]
  (if (>= (:percent-deteriorated zombie-state) 50)
    (future (println (:cuddle-hunger-level zombie-state)))))

;; To update an atom so that it refers to a new state, we use `swap!`:

(swap! atom f)
(swap! atom f x)
(swap! atom f x y)
(swap! atom f x y & args)

(swap! fred
       (fn [current-state]
         (merge-with + current-state {:cuddle-hunger-level 1})))

@fred
; => {:cuddle-hunger-level 1, :percent-deteriorated 0}

(swap! fred
       (fn [current-state]
         (merge-with + current-state {:cuddle-hunger-level 1
                                      :percent-deteriorated 1})))

@fred
; => {:cudde-hunger-level 2, :percent-deteriorated 1}

(defn increase-cuddle-hunger-level
  [zombie-state increase-by]
  (merge-with + zombie-state {:cuddle-hunger-level increase-by}))

(increase-cuddle-hunger-level @fred 10)
; => {:cuddle-hunger-level 12, :percent-deteriorated 1}
;; NOTE: Not actually updating `fred`, because it is not
;;       using `swap!` - we're just making a normal function
;;       call which returns a result

(swap! fred increase-cuddle-hunger-level 10)
; => {:cuddle-hunger-level 12, :percent-deteriorated 1}
;; NOTE: Here, we used `swap!`, so `fred` was updated

;; This is all fine and good, but we could express this all in
;; terms of a built-in Clojure function: `update-in` ... a
;; function which takes three params: a coll, a vector identifying
;; the value to update, and a function to update that value

(update-in {:a {:b 3}} [:a :b] inc)
; => {:a {:b 4}}

(update-in {:a {:b 3}} [:a :b] + 10)
; => {:a {:b 13}}

(swap! fred update-in [:cuddle-hunger-level] + 10)
; => {:cuddle-hunger-level 22, :percent-deteriorated 1}

;; Using atoms, we can retain a past state, you can dereference
;; to retrieve State 1, update the atom -- in effect, creating
;; State 2, and still use State 1:

(let [num (atom 1)
      s1 @num]
  (swap! num inc)
  (println "State 1:" s1)
  (println "Current status:" @num))
; => State 1: 1
; => Current state: 2

;; `swap!` implements "compare-and-set" semantics, meaning that
;; if two separate threads call a `swap!` function, there is
;; no risk of one of the increments getting lost the way it did
;; in the Ruby example because behind the scenes `swap!`:

;; 1) reads the current state of the atom
;; 2) then applies the update function to that state
;; 3) next, it checks whether the value it read in step 1 is identical
;;    to the atom's current value
;; 4) if it is, then `swap!` updates the atom to refer to the result
;;    of step 2
;; 5) if it isn't, then `swap!` retries, going through the
;;    process again with step 1

;; Further, the atom updates `swap!` triggers happen synchronously,
;; that is, they block the thread.

;; Sometimes, though, you may want to update an atom without
;; checking its current value - for this, we can use the `reset!`
;; function:

(reset! fred {:cuddle-hunger-level 0
              :percent-deteriorated 0})

; ------------------------------------------------------------------------------
; Watches and Validators

;; Watches allow us to check changes in our reference types, while
;; validators allow us to restrict what states are allowable - both
;; are simply functions.

; -------
; Watches
; -------

(defn shuffle-speed
  [zombie]
  (* (:cuddle-hunger-level zombie)
     (- 100 (:percent-deteriorated zombie))))

;; Now, we'll create a watch function (these take four args: a key
;; used for reporting, the atom being watched, the state of the atom
;; before its update, and the state of the atom after the update

(defn shuffle-alert
  [key watched old-state new-state]
  (let [sph (shuffle-speed new-state)]
    (if (> sph 5000)
      (do
        (println "Run, you fool!")
        (println "The zombie's SPH is now " sph)
        (println "This message brought to you, courtesy of " key))
      (do
        (println "All's well with " key)
        (println "Cuddle hunger: " (:cuddle-hunger-level new-state))
        (println "Percent deteriorated: " (:percent-deteriorated new-state))
        (println "SPH: " sph)))))

;; We attach this new function to our atom `fred` with `add-watch`.
;; The general form of `add-watch` is:

(add-watch ref key watch-fn)

(reset! fred {:cuddle-hunger-level 22
              :percent-deteriorated 2})
(add-watch fred :fred-shuffle-alert shuffle-alert)
(swap! fred update-in [:percent-deteriorated] + 1)
; => All's well with :fred-shuffle-alert
; => Cuddle hunger: 22
; => Percent deteriorated: 3
; => SPH: 2134

(swap! fred update-in [:cuddle-hunger-level] + 30)
; => Run, you fool!
; => The zombie's SPH is now 5044
; => This message brought to you, courtesy of :fred-shuffle-alert

; ----------
; Validators
; ----------

;; Validators let us specify what states are allowable for a reference.

;; We could use a validator to ensure that `:percent-deteriorated` is
;; between 0 and 100

(defn percent-deteriorated-validator
  [{:keys [percent-deteriorated]}]
  (and (>= percent-deteriorated 0)
       (<= percent-deteriorated 100)))

;; If the validator fails by returning `false` or throwing an exception,
;; the reference won't change to point to the new value.

;; We can attach a validator during atom creation:

(def bobby
  (atom
   {:cuddle-hunger-level 0 :percent-deteriorated 0}
   :validator percent-deteriorated-validator))
(swap! bobby update-in [:percent-deteriorated] + 200)
; => This throws "Invalid reference state"

;; We can get a more descriptive, custom error message:

(defn percent-deteriorated-validator
  [{:keys [percent-deteriorated]}]
  (or (and (>= percent-deteriorated 0)
           (<= percent-deteriorated 100))
      (throw (IllegalStateException. "That's not mathy!"))))
(def bobby
  (atom
   {:cuddle-hunger-level 0 :percent-deteriorated 0}
   :validator percent-deteriorated-validator))
(swap! bobby update-in [:percent-deteriorated] + 200)
; This throws "IllegalStateException: That's not mathy!"

;; Atoms are great for managing the state of independent identities.
;; Sometimes, we need to express that an event should update the
;; state of more than one identity simultaneously - for this,
;; `refs` are the perfect tool!

; ------------------------------------------------------------------------------
; Modeling Sock Transfers

;; Refs have three primary features:
;; 1) They are "atomic," meaning that all refs are updated or none of them are
;; 2) They are "consistent," meaning that the refs always appear to have valid
;;    states. In this example, a sock will always belong to a dryer or a gnome,
;;    but never both or neither.
;; 3) They are "isolated," meaning that transactions behave as if they executed
;;    serially; if two threads are simultaneously running transactions that alter
;;    the same ref, one transaction will retry. This is similar to the
;;    compare-and-set semantics of atoms.

;; NOTE: These are the A, C, and I in the ACID properites of database
;;       transactions. `Refs` give us the same concurrency safety as
;;       database transactions, only with in-memory data.

;; To implement this behavior, Clojure uses "software transactional memory"
;; (STM).

;; Let's create some sock and gnome generators:

(def sock-varieties
  #{"darned" "argyle" "wool" "horsehair" "mulleted"
    "passive-aggressive" "striped" "polka-dotted"
    "athletic" "business" "power" "invisible" "gollumed"})

(defn sock-count
  [sock-variety count]
  {:variety sock-variety
   :count count})

(defn generate-sock-gnome
  "Create an initial sock gnome state with no socks"
  [name]
  {:name name
   :socks #{}})

;; Now, we create our actual refs:

(def sock-gnome (ref (generate-sock-gnome "Barumpharumph")))
(def dryer (ref {:name "LG 1337"
                 :socks (set (map #(sock-count % 2) sock-varieties))}))

;; Just like dereferencing `atoms` we can dereference `refs`:

(:socks @dryer)
; => #{{:variety "passive-aggressive", :count 2} {:variety "power", :count 2}
; =>   {:variety "athletic", :count 2} {:variety "business", :count 2}
; =>   {:variety "argyle", :count 2} {:variety "horsehair", :count 2}
; =>   {:variety "gollumed", :count 2} {:variety "darned", :count 2}
; =>   {:variety "polka-dotted", :count 2} {:variety "wool", :count 2}
; =>   {:variety "mulleted", :count 2} {:variety "striped", :count 2}
; =>   {:variety "invisible", :count 2}}

;; We are ready to perform the transfer, but we need to modify the `sock-gnome`
;; ref to show that it has gained a sock and modify the `dryer` ref
;; to show that it's lost a sock. To modify `refs` we use `alter` and you
;; must use `alter` within a transaction.

;; NOTE: `dosync` inities a transaction, all transaction operations must
;;       go in its body.

(defn steal-sock
  [gnome dryer]
  (dosync
   (when-let [pair (some #(if (= (:count %) 2) %) (:socks @dryer))]
     (let [updated-count (sock-count (:variety pair) 1)]
       (alter gnome update-in [:socks] conj updated-count)
       (alter dryer update-in [:socks] disj pair)
       (alter dryer update-in [:socks] conj updated-count)))))

;; NOTE: `disj` - disjoin - returns a new set of the same (hashed/sorted)
;;       type, that does not contain key(s)
;;       (disj set)
;;       (disj set key)
;;       (disj set key & ks)
;;       (disj #{1 2 3}) ; disjoin nothing
;;       ; => #{1 2 3}
;;       (disj #{1 2 3} 2) ; disjoin 2
;;       ; => #{1 3}
;;       (disj #{1 2 3} 4) ; disjoin non-existent item
;;       ; => #{1 2 3}
;;       (disj #{1 2 3} 1 3) ; disjoin several items at once
;;       ; => #{2}

(steal-sock sock-gnome dryer)

(:socks @sock-gnome)
; => #{{:variety "passive-aggressive", :count 1}}

(defn similar-socks
  [target-sock sock-set]
  (filter #(= (:variety %) (:variety target-sock)) sock-set))

(similar-socks (first (:socks @sock-gnome)) (:socks @dryer))
; => ({:variety "passive-aggressive", :count 1})

;; A couple quick points about this...when you `alter` a ref, the change
;; isn't immediately visible outside of the current transaction. This is
;; what lets you `alter` on the `dryer` twice within a transaction without
;; worrying about whether or not `dryer` will be read in an inconsistent
;; state. If you `alter` a ref and then `deref` it in the same transaction,
;; the `deref` will return the new state.

;; Here's an example of this in-transaction state:
(def counter (ref 0))
(future
  (dosync
   (alter counter inc)
   (println @counter)
   (Thread/sleep 500)
   (alter counter inc)
   (println @counter)))
(Thread/sleep 250)
(println @counter)
; => 1 -- future creates new thread for the transaction, counter incremented
; => 0 -- on the main thread, a wait of 250ms, and counter value printed as 0
;      -- because the value has never changed yet - the transaction creates
;      -- its own scope and the previous increment action has never
;      -- occured outside of it
; => 2 -- after the wait of 500ms on transaction's thread, counter incremented

; ---------
; `commute`
; ---------

;; `commute` allows you to update a ref's state within a transaction, just like
;; `alter` - however, its behavior at commit time is completely different.

;; `alter`
;; -------
;; 1) Reach outside the transaction and read the ref's current state
;; 2) Compare the current state to the state the ref started with within
;;    the transaction
;; 3) If the two differ, make the transaction retry
;; 4) Otherwise, commit the altered ref state

;; `commute`
;; ---------
;; 1) Reach outside the transaction and read the ref's current state
;; 2) Run the `commute` function again using the current state
;; 3) Commit the result

;; Because the transaction retry does not in `commute` this can help improve
;; performance, but one should only use `commute` when it is certain that
;; it's not possible for your refs to end up in an invalid state.

;; `commute`
;; (commute ref fun & args)
;; Must be called in transaction -
;; Sets the in-transaction value of ref to:
;; (apply fun in-transaction-value-of-ref args)
;; At commit point, sets the value of ref to be:
;; (apply fun most-recently-committed-value-of-ref args)

;; "safe" `commute`:
(defn sleep-print-update
  [sleep-time thread-name update-fn]
  (fn [state]
    (Thread/sleep sleep-time)
    (println (str thread-name ": " state))
    (update-in state)))

(def counter (ref 0))

(future
  (dosync
   (commute counter (sleep-print-update 100 "Thread A" inc))))
(future
  (dosync
   (commute counter (sleep-print-update 150 "Thread B" inc))))
;; Thread A: 0 | 100ms
;; Thread B: 0 | 150ms
;; Thread A: 0 | 200ms
;; Thread B: 1 | 300ms

;; "unsafe" `commute`:
(def receiver-a (ref #{}))
(def receiver-b (ref #{}))
(def giver (ref #{1}))
(do
  (future
    (dosync
     (let [gift (first @giver)]
       (Thread/sleep 10)
       (commute receiver-a conj gift)
       (commute giver disj gift))))
  (future
    (dosync
     (let [gift (first @giver)]
       (Thread/sleep 50)
       (commute receiver-b conj gift)
       (commute giver disj gift)))))
@receiver-a
; => #{1}

@receiver-b
; => #{1}

@giver
; => #{}

;; The `1` ws given to both `receiver-a` and `receiver-b`, and you've ended up
;; with two instances of `1`, which isn't valid for this program.
;; What's different about this program is that the functions that are applied,
;; essentially #(conj % gift) and #(disj % gift), are derived from the state of
;; `giver`. Once `giver` changes, the derived functions produce an invalid state
;; but `commute` doesn't care that the resulting state is invalid and commits the
;; result anyway.

; ------------------------------------------------------------------------------
; Vars

;; We've already worked with `vars` but to review: `vars` are associations
;; between symbols and objects, new vars are created with `def`

;; Although `vars` are not used to manage state in the way `atoms` and `refs`,
;; we can use dynamic binding and the ability to alter their roots when
;; dealing with issues of concurrency

; ---------------
; Dynamic Binding
; ---------------
;; Usually we think of using `def` like defining a constant, but we can
;; create a dynamic `var` whose binding can be changed.
;; Dynamic bindings are useful for creating a global name that should refer
;; to different values in different contexts.

(def ^:dynamic *notification-address* "dobby@elf.org")

;; To temporarily change the the value of dynamic vars by using `binding`:
(binding [*notification-address* "test@elf.org"]
  *notification-address*)
; => "test@elf.org"

;; You can also stack bindings (like with `let`):
(binding [*notification-address* "test1@elf.org"]
  (println *notification-address*)
  (binding [*notification-address* "test2@elf.org"]
    (println *notification-address*))
  (println *notification-address*))
;; test1@elf.org
;; test2@elf.org
;; test1@elf.org

;; Here's a real-world use case:
(defn notify
  [message]
  (str "TO: " *notification-address* "\n"
       "MESSAGE: " message))
(notify "I fell.")
; => "TO: dobby@elf.org\nMESSAGE: I fell."

(binding [*notification-address* "test@elf.org"]
  (notify "test!"))
; => "TO: test@elf.org\nMESSAGE: test!"

;; Here, we could have just modified `notify` to take an email address,
;; of course, so why would we want to use dynamic vars instead?

;; Dynamic vars are most often applicable to name a resource that one or
;; more functions target. In the previous example, we can imagine
;; the email address as a resource that we write to. This type of action
;; is so common that Clojure provides us with a number of built-in functions
;; like `*out*` - a function that represents the standard output for
;; print operations.

(binding [*out* (clojure.java.io/writer "print-output")]
  (println "A man who carries a cat by the tail learns
something he can learn in no other way.
-- Mark Twain"))

(slurp "print-output")
; => A man who carries a cat by the tail learns
;    something he can learn in no other way
;    -- Mark Twain

;; Dynamic vars are also useful for configuration, for instance, here is
;; the built-in `*print-lenth*` which lets us define how many items
;; in a collection will be printed:

(binding [*print-length* 1]
  (println ["Print" "just" "one!"]))
; => [Print ...]

;; NOTE: We can also `set!` dynamic vars that have been bound. It allows
;;       us to convey information "out" of a function without having to
;;       return it as an argument

(def ^:dynamic *troll-thought* nil)
(defn troll-riddle
  [your-answer]
  (let [number "man meat"]
    (when (thread-bound? #'*troll-thought*)
      (set! *troll-thought* number))
    (if (= number your-answer)
      "TROLL: You can cross the bridge!"
      "TROLL: Time to eat you, succulent human!")))

(binding [*troll-thought* nil]
  (println (troll-riddle 2))
  (println "SUCCULENT HUMAN: Oooooh! The answer was" *troll-thought*)
  (println (troll-riddle "man meat"))
  (println "SUCCULENT HUMAN: Oooooh! The answer was" *troll-thought*))
;; TROLL: Time to eat you, succulent human!
;; SUCCULENT HUMAN: Oooooh! The answer was man meat
;; TROLL: You can cross the bridge!
;; SUCCULENT HUMAN: Oooooh! The answer was man meat

;; Lastly, if you access a dynamically bound var from within a manually
;; created thread, the var will evaluate to the original value.

(.write *out* "prints to repl")
; => prints to repl

;; The following won't print to REPL, because `*out*` is not bound to the REPL:

(.start (Thread. #(.write *out* "prints to standard out")))

;; We can work around this using the following two techniques:

(let [out *out*]
  (.start
   (Thread. #(binding [*out* out]
               (.write *out* "prints to repl from thread")))))

;; The `let` binding captures `*out*` so we can rebind it in our manually
;; generated child thread. Though bindings don't get passed to manually
;; created threads, they do get passed to futures. This is called
;; "binding conveyance."

; ---------------------
; Altering the Var Root
; ---------------------

;; When we create a new var, we assume its initial value that we supply
;; is its "root."

(def power-source "hair")

;; Here, we understand that "hair" is the root value of `power-source`

(alter-var-root #'power-source (fn [_] "7-eleven parking lot"))
power-source
; => "7-eleven parking lot"

;; Also, if at all possible, don't do this! Use functional programming
;; techniques instead, as this goes against Clojure's core philosophy
;; of immutable data!

; ------------------------------------------------------------------------------
; Stateless Concurrency and Parallelism with `pmap`

;; `pmap` is a parallel map function which makes it easy to achieve
;; stateless concurrency

(defn always-1
  []
  1)
(take 5 (repeatedly always-1))
; => (1 1 1 1 1)

(take 5 (repeatedly (partial rand-int 10)))
; => (1 5 0 3 4)

(def alphabet-length 26)

(def letters
  "Vector of chars, A-Z"
  (mapv (comp str char (partial + 65)) (range alphabet-length)))

(println letters)
; => [A B C D E F G H I J K L M N O P Q R S T U V W X Y Z]

(defn random-string
  "Returns a random string of specified length"
  [length]
  (apply str (take length (repeatedly #(rand-nth letters)))))

(println (random-string 18))
; => SJMUEJPOMDJENPHSVQ

(defn random-string-list
  [list-length string-length]
  (doall (take list-length (repeatedly (partial random-string string-length)))))

(def orc-names (random-string-list 3000 7000))

;; (time (dorun (map clojure.string/lower-case orc-names)))
; => "Elapsed time: 261.930982 msecs"

;; (time (dorun (pmap clojure.string/lower-case orc-names)))
; => "Elapsed time: 88.05338 msecs"

;; Wow! That's a crazy increase in performance - but `pmap` isn't always
;; the right answer. There is some overhead involved in the creation
;; and coordination of threads, so it is possible that this overhead could
;; surpass the time of each function application.

;; We can see this here in action:
(def orc-name-abbrevs (random-string-list 20000 300))
(time (dorun (map clojure.string/lower-case orc-name-abbrevs)))
; => "Elapsed time: 79.033944 msecs"

(time (dorun (pmap clojure.string/lower-case orc-name-abbrevs)))
; => "Elapsed time: 68.134221 msecs"

;; Here, the performance gap is much less pronounced - the issue here
;; is that the "grain size" or amount of work done by each parallelized
;; task is too small compared to the overhead of `pmap`

;; To increase the "grain size" as it were, we could apply
;; `clojure.string/lower-case` to multiple elements instead of only one,
;; using `partition-all`

(def numbers [1 2 3 4 5 6 7 8 9 10])
(partition-all 3 numbers)
; => ((1 2 3) (4 5 6) (7 8 9) (10))

(pmap inc numbers) ; => grain size of one

(pmap (fn [number-group] (doall (map inc number-group)))
      (partition-all 3 numbers))
; => ((2 3 4) (5 6 7) (8 9 10) (11))

(apply concat
       (pmap (fn [number-group] (doall (map inc number-group)))
             (partition-all 3 numbers)))
; => (2 3 4 5 6 7 8 9 10 11)

(time
 (dorun
  (apply concat
         (pmap (fn [name] (doall (map clojure.string/lower-case name)))
               (partition-all 1000 orc-name-abbrevs)))))
; => "Elapsed time: 28.466121 msecs"

;; Now, we're back to that performance gain we're expecting!

;; Let's generalize this technique into a function called `ppmap`,
;; for "partitioned pmap"
(defn ppmap
  "Partitioned pmap, for grouping map ops together to make parallel
  overhead worthwhile"
  [grain-size f & colls]
  (apply concat
         (apply pmap
                (fn [& pgroups] (doall (apply map f pgroups)))
                (map (partial partition-all grain-size) colls))))
(time (dorun (ppmap 1000 clojure.string/lower-case orc-name-abbrevs)))
; => "Elapsed time: 28.03561msecs"

;; For more like this chapter, check out clojure.core.reducers library,
;; `http://clojure.org/reducers/` - which provides alternative
;; implementations of seq functions like `map` and `reduce` that
;; are usually faster than their `clojure.core` counterparts - however,
;; these functions are not lazy!
