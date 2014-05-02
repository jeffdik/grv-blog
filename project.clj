(defproject grv/blog "0.1.0-SNAPSHOT"
  :description "A Simple Blog"
  :url "https://github.com/jeffdik/grv-blog"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.5"]
                 [http-kit "2.1.8"]
                 [hiccup "1.0.5"]
                 [enlive "1.1.5"]]
  :profiles {:dev {:dependencies [[ring-mock "0.1.5"]]}})
