CREATE TABLE IF NOT EXISTS one_time_tokens (
    token_value varchar(64) NOT NULL PRIMARY KEY,
    username varchar(320) NOT NULL,
    expires_at timestamp with time zone NOT NULL
);
CREATE INDEX IF NOT EXISTS one_time_tokens_expiry_idx ON one_time_tokens (expires_at);

CREATE TABLE IF NOT EXISTS app_one_time_token (
    token_value varchar(64) NOT NULL PRIMARY KEY,
    username varchar(320) NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    purpose varchar(32) NOT NULL DEFAULT 'AUTHENTICATION'
);
CREATE INDEX IF NOT EXISTS app_one_time_token_expiry_idx ON app_one_time_token (expires_at);

CREATE TABLE IF NOT EXISTS legal_document_release (
    version integer PRIMARY KEY CHECK (version > 0),
    published_on date NOT NULL,
    content text NOT NULL,
    content_hash varchar(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS legal_document_acceptance (
    user_id uuid NOT NULL,
    version integer NOT NULL REFERENCES legal_document_release(version),
    accepted_at timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, version)
);
