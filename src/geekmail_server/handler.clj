(ns geekmail-server.handler
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.route :as route]
	    [compojure.handler :refer [site]]
            [clojure-mail.core :refer :all]
            [clojure-mail.gmail :as gmail]
            [clojure-mail.message :refer (read-message)]
            [clojure.java.io :as io]
            [environ.core :refer [env]]
            [clojure.data.json :as json]
            [cheshire.core :refer :all]
            [ring.adapter.jetty :as jetty]
            [clojure.java.jdbc :as db])
  
  (:import [java.util Properties]
           [javax.mail.search FlagTerm]
           [java.io FileInputStream File]
           [javax.mail.internet MimeMessage]
           [javax.mail Session
            Folder
            Flags
            Flags$Flag AuthenticationFailedException]
           (com.sun.mail.imap IMAPStore)))

(use 'ring.middleware.session
     'ring.util.response
     'ring.middleware.cookies)

;;Utility function to get element from a collection
(defn in? 
  [coll elm]  
  (some #(= elm %) coll))

(def user-names (atom {}))
(def user-mails (atom {}))

;;Creates a message object for each fucntion
(def make-msg (fn [x]
                (let [head (reduce #(merge %1 %2) (:headers x))]
                  {:from (get head "From")
                   :subject (get head "Subject")
                   :date (get head "Date")
                   :message-id (get head "Message-ID")
                   :body (-> x :body first :body)
                   :status nil})))

;;Adds mails to user-mails atom. 
(defn add-user-mails 
  [mails user]
  (println "User mails " @user-mails)
  (let [old-mails (get @user-mails user)]
    (if old-mails
      (swap! user-mails assoc (str user) (-> (merge mails old-mails)))
      (swap! user-mails assoc (str user) mails))))

;;Connect and read mails from server
(defn read-mails [user key]
  (let [stored (if (and (get @user-names "user") (connected? (get @user-names "user"))) 
                 (get @user-names "user") (xoauth2-store "imap.gmail.com" user key))]
    (if (get @user-names "user")
      (let [mails (map make-msg (map read-message (unread-messages stored "inbox")))]
        (mark-all-read stored "inbox")
        (swap! user-names assoc (str user) stored)
        (add-user-mails mails user)
        (get @user-mails user))
      (let [mails (map make-msg (map read-message (take 10 (inbox stored))))]
        (mark-all-read stored "inbox")
        (swap! user-names assoc (str user) stored)
        (add-user-mails mails user)
        (get @user-mails user)))))

(def get-that-message (fn [user message-id] 
                        (for [x (get @user-mails user) :let [y (get x :message-id)] :when (= message-id y)] 
                          x)))

;; ;;change status (defer, done, delegate)
;; (defn change-message-satus [user message-id status]
;;   (let [messages (get @user-mails user)]
;;     (for [x messages :let []] 
;;       )))

;;Routes
(defroutes app
  (GET "/" [] 
       (slurp (io/resource "frontend.html")))
  
  (POST "/signing-up" request
        (let [req (json/read-str (ring.util.request/body-string request))]
          (when-let [mails (read-mails (get req "email") (get req "token"))]
            (clojure.data.json/json-str mails))))

  (route/not-found "Not Found"))
