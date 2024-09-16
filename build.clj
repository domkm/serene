(ns build
  (:refer-clojure :exclude [test])
  (:require [clojure.tools.deps :as t]
            [clojure.tools.build.api :as b]
            [deps-deploy.maven-settings :as ms]
            [deps-deploy.deps-deploy :as dd]))

(def lib 'neax.clojure.serene/serene)
(def version "0.0.3-SNAPSHOT")
(def class-dir "target/classes")
(def uber-file (str "target/serene.jar"))
(def basis (b/create-basis {:project "deps.edn"}))

(def repo-url "https://maven.pkg.github.com/neaxplore/xplore-backend-lambda-functions")

(defn test "Run all the tests." [opts]
  (println "\nRunning tests...")
  (let [basis    (b/create-basis {:aliases [:test]})
        combined (t/combine-aliases basis [:test])
        cmds     (b/java-command
                  {:basis basis
                   :java-opts (:jvm-opts combined)
                   :main      'clojure.main
                   :main-args ["-m" "cognitect.test-runner"]})
        {:keys [exit]} (b/process cmds)]
    (when-not (zero? exit) (throw (ex-info "Tests failed" {}))))
  opts)

(defn- jar-opts [opts]
  (assoc opts
         :lib lib :version version
         :jar-file (format "target/%s-%s.jar" lib version)
         :scm {:tag (str "v" version)}
         :basis (b/create-basis {})
         :class-dir class-dir
         :target "target"
         :src-dirs ["src"]))

(defn compile-ns [opts]
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir
                  :compile-opts {:direct-linking true
                                 :elide-meta [:file :line :added]}}))

(defn clean [path]
  (b/delete {:path path}))

(defn uber [_]
  (println (format "building: %s %s" lib version))
  (clean "target/classes")
  (b/copy-dir {:src-dirs ["src"]
               :target-dir class-dir})
  (compile-ns nil)
  ;; only remove uberjar after successful compilation
  (clean uber-file)
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main lib}))

(defn ci "Run the CI pipeline of tests (and build the JAR)." [opts]
  (b/delete {:path "target"})
  (test nil)
  (let [opts (jar-opts opts)]
    (println "\nWriting pom.xml...")
    (b/write-pom opts)
    (println "\nCopying source...")
    (b/copy-dir {:src-dirs ["resources" "src"] :target-dir class-dir})
    (compile-ns nil)
    (println "\nBuilding JAR...")
    (b/jar opts))
  opts)

(defn install "Install the JAR locally." [opts]
  (let [opts (jar-opts opts)]
    (b/install opts))
  opts)

(defn deploy "Deploy the JAR to Github." [opts]
  (let [{:keys [jar-file] :as opts} (jar-opts opts)]
    (dd/deploy {:installer :remote :artifact (b/resolve-path jar-file)
                :repository (assoc-in (ms/deps-repo-by-id-plaintext "github")
                                      ["github" :url]
                                      repo-url)
                :pom-file (b/pom-path (select-keys opts [:lib :class-dir]))}))
  opts)
