-- :name get-latest-migration :? :1
-- :doc Get the latest migration
SELECT   id
FROM     ragtime_migrations
ORDER BY created_at DESC
LIMIT    1
