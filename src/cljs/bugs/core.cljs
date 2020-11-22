(ns ^:figwheel-hooks bugs.core)

(enable-console-print!)

(defn say-hello
  [text]
  (let [message (str "Hello, " text "?")]
    (js/console.log message)
    message))

(defn mount
  [el]
  (set! (.-innerHTML el)
        (str (say-hello "world") " (from ClojureScript)")))

(defn mount-app-element
  []
  (when-let [el (.getElementById js/document "app")]
    (mount el)))

(mount-app-element)

;; specify reload hook with ^;after-load metadata
(defn ^:after-load on-reload []
  (mount-app-element)
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
