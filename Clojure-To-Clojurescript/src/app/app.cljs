(ns app.core)

(enable-console-print!)

(def concert-audience
  {0 {:has-tattoos? false :plays-accordian? false :name "Angus Lars Anthrax"}
   1 {:has-tattoos? false :plays-accordian? false :name "Judas Jon Johannson"}
   2 {:has-tattoos? true :plays-accordian? true :name "Ernst Van Streuselmeyer"}
   3 {:has-tattoos? true :plays-accordian? false :name "Margot Gunnarschmitt"}})

(defn metal-fan-details
  [social-security-number]
  (get concert-audience social-security-number))

(defn polka-enthusiast?
  [record]
  (and (:has-tattoos? record)
       (:plays-accordian? record)
       record))

(defn identify-polka-enthusiast
  [social-security-numbers]
  (first (filter polka-enthusiast?
                 (map metal-fan-details social-security-numbers))))

(println (time (identify-polka-enthusiast (range 0 1000000))))
