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
ALTER TABLE app_one_time_token ADD COLUMN IF NOT EXISTS purpose varchar(32) NOT NULL DEFAULT 'AUTHENTICATION';
CREATE INDEX IF NOT EXISTS app_one_time_token_expiry_idx ON app_one_time_token (expires_at);
