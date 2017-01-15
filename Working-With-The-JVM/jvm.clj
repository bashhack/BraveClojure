
;;;; ---------------------------------------------------------------------------
;;;; ------------------------- Working with the JVM ----------------------------
;;;; ---------------------------------------------------------------------------

;; Welp - I've spent long enough "almost" writing some Java (ahem...looking at
;; you, C#!) - might as well enjoy this!

;; A couple main reasons why some knowledge of Java is useful:

;; Clojure, as we know, is hosted on the JVM (Java Virtual Machine). Because of
;; this we run Clojure applications the same way we run Java applications. We
;; use Java objects for core functionality, like reading files and working with
;; dates. Finally, Java has a huge ecosystem of useful libs, and we'll need to
;; know a bit about Java in order to utilize those, if needed.

;; We can think of the JVM model like we think of the computer itself. A system
;; of processes where low-level instructions "Java bytecode" are translated
;; into machine code that the host will understand, a process called
;; "just-in-time compilation".

;; For a program to run on the JVM, it must get compiled to Java bytecode.
;; Usually, the resulting bytecode is saved in a ".class" file. These are
;; packaged in "Java archive files (JAR files)". Just as a CPU doesn't care
;; what language created the machine instructions, the JVM doesn't care
;; how you create bytecode. It doesn't care if you use Scala, JRuby, Clojure,
;; or Java.

;; When we say "Clojure runs on the JVM" we're really saying that Clojure
;; programs get compiled to Java bytecode and JVM processes execute them.
;; There is no quantifiable difference between a Java and a Clojure program
;; any more than you can tell the difference between a C and C++ program.

; -----------------------------------------------------------------------------
; Writing, Compiling, and Running a Java Program

; -----------------------------------------------------------
; Object-Oriented Programming in the World's Tiniest Nutshell

;; Java is an OOP language, we need to remember our fundamentals here:
;; "classes," "objects," and "methods"

;; Let's write some basic Java code in the directory `PiratePhrases`

; -----------------------------------------------------------------------------
; Packages and Imports

;; A Java package is like a Clojure namespace, they provide code organization.
;; Packages contain classes, and package names correlate with directories.
;; Ex. If the line `package com.shapemaster` is in a file, the directory
;; `com/shapemaster` must exist somewhere on your classpath.
;; Within that directory will be files defining classes.

;; A Java import statement allows you to import classes, which means we can
;; refer to them without their namespace prefix.
;; Ex. `com.shapemaster` has a class named `Square`, we could write
;; `import com.shapemaster.Square` or `import com.shapemaster.*` at the top of
;; a `.java` file to use `Square` instead of `com.shapemaster.Square`

; -----------------------------------------------------------------------------
; JAR Files

;; JAR files bundle our `.class` files into a single archive file.

;; Example of creating a JAR:
;; $jar cvfe conversation.jar PirateConversation PirateConversation.class pirate_phrases/*.class

;; To inspect the contents of the JAR:
;; $jar tf conversation.jar

; ------------------------------------------------------------------------------
; Java Interop

;; Rich Hickey has designed Clojure to be a practical language - and so Clojure
;; was designed to make it easy to interact with Java classes and objects,
;; meaning that we can draw on Java's large native functionality and ecosystem.

; --------------
; Interop Syntax
; --------------

;; ---------------------
;; Object Interop Syntax

;; `(.` allows us to call methods on an object

(.toUpperCase "By Bluebeard's bananas!")
; => "BY BLUEBEARD'S BANANAS!

(.indexOf "Let's synergize our bleeding edges" "y")
; => 7

;; We can access static methods on classes and access classes' static fields:

(java.lang.Math/abs -3)
; => 3

java.lang.Math/PI
; => 3.14159265358793

;; In the examples, we're actually using macros that expand to use the "dot
;; special form." We won't typically need to use the dot special form unless
;; writing our own macros to interact with Java objects and classes.

(macroexpand-1 '(.toUpperCase "By Bluebeard's bananas!"))
; => (. "By Bluebeard's bananas!" toUpperCase)

;; -----------------------------
;; Creating and Mutating Objects

;; To create new objects and interact with them, we use one of the
;; following two ways: `(new ClassName opt-args)` and `(ClassName. opt-args)`

(new String)
; => ""

(String.)
; => ""

(String. "To Davey Jones's Locker with ye hardies")
; => "To Davey Jones's Locker with ye hardies"

;; Most people use the dot version FYI

;; To modify an object, we call methods on it as we have just done.

(java.util.Stack.)
; => []

(let [stack (java.util.Stack.)]
  (.push stack "Latest episode of Game of Thrones, ho!")
  stack)
; => ["Latest episode of Game of Thrones, ho!"]

;; We could execute multiple methods on the same object, using the `doto` macro:

(doto (java.util.Stack.)
  (.push "Latest episode of Game of Thrones, ho!")
  (.push "Whoops, I meant 'Land, ho!'"))
; => ["Latest episode of Game of Thrones, ho!", "Whoops, I meant 'Land, ho!'"]

;; Expanding the macro shows us it is equivalent to the `let` expression example:

(macroexpand-1
 '(doto (java.util.Stack.)
    (.push "Latest episode of Game of Thrones, ho!")
    (.push "Whoops, I meant 'Land, ho!'")))
; => (clojure.core/let
; =>   [G__10504 (java.util.Stack.)]
; =>   (.push G__10504 "Latest episode of Game of Thrones, ho!")
; =>   (.push G__10504 "Whoops, I meant 'Land, ho!'")
; =>   G__10504)

;; ---------
;; Importing

;; In Clojure, importing has the same effect as in Java, we can use classes
;; without having to type their entire package prefix:

;; The general form looks like this:
;; (import [package.name1 ClassName1 ClassName2]
;;         [package.name2 ClassName3 ClassName4])

(ns pirate.talk
  (:import [java.util Date Stack]
           [java.net Proxy URI]))

;; NOTE: Clojure automatically imports for us the classes in `java.lang`,
;;       including `java.lang.String` and `java.lang.Math`

; -----------------------------------------------------------------------------
; Commonly Used Java Classes

;; ----------------
;; The System Class

;; The `System` class is useful for interacting with the environment the program
;; is running in. It can be used for env variables, I/O, and error output.

;; The most useful functions are:
;; `exit`, `getenv` and `getProperty`

;; --------------
;; The Date Class

;; Java has great tools for working with dates - one of which is the
;; `java.util.Date` class, whose online API docs are here:
;; http://docs.oracle.com/javase/7/docs/api/java/util/Date.html

;; The main takeaways from the `date` class are:
;; (1) you can represent dates as literals using a form like:
#inst "2016-09-19T20:40:02.733-00:00"
;; (2) you have to use the `java.util.DateFormat` class if you want to
;; customize how you convert dates to strings or convert strings to dates
;; (3) if you need to perform tasks like comparing dates or add minutes,
;; hours, or other units of time to a date, use the clj-time library:
;; https://github.com/clj-time/clj-time

; -----------------------------------------------------------------------------
; Files and I/O

;; Java I/O isn't exactly easy or straightforward - Clojure makes it simpler.
;; https://clojure.github.io/clojure/clojure.java.io-api.html

;; Java splits its IO classes, `java.io.BufferedReader`, `java.io.FileReader`,
;; `java.io.BufferedWriter`, `java.io.FileWriter`, etc each with their own
;; implementations of methods like `read`, `close`, `append`, `write`, `flush`,
;; etc. - there's enough here to get yourself into trouble if not careful.

;; Meanwhile, in Clojure, we reduce this complexity to:
;; `spit` which writes to a resource, and
;; `slurp` which reads from one

(spit "/tmp/hercules-todo-list"
"- kill dat lion brov
- chop up what nasty nasty multi-headed snake thing")

(slurp "/tmp/hercules-todo-list")
; => "- kill dat lion brov
; =>  - chop up what nasty multi-headed snake thing"

;; The `with-open` macro is also helpful - as it automatically closes a resource
;; at the end of its body, ensuring that we don't tie up resources by forgetting
;; to manually close the resource.

;; The `reader` function is also useful as an alternative to `slurp` when
;; we don't want to try to read the resource in full and don't want to
;; sort out which Java class we need to use.

;; We can combine these with the `line-seq` function to read one line at a time:

(with-open [todo-list-rdr (clojure.java.io/reader "/tmp/hercules-todo-list")]
  (println (first (line-seq todo-list-rdr))))
; => - kill dat lion brov

;; For more on IO in Clojure, it can be helpful to refer back to:
;; https://clojure.github.io/clojure/clojure.java.io-api.html
;; https://docs.oracle.com/javase/7/docs/api/java/nio/file/package-summary.html
;; and, http://docs.oracle.com/javase/7/docs/api/java/io/package-summary.html
