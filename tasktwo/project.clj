(defproject tasktwo "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"][org.clojure/tools.namespace "0.2.11"][cheshire "5.8.0"][clj-time "0.14.3"][hiccup "1.0.5"]]
  :main ^:skip-aot tasktwo.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
