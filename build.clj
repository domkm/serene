(ns build
  (:require [clojure.tools.build.api :as b]))

(def lib 'com.paren/serene)
(def version "0.0.3-SNAPSHOT")
(def repo-url "https://github.com/domkm/serene")
(def class-dir "target/jar")
(def basis (delay (b/create-basis {:project "deps.edn"})))

(defn jar [_]
  (b/delete {:path class-dir})
  (b/copy-dir {:src-dirs (:paths @basis)
               :target-dir class-dir})
  (b/write-pom {:class-dir class-dir
                :lib lib
                :version version
                :basis @basis
                :src-dirs ["sources/main"]
                :scm {:url repo-url}
                :pom-data [[:description "Generate clojure.spec with GraphQL and extend GraphQL with clojure.spec"]
                           [:url repo-url]
                           [:licenses
                            [:license
                             [:name "Eclipse Public License 2.0"]
                             [:url "https://www.eclipse.org/legal/epl-2.0/"]]]]})
  (b/jar {:class-dir class-dir
          :jar-file "target/serene.jar"}))
