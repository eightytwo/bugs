-- :name insert-bug :i!
-- :doc Insert a single bug returning affected row count
INSERT INTO bugs (name, short_description, tag, age, rating)
VALUES (:name, :short-description, :tag::enum_tags, :age, :rating)

-- :name get-bugs :? :*
-- :doc Get bug by id
SELECT  *
FROM    bugs

-- :name get-bug-by-id :? :1
-- :doc Get bug by id
SELECT  *
FROM    bugs
WHERE   id = :id
