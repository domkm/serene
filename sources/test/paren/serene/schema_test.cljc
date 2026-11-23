(ns paren.serene.schema-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.string :as str]
   [clojure.test :as t]
   [paren.serene :as serene]
   [paren.serene.schema :as schema]
   #?@(:clj [[clojure.data.json :as json]
             [clojure.java.io :as io]
             [org.httpkit.client :as http]]))
  #?(:cljs (:require-macros [paren.serene.schema-test])))

#?(:clj (defn- modern-introspection []
          ;; Trimmed GraphQL.js response for modern-schema.graphql.
          (-> "paren/serene/modern-introspection.json" io/resource slurp json/read-str)))

(def ^:private defined-specs (serene/def-specs (modern-introspection)))

(t/deftest modern-introspection-test
  (t/is (s/valid? ::Filter {}))
  (t/is (s/valid? ::Filter {:legacy "value"}))
  (t/is (not (s/valid? ::Filter {:legacy nil})))
  (let [args :paren.serene.schema-test.Query.search/&args]
    (t/is (s/valid? args {:legacy 1 :filter {:legacy "value"}}))
    (t/is (not (s/valid? args {:legacy "not an integer"})))))

(t/deftest introspection-query-test
  (let [include-deprecated-args #"args\(includeDeprecated: true\)"
        include-deprecated-fields "inputFields(includeDeprecated: true)"]
    (t/is (not (re-find include-deprecated-args schema/query)))
    (t/is (not (str/includes? schema/query include-deprecated-fields)))
    (t/is (= 2 (count (re-seq include-deprecated-args schema/query-with-deprecated-inputs))))
    (t/is (str/includes? schema/query-with-deprecated-inputs include-deprecated-fields))))

#?(:clj
   (t/deftest fetch-query-test
     (with-redefs [http/request (fn [request] (delay {:body (json/write-str request)}))]
       (let [url "https://example.invalid/graphql"
             modern-opts {:include-deprecated-inputs? true :headers {"X-Test" "header"}}
             legacy (schema/fetch url)
             modern (schema/fetch url modern-opts)]
         (t/is (= {"query" schema/query} (json/read-str (:body legacy))))
         (t/is (= {"query" schema/query-with-deprecated-inputs} (json/read-str (:body modern))))
         (t/is (= {:X-Test "header"} (:headers modern)))
         (t/is (not (contains? modern :include-deprecated-inputs?)))
         (t/is (= "custom" (:body (schema/fetch url (assoc modern-opts :body "custom")))))))))
