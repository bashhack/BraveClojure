
;;;; ---------------------------------------------------------------------------
;;;; -------------------------   Peg Thing Game   ------------------------------
;;;; ---------------------------------------------------------------------------

;;; Goals:
;;; - Demonstrate sound program architecture
;;; - Utilize immutable data structures
;;; - Implement lazy sequences
;;; - Use pure functions where possible
;;; - Favor recursion over iteration

;;;; ---------------------------------------------------------------------------
;;;; ---------------------------------------------------------------------------

;; Define namespace for program, require namespace 'clojure.set' for easy
;; access to utility fns it provides working with sets (difference, union, join,
;; map-invert, etc.), and calling gen-class to run program from command line

;; (ns pegthing.core
;;   (require [clojure.set :as set])
;;   (:gen-class))

;; We're going to end up with a data structure that should look like this:
;; {1 {:pegged true, :connections {6 3, 4 2}},
;;  2 {:pegged true, :connections {9 5, 7 4}},
;;  3 {:pegged true, :connections {10 6, 8 5}},
;;  4 {:pegged true, :connections {13 8, 11 7, 6 5, 1 2}},
;;  5 {....
;;  .......
;;  15 {:pegged true, :connections {13 14, 6 10}},
;;  :rows 5}
;;
;; ...where `:pegged` represents whether a peg is placed at that position,
;; and `:connections` is a map where each key identifies a legal position and
;; each value is the position which would be jumped over

;;;; ---------------------------------------------------------------------------
;;;; Creating the Board
;;;; ---------------------------------------------------------------------------

;; http://mathworld.wolfram.com/TriangularNumber.html
(defn tri*
  "Generates lazy sequence of triangular numbers"
  ([] (tri* 0 1))
  ([sum n]
   (let [new-sum (+ sum n)]
     ;; (tri* 1 2) => (tri* 3 3) => (tri* 6 4) => (tri* 10 5)...
     (cons new-sum (lazy-seq (tri* new-sum (inc n)))))))

(def tri (tri*))

;; (take 5 tri)
; => (1 3 6 10 15)

(defn triangular?
  "Is the number triangular? eg. 1, 3, 6, 10, 15, etc."
  [n]
  (= n (last (take-while #(>= n %) tri))))

;; (triangular? 5)
; => false

(defn row-tri
  "The triangular number at the end of row n"
  [n]
  (last (take n tri)))

;; (row-tri 5)
; => 15

;; (= (row-tri 6) (last (take 6 tri)))
; => true

(defn row-num
  "Returns row number the position belongs to: pos 1 in row 1,
  positions 2 and 3 in row 2, etc."
  [pos]
  (inc (count (take-while #(> pos %) tri))))

;; 1) (take-while #(> 10 %) tri)
; => (1 3 6)

;; 2) (count (take-while #(> 10 %) tri))
; => 3

;; 3) (inc (count (take-while #(> 10 %) tri)))
; => 4

;; (row-num 10)
; => 4

;; (row-tri 4)
; => 10

(defn connect
  "Form a mutual connection between two positions"
  [board max-pos pos neighbor destination]
  (if (<= destination max-pos)
    (reduce (fn [new-board [p1 p2]]
              (assoc-in new-board [p1 :connections p2] neighbor))
            board
            [[pos destination] [destination pos]])
    board))

;; (connect {} 15 1 2 4)
; => {1 {:connections {4 2}}, 4 {:connections {1 2}}}

;; (assoc-in {} [:cookie :monster :vocals] "Finntroll")
; => {:cookie {:monster {:vocals "Finntroll"}}}

;; (get-in {:cookie {:monster {:vocals "Finntroll"}}} [:cookie :monster]
; => {:vocals "Finntroll"}

(defn connect-right
  [board max-pos pos]
  (let [neighbor (inc pos)
        destination (inc neighbor)]
    (if-not (or (triangular? neighbor) (triangular? pos))
      (connect board max-pos pos neighbor destination)
      board)))

(defn connect-down-left
  [board max-pos pos]
  (let [row (row-num pos)
        neighbor (+ row pos)
        destination (+ 1 row neighbor)]
    (connect board max-pos pos neighbor destination)))

(defn connect-down-right
  [board max-pos pos]
  (let [row (row-num pos)
        neighbor (+ 1 row pos)
        destination (+ 2 row neighbor)]
    (connect board max-pos pos neighbor destination)))

;; (connect-down-right {} 15 4)
;;; row => 3
;;; neighbor => 8
;;; destination => 13
;;; (connect {} 15 4 8 13)
; => {4 {:connections {13 8}}, 13 {:connections {4 8}}}

(defn add-pos
  "Pegs the position and performs connections"
  [board max-pos pos]
  (let [pegged-board (assoc-in board [pos :pegged] true)]
    (reduce (fn [new-board connection-creation-fn]
              (connection-creation-fn new-board max-pos pos))
            pegged-board
            [connect-right connect-down-left connect-down-right])))

;; (add-pos {} 15 4)
; {4 {:pegged true, :connections {6 5, 11 7, 13 8}},
;  6 {:connections {4 5}},
;  11 {:connections {4 7}},
;  13 {:connections {4 8}}}

;; Our `add-pos` function, which utilizes reducing over functions
;; is really another way of composing functions (see: `comp`)

;; We could rewrite our previous string clean method:

; (require '[clojure.string :as s])
; (defn clean
;   [text]
;   (s/replace (s/trim text) #"lol" "LOL"))
; => (clean "My boa constrictor is so sassy lol!  ")

; (defn clean-reduced
;   [text]
;   (reduce (fn [string string-fn] (string-fn string))
;           text
;           [s/trim #(s/replace % #"lol" "LOL")]))

(defn new-board
  "Creates a new board with the given number of rows"
  [rows]
  (let [initial-board {:rows rows}
        max-pos (row-tri rows)]
    (reduce (fn [board pos] (add-pos board max-pos pos))
            initial-board
            (range 1 (inc max-pos)))))

;; (new-board 5)
; {7 {:connections {2 4, 9 8}, :pegged true},
;  1 {:pegged true, :connections {4 2, 6 3}},
;  4 {:connections {1 2, 6 5, 11 7, 13 8}, :pegged true},
;  15 {:connections {6 10, 13 14}, :pegged true},
;  13 {:connections {4 8, 6 9, 11 12, 15 14}, :pegged true},
;  :rows 5,
;  6 {:connections {1 3, 4 5, 13 9, 15 10}, :pegged true},
;  3 {:pegged true, :connections {8 5, 10 6}},
;  12 {:connections {5 8, 14 13}, :pegged true},
;  2 {:pegged true, :connections {7 4, 9 5}},
;  11 {:connections {4 7, 13 12}, :pegged true},
;  9 {:connections {2 5, 7 8}, :pegged true},
;  5 {:pegged true, :connections {12 8, 14 9}},
;  14 {:connections {5 9, 12 13}, :pegged true},
;  10 {:connections {3 6, 8 9}, :pegged true},
;  8 {:connections {3 5, 10 9}, :pegged true}}

;;;; ---------------------------------------------------------------------------
;;;; Moving Pegs
;;;; ---------------------------------------------------------------------------

(defn pegged?
  "Does the position have a peg in it?"
  [board pos]
  (get-in board [pos :pegged]))

(defn remove-peg
  "Take the peg at given position out of the board"
  [board pos]
  (assoc-in board [pos :pegged] false))

(defn place-peg
  "Put a peg in the board at given position"
  [board pos]
  (assoc-in board [pos :pegged] true))

(defn move-peg
  "Take peg out of p1 and place it in p2"
  [board p1 p2]
  (place-peg (remove-peg board p1) p2))

;;;; ---------------------------------------------------------------------------
;;;; Evaluate Moves
;;;; ---------------------------------------------------------------------------

(defn valid-moves
  "Return a map of all valid moves for pos, where the key is the
  destination and the value is the jumped position"
  [board pos]
  (into {}
        (filter (fn [[destination jumped]]
                  (and (not (pegged? board destination))
                       (pegged? board jumped)))
                (get-in board [pos :connections]))))

(def my-board (assoc-in (new-board 5) [4 :pegged] false))

;; (my-board 1)
; => {:pegged true, :connections {4 2, 6 3}}

;; (valid-moves my-board 1)
; => {4 2}

(defn valid-move?
  "Return jumped position if the move from p1 to p2 is valid, nil
  otherwise"
  [board p1 p2]
  (get (valid-moves board p1) p2))

(defn make-move
  "Move peg from p1 to p2, removing jumped peg"
  [board p1 p2]
  (if-let [jumped (valid-move? board p1 p2)]
    (move-peg (remove-peg board jumped) p1 p2)))

(defn can-move?
  "Do any of the pegged positions have valid moves?"
  [board]
  (some (comp not-empty (partial valid-moves board))
        (map first (filter #(get (second %) :pegged) board))))

;;;; ---------------------------------------------------------------------------
;;;; ---------------------------------------------------------------------------
