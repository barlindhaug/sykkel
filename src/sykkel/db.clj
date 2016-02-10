(ns sykkel.db
  (:require
    [clojure.java.jdbc :as jdbc]))

(defn options [] {
  :host (System/getenv "DB_HOST")
  :port (System/getenv "DB_PORT")
  :user (System/getenv "DB_USER")
  :password (System/getenv "DB_PASS")})

(defn create-connection-definition []
  (let [{:keys [user password host port]} (options)]
    {:classname "org.postgresql.Driver"
     :subprotocol "postgresql"
     :subname (str "//" host ":" port)
     :user user
     :password password}))


(defn users []
  (jdbc/query (create-connection-definition)
              ["SELECT * FROM users"]))

(defn user-token [strava-id]
  (:token (first
            (jdbc/query (create-connection-definition)
              ["SELECT token FROM users where strava_id = ?" strava-id]))))

(defn insert-row [table row]
  (jdbc/with-db-transaction [transaction (create-connection-definition)]
    (let [result (jdbc/update! transaction table row ["strava_id = ?" (:strava_id row)])]
      (if (zero? (first result))
        (jdbc/insert! transaction table row)))))

(defn insert-user [user-row]
  (insert-row :users user-row))

(defn insert-activity [activity-row]
  (insert-row :activities activity-row))
