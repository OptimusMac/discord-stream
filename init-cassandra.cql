CREATE KEYSPACE IF NOT EXISTS discord
WITH REPLICATION = {
  'class' : 'SimpleStrategy',
  'replication_factor' : 1
};

USE discord;

CREATE TABLE IF NOT EXISTS messages (
    id UUID,
    create_at TIMESTAMP,
    content TEXT,
    content_id TEXT,
    PRIMARY KEY (id, create_at)
) WITH CLUSTERING ORDER BY (create_at DESC);

CREATE INDEX IF NOT EXISTS messages_content_id_idx ON messages (content_id);