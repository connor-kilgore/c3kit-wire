(ns c3kit.wire.google
  (:require [c3kit.apron.log :as log]
            [c3kit.wire.js :as wjs]
            [reagent.core :as reagent]
            [reagent.dom :as reagent-dom]))

;; https://developers.google.com/identity/gsi/web/reference/html-reference#element_with_class_g_id_signin

(defn account-id []
  (wjs/o-get-in js/window ["google" "accounts" "id"]))

(defn render-button [account-id node options]
  (.renderButton account-id node options))

(defn mount-oauth-button [node options]
  (if-let [google-id (account-id)]
    (render-button google-id node (clj->js options))
    (log/warn "window.google.accounts.id doesn't exist")))

(defn- on-button-mount [options this]
  (let [node (reagent-dom/dom-node this)]
    (if (wjs/doc-ready?)
      (mount-oauth-button node options)
      (wjs/add-listener js/window "load" (partial mount-oauth-button node options)))))

(defn oauth-button
  "Renders the Google OAuth Button."
  [options _body]
  (reagent/create-class
    {:component-did-mount (partial on-button-mount options)
     :reagent-render      (fn [_options body] body)}))
