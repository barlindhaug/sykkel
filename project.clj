(defproject sykkel "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/java.jdbc "0.4.2"]
                 [postgresql "9.1-901.jdbc4"]
                 [clj-http "0.9.2"]
                 [clj-time "0.11.0"]
                 [compojure "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [ring/ring-jetty-adapter "1.4.0"]]
  :plugins [[lein-ring "0.8.10"]]
  :ring {:handler sykkel.server/app
         :auto-refresh? true}
  :main sykkel.server
  :min-lein-version "2.0.0")
