--create it as non-root user in a non-default database!
CREATE TABLE todo (
    id bigserial PRIMARY KEY,
    name text NOT NULL,
    description text
);