(ns sykkel.core-test
  (:require [clojure.test :refer :all]
            [clj-time.core :as time]
            [sykkel.core :as core]))

(def test-challenges [{:id 3
                       :end_date (time/date-time 2018 01 01)}
                      {:id 2
                       :end_date (time/date-time 2017 01 01)}
                      {:id 0
                       :end_date (time/date-time 2015 01 01)}
                      {:id 1
                       :end_date (time/date-time 2016 01 01)}])

(defn extract-ids [challenges]
  (map #(:id %) challenges))

(deftest oldest-end-date-first
    (is (=
          (extract-ids (core/oldest-first test-challenges))
          [0 1 2 3])))

(deftest oldest-end-date-last
    (is (=
          (extract-ids (core/newest-first test-challenges))
          [3 2 1 0])))

(deftest past-challenges-last
  (let [split-challenges (core/split-at-now test-challenges)]
    (is (=
          (extract-ids (first split-challenges))
          [3 2]))
    (is (=
          (extract-ids (second split-challenges))
          [0 1]))))

(deftest sort-challenges
  (is (=
        (extract-ids (core/sort-challenges test-challenges))
        [2 3 1 0])))
