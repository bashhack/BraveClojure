;;;; Big Government Data Collection -
;;;;
;;;; A project that plays off of the idea of mass-surveilance by some
;;;; big government entity looking over random data about its citizens
;;;; having collected their names and an arbitrary threat index.

;;; Init
(ns bgdc.core)

;;; Create shorthand symbol to easily access our src data
(def filename "suspects.csv")
;(slurp filename)    ; Read content from filename

(def suspect-keys [:name :threat-index])

(defn str->int
  ;; Converts a string to an int using Java interop
  [str]
  (Integer. str))

(def conversions {:name identity
                  :threat-index str->int})

(defn convert
  [suspect-key value]
  ((get conversions suspect-key) value))

(defn parse
  ;; Convert a CSV into rows of colums
  [string]
  (map #(clojure.string/split % #",")
       (clojure.string/split string #"\n")))

;(parse (slurp filename))
; => (["Suspicious John Doe" "0"]
;     ["Suspicious Jane Doe" "6"]
;     ["Suspicious Jane Doe" "0"]
;     ["Suspicious John Doe" "4"]
;     ["Suspicious John Doe" "10"])

(defn mapify
  ;; Return a seq of maps like {:name \"Suspicious John Doe\" :threat-index 3}"
  [rows]
  (map (fn [unmapped-row]
         ;; ["Suspicious John Doe" "0"]
         (reduce (fn [row-map [suspect-key value]]
                   (assoc row-map suspect-key (convert suspect-key value)))
                 {}
                 ;; NOTE: When map is passed multiple collections,
                 ;; they are consumed in parallel!
                 (map vector suspect-keys unmapped-row)))
                 ;; => ([:name "Suspicious John Doe"] [:threat-index "0"])
       rows))

(defn threat-filter
  [minimum-threat records]
  (filter #(>= (:threat-index %) minimum-threat) records))

(mapify (parse (slurp filename)))
;; Visualizing reduce can be hard to, with simulatenous events occuring,
;; so I logged out the values at every step of the way:

;; Row-map: {}
;; Suspect-key: :name
;; Value: Suspicious John Doe
;; Convert result: Suspicious John Doe
;; Assoc result: {:name "Suspicious John Doe"}

;; Row-map: {:name "Suspicious John Doe"}
;; Suspect-key: :threat-index
;; Value: 0
;; Convert result: 0
;; Assoc result: {:name "Suspicious John Doe", :threat-index 0}

;; Row-map: {}
;; Suspect-key: :name
;; Value: Suspicious Jane Doe
;; Convert result: Suspicious Jane Doe
;; Assoc result: {:name "Suspicious Jane Doe"}

;; Row-map: {:name "Suspicious Jane Doe"}
;; Suspect-key: :threat-index
;; Value: 6
;; Convert result: 6
;; Assoc result: {:name "Suspicious Jane Doe", :threat-index 6}

;; Row-map: {}
;; Suspect-key: :name
;; Value: Suspicious Jane Doe
;; Convert result: Suspicious Jane Doe
;; Assoc result: {:name "Suspicious Jane Doe"}

;; Row-map: {:name "Suspicious Jane Doe"}
;; Suspect-key: :threat-index
;; Value: 0
;; Convert result: 0
;; Assoc result: {:name "Suspicious Jane Doe", :threat-index 0}

;; Row-map: {}
;; Suspect-key: :name
;; Value: Suspicious John Doe
;; Convert result: Suspicious John Doe
;; Assoc result: {:name "Suspicious John Doe"}

;; Row-map: {:name "Suspicious John Doe"}
;; Suspect-key: :threat-index
;; Value: 4
;; Convert result: 4
;; Assoc result: {:name "Suspicious John Doe", :threat-index 4}

;; Row-map: {}
;; Suspect-key: :name
;; Value: Suspicious John Doe
;; Convert result: Suspicious John Doe
;; Assoc result: {:name "Suspicious John Doe"}

;; Row-map: {:name "Suspicious John Doe"}
;; Suspect-key: :threat-index
;; Value: 10
;; Convert result: 10
;; Assoc result: {:name "Suspicious John Doe", :threat-index 10}

(first (mapify (parse (slurp filename))))

(threat-filter 5 (mapify (parse (slurp filename))))
; => ({:name "Suspicious Jane Doe", :threat-index 6}
;     {:name "Suspicious John Doe", :threat-index 10})
