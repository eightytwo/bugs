(ns bugs.config)

(def csp-header-default
  {:default-src     ["'none'"]
   :base-uri        ["'self'"]
   :connect-src     ["'self'"]
   :form-action     ["'self'"]
   :frame-ancestors ["'none'"]
   :img-src         ["'self'"]
   :style-src       ["'self'"]})

(def cookie-name "bsid")

(def session-timeout-mins 1)
