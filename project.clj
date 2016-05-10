(defproject geekmail-server "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.4.0"]
                 [org.clojure/data.json "0.2.6"]
                 [ring/ring-defaults "0.1.5"]
                 [cheshire "5.6.1"]
		 [io.forward/clojure-mail "1.0.4"]
                 [environ "1.0.0"]
                 [honeysql "0.6.3"]
                 [org.clojure/java.jdbc "0.3.5"]
                 [org.postgresql/postgresql "9.4-1201-jdbc4"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler geekmail-server.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
