(ns sykkel.db
  (:require
    [clj-time.coerce :as time-coerce]
    [clj-time.format :as time-format]
    [clojure.java.jdbc :as jdbc]))

(defn options [] {
  :host (System/getenv "DB_HOST")
  :port (System/getenv "DB_PORT")
  :user (System/getenv "DB_USER")
  :password (System/getenv "DB_PASS")})

(defn to-timestamp [datestring]
  (time-coerce/to-timestamp
    (time-format/parse datestring)))

(defn to-datetime [timestamp]
  (time-coerce/from-sql-time timestamp))

(defn create-connection-definition []
  (let [{:keys [user password host port]} (options)]
    {:classname "org.postgresql.Driver"
     :subprotocol "postgresql"
     :subname (str "//" host ":" port)
     :user user
     :password password}))

(defn user [athlete-id]
  (first
    (jdbc/query (create-connection-definition)
      ["SELECT * FROM users WHERE strava_id = ?" athlete-id])))

(defn users []
  (jdbc/query (create-connection-definition)
              ["SELECT * FROM users"]))

(defn user-token [strava-id]
  (:token (first
            (jdbc/query (create-connection-definition)
              ["SELECT token FROM users where strava_id = ?" strava-id]))))

(defn activities [start-date end-date activity-type]
    (let [start-date-sql (time-coerce/to-sql-time start-date)
          end-date-sql (time-coerce/to-sql-time end-date)]
      (jdbc/query (create-connection-definition)
                  ["SELECT * FROM activities WHERE start_date >= ? and start_date < ? AND type = ?"
                   start-date-sql end-date-sql activity-type])))

(defn insert-row [table row]
  (try
    (jdbc/with-db-transaction [transaction (create-connection-definition)]
      (let [result (jdbc/update! transaction table row ["strava_id = ?" (:strava_id row)])]
        (if (zero? (first result))
          (jdbc/insert! transaction table row))))
    (catch Exception e ((.printStackTrace (.getNextException e))))))

(defn insert-user [user-row]
  (insert-row :users user-row))

(defn insert-activity [activity-row]
  (insert-row :activities activity-row))
