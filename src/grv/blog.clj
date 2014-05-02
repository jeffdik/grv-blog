(ns grv.blog
  (:require [clojure.pprint :as pp :refer [pprint]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.nested-params :refer [wrap-nested-params]]
            [compojure.core :refer [defroutes GET POST PUT DELETE context]]
            [compojure.handler :as comp-handler]
            [compojure.route :as comp-route]
            [hiccup.core :as hiccup]
            [net.cgrand.enlive-html :as enlive])
  (:import [java.util Date]))

(defn add-post [blog post]
  (conj blog
        (merge {:index (count blog)
                :time (Date.)}
               post)))

(defn edit-post [blog index post]
  (nth blog index)
  (update-in blog [index] merge post))

(defn delete-post [blog index]
  (nth blog index)
  (assoc blog index nil))

(defonce blog-atom (atom []))

(defn always-successful-hander [req]
  {:status 200
   :body "Happy Times!"})

(defn blog-handler [{:keys [request-method uri] :as req}]
  (try
    (cond
     (and (= :post (:request-method req))
          (= "/blog" (:uri req)))
     {:status 201
      :headers {"content-type" "application/edn"}
      :body (pr-str (swap! blog-atom add-post (:params req)))}

     (and (= :get request-method)
          (re-matches #"/blog/\d+" uri))
     (let [index (-> (re-find #"/blog/(\d+)" uri)
                     second
                     Integer/parseInt)]
       {:status 200
        :headers {"content-type" "application/edn"}
        :body (pr-str (nth @blog-atom index))})

     (and (= :put request-method)
          (re-matches #"/blog/\d+" uri))
     (let [index (-> (re-find #"/blog/(\d+)" uri)
                     second
                     Integer/parseInt)
           new-post (:params req)
           new-blog (swap! blog-atom edit-post index new-post)]
       {:status 200
        :headers {"content-type" "application/edn"}
        :body (pr-str (nth new-blog index))}))
    (catch IndexOutOfBoundsException e
      {:status 404
       :body "Not found"})))

(defn wrap-logging [handler]
  (fn [req]
    (println (str "New Request (") (Date.) "):")
    (pprint req)
    (println)
    (handler req)))

(def blog-app (-> blog-handler
                  wrap-logging
                  wrap-keyword-params
                  wrap-params))

(defroutes routes
  (POST "/blog" [title body]
        {:status 201
         :headers {"content-type" "application/edn"}
         :body (pr-str (swap! blog-atom add-post {:title title :body body}))})
  (context ["/blog/:id" :id #"\d+"] [id]
           (GET "/" []
                {:status 200
                 :headers {"content-type" "application/edn"}
                 :body (pr-str (or (nth @blog-atom (Integer/parseInt id))
                                   {:status 404}))})
           (PUT "/" req
                (let [index (Integer/parseInt id) 
                      new-blog (swap! blog-atom edit-post index (:params req))]
                  {:status 200
                   :headers {"content-type" "application/edn"}
                   :body (pr-str (nth new-blog index))}))
           (DELETE "/" []
                   (swap! blog-atom delete-post (Integer/parseInt id))
                   {:status 200})))

(defn wrap-index-out-of-bounds [handler]
  (fn [req]
    (try
      (handler req)
      (catch IndexOutOfBoundsException e
      {:status 404
       :body "Not found"}))))

(def comp-app (-> routes
                  wrap-index-out-of-bounds
                  wrap-logging
                  wrap-keyword-params
                  wrap-params))
