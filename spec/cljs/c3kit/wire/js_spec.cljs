(ns c3kit.wire.js-spec
  (:require-macros [speclj.core :refer [context describe it should should-be-nil should-not should=]])
  (:require [c3kit.apron.time :as time]
            [c3kit.wire.js :as sut]
            [speclj.core]))

(describe "JavaScript"

  (it "->query-string"
    (should= "" (sut/->query-string nil))
    (should= "" (sut/->query-string {}))
    (should= "hello=%3Aworld" (sut/->query-string {:hello :world}))
    (should= "goodbye=cruel%20world" (sut/->query-string {"goodbye" "cruel world"}))
    (should= "one=1&two=2&pi=3.1415926" (sut/->query-string {:one 1 :two 2 :pi 3.1415926}))
    (should= "nothing%3F=" (sut/->query-string {:nothing? nil}))
    (should= "date=%23inst%20%222022-03-04T23%3A59%3A02.000-00%3A00%22" (sut/->query-string {:date (time/parse :ref3339 "2022-03-04T23:59:02-00:00")})))

  (it "encode-url"
    (should= "root" (sut/encode-url "root" nil))
    (should= "root" (sut/encode-url "root" {}))
    (should= "root?foo=%3Abar" (sut/encode-url "root" {:foo :bar})))

  (context "cookies"
    (it "nil"
      (with-redefs [sut/doc-cookie (constantly nil)]
        (should= {} (sut/cookies))))

    (it "empty"
      (with-redefs [sut/doc-cookie (constantly "")]
        (should= {} (sut/cookies))))

    (it "one cookie"
      (with-redefs [sut/doc-cookie (constantly "hello=world")]
        (should= {"hello" "world"} (sut/cookies))))

    (it "two cookies"
      (with-redefs [sut/doc-cookie (constantly "foo=bar; cheese=whiz; ")]
        (should= {"foo" "bar" "cheese" "whiz"} (sut/cookies))))

    (it "three cookies"
      (with-redefs [sut/doc-cookie (constantly "foo=bar; hello=world; cheese=whiz; ")]
        (should= {"hello" "world" "foo" "bar" "cheese" "whiz"} (sut/cookies))))
    )

  (context "e-value"
    (it "checkbox"
      (let [checked   (clj->js {:target {:type "checkbox" :checked true}})
            unchecked (clj->js {:target {:type "checkbox" :checked false}})]
        (should (sut/e-value checked))
        (should-not (sut/e-value unchecked))))

    (it "date"
      (let [mar-16  (clj->js {:target {:type "date" :value "1990-03-16"}})
            dec-25  (clj->js {:target {:type "date" :value "2022-12-25"}})
            jul-4   (clj->js {:target {:type "date" :value "2203-07-04"}})
            blank   (clj->js {:target {:type "date" :value "\r\n\t "}})
            garbage (clj->js {:target {:type "date" :value "garbage"}})]
        (should= (time/utc 1990 3 16) (sut/e-value mar-16))
        (should= (time/utc 2022 12 25) (sut/e-value dec-25))
        (should= (time/utc 2203 7 4) (sut/e-value jul-4))
        (should-be-nil (sut/e-value blank))
        (should-be-nil (sut/e-value garbage))))

    (it "text"
      (let [hello (clj->js {:target {:type "text" :value "hello"}})
            world (clj->js {:target {:type "text" :value "world"}})
            foo   (clj->js {:target {:type "text" :value "foo bar baz"}})]
        (should= "hello" (sut/e-value hello))
        (should= "world" (sut/e-value world))
        (should= "foo bar baz" (sut/e-value foo))))
    )

  )
