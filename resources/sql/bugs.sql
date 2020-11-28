-- A :result value of :n below will return affected rows:
-- :name insert-bug :i!
-- :doc Insert a single bug returning affected row count
INSERT INTO bugs (name)
VALUES (:name)

-- :name get-bugs :n
-- :doc Get bug by id
SELECT  *
FROM    bugs

-- A ":result" value of ":1" specifies a single record
-- (as a hashmap) will be returned
-- :name get-bug-by-id :? :1
-- :doc Get bug by id
SELECT  *
FROM    bugs
WHERE   id = :id

-- Let's specify some columns with the
-- identifier list parameter type :i* and
-- use a value list parameter type :v* for IN()
-- :name bugs-by-ids-specify-cols :? :*
-- :doc Bugs with returned columns specified
SELECT  :i*:cols
FROM    bugs
WHERE   id IN (:v*:ids)
