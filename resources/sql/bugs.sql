-- A :result value of :n below will return affected rows:
-- :name insert-bug :i!
-- :doc Insert a single bug returning affected row count
INSERT INTO bugs (name, short_description)
VALUES (:name, :short-description)

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
