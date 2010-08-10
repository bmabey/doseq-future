(ns doseq-future.core)

deftype Foo [])

(defonce quit? (atom false))

(defn good
  "In this version the lazy-seq is created and relaized all within the
  same future.  It does not retain a reference to the head of the lazy-seq
  and memory ussuage is fine."
  []
  (@(future (let [foos (repeatedly (fn []
                                   (java.lang.Thread/sleep 1)
                                   (when @quit? (throw (Exception. "quitting!")))
                                   (Foo.)))]
           (doseq [foo foos] foo)))))

(defn bad
  "Here the lazy-seq is defined outside of the future where the realization
  happends.  As a result the entire lazy-seq is kept in memory."
  []
  (let [foos (repeatedly (fn []
                            (java.lang.Thread/sleep 1)
                            (when @quit? (throw (Exception. "quitting!")))
                            (Foo.)))]
    @(future (doseq [foo foos] foo))))

(defn quit []
  (reset! quit? true)
  (java.lang.Thread/sleep 1000)
  (reset! quit? false))

(defn run
  "Calls f and then calls quit after t seconds"
  [f t]
  (future (java.lang.Thread/sleep (* t 1000)) (quit))
  (f))
