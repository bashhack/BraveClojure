(defproject pegthing "0.1.0-SNAPSHOT"
  :description "A peg solitaire game a la Cracker Barrel"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :main ^:skip-aot pegthing.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
