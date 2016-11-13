;; Clojure Exercises
;; (from Chapter 8, 'Writing Macros')

; ------------------------------------------------------------------------------

; (1) Write the macro `when-valid` so that it behaves similarly to `when`.
; Here is an example of calling it:

(when-valid order-details order-details-validations
            (println "It's a success!")
            (render :success))

; When the data is valid, the println and render forms should be evaluated,
; and when-valid should return nil if the data is invalid.

; (2) You saw that `and` is implemented as a macro. Implement `or` as a macro.

; (3) In Chapter 5 you created a series of functions (`c-int`, `c-str`, `c-dex`)
; to read an RPG character’s attributes. Write a macro that defines an arbitrary
; number of attribute-retrieving functions using one macro call.
; Here’s how you would call it:

(defattrs c-int :intelligence
          c-str :strength
          c-dex :dexterity)
