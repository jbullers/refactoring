(ns farm)

(defn hungry-idx [[i bunny]]
  (when (< (:amount bunny) (:capacity bunny))
    i))

(defn not-eaten? [vegetable]
  (not (zero? (:size vegetable))))

(defn eat [i ctx]
  (let [[vegetable & remaining-veg] (:vegetables ctx)
        farm (-> ctx :farm-states last)
        bunny (-> farm :bunnies (get i))
        amount-to-eat (min (:size vegetable) (- (:capacity bunny) (:amount bunny)))
        updated-vegetable (case (:type vegetable)
                            :lettuce (update vegetable :size - amount-to-eat)
                            :carrot (assoc vegetable :size 0))
        updated-bunny (update bunny :amount + amount-to-eat)
        updated-farm (assoc-in farm [:bunnies i] updated-bunny)]
    (-> ctx
        (update :farm-states conj updated-farm)
        (assoc :vegetables (if (not-eaten? updated-vegetable)
                             (cons updated-vegetable remaining-veg)
                             remaining-veg)))))

(defn store [ctx]
  (let [[vegetable & remaining-veg] (:vegetables ctx)
        farm (-> ctx :farm-states last)
        updated-farm (update farm :basket + (:size vegetable))]
    (-> ctx
        (update :farm-states conj updated-farm)
        (assoc :vegetables remaining-veg))))

(defn harvest [ctx]
  (if (:vegetables ctx)
    (if-let [bunny-idx (->> (-> ctx :farm-states last :bunnies)
                            (map-indexed vector)
                            (some hungry-idx))]
      (update ctx :queue concat [(partial eat bunny-idx) harvest])
      (update ctx :queue concat [store harvest]))
    ctx))

(defn run [{:keys [queue] :as ctx}]
  (if-let [[step & next-queue] (seq queue)]
    (recur (step (assoc ctx :queue next-queue)))
    ctx))

(defn simulate [farm vegetables]
  (run {:farm-states [farm]
        :vegetables vegetables
        :queue [harvest]}))

(comment
  (simulate {:bunnies [{:name "Buster" :capacity 10 :amount 0}
                       {:name "Babs" :capacity 8 :amount 0}]
             :basket 0}
            [{:type :lettuce :size 3}
             {:type :carrot :size 2}
             {:type :lettuce :size 7}
             {:type :carrot :size 5}
             {:type :lettuce :size 6}])
  ;; => {:farm-states
  ;;     [{:bunnies [{:name "Buster", :capacity 10, :amount 0} {:name "Babs", :capacity 8, :amount 0}], :basket 0}
  ;;      {:bunnies [{:name "Buster", :capacity 10, :amount 3} {:name "Babs", :capacity 8, :amount 0}], :basket 0}
  ;;      {:bunnies [{:name "Buster", :capacity 10, :amount 5} {:name "Babs", :capacity 8, :amount 0}], :basket 0}
  ;;      {:bunnies [{:name "Buster", :capacity 10, :amount 10} {:name "Babs", :capacity 8, :amount 0}], :basket 0}
  ;;      {:bunnies [{:name "Buster", :capacity 10, :amount 10} {:name "Babs", :capacity 8, :amount 2}], :basket 0}
  ;;      {:bunnies [{:name "Buster", :capacity 10, :amount 10} {:name "Babs", :capacity 8, :amount 7}], :basket 0}
  ;;      {:bunnies [{:name "Buster", :capacity 10, :amount 10} {:name "Babs", :capacity 8, :amount 8}], :basket 0}
  ;;      {:bunnies [{:name "Buster", :capacity 10, :amount 10} {:name "Babs", :capacity 8, :amount 8}], :basket 5}],
  ;;     :vegetables nil,
  ;;     :queue nil}
  )
