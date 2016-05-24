(ns first-clojure-project.core
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, Clojure - it's great to meet you!!"))

;; Learning how to evaluate from Emacs buffer using Monroe/CIDER, C-c C-e keybinding
(println "Functional programming is amazing - the power of Lambda compels thee! Oh, and Stallman was right, Emacs is really the one true way!")

;; Creating my first Clojure function
(defn metal
  []
  (println "Eleven. Exactly. One louder."))

;; Getting to know errors in Emacs buffers and nREPL
;; (map)
;;  map requires args >= 1, calling map resulted in the REPL showing me the following stacktrace:
;; ArityException Wrong number of args (0) passed to: core/map clojure.lang.AFn.throwArity (Afn.java:429)

;; Using Evil Lispy
(+ 1 (+ 2 3) 4)

;; Using Paredit - after fixing user.el, works like a charm with Evil mode! Huzzah!
(+ 1 2 3 (* 4 5) 6)

;; More editing using Paredit
(* (/ 2 4 6 (- 1 (+ 3 5)) 42 24) 10 (* 9 8 7))

(+ 1 (2) (* 3 4) 5 6 (- 7 8 9) 10)
