(ns paren.test-runner
  (:require [clojure.test :as t]
            [paren.serene-test]))

#?(:cljs
   (defmethod t/report [:cljs.test/default :end-run-tests] [summary]
     (set! (.-exitCode js/process) (if (t/successful? summary) 0 1))))

(defn -main []
  #?(:clj (when-not (t/successful? (t/run-tests 'paren.serene-test))
            (System/exit 1))
     :cljs (do
             (set! (.-exitCode js/process) 1)
             (t/run-tests 'paren.serene-test))))

#?(:cljs (set! *main-cli-fn* -main))
