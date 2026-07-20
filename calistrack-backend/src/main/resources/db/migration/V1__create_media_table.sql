-- Media assets: metadata only. Binaries live in the configured storage provider.
CREATE TABLE IF NOT EXISTS media (
    id                  UUID            PRIMARY KEY,
    owner_user_id       UUID            NOT NULL,
    provider            VARCHAR(32)     NOT NULL,
    bucket_name         VARCHAR(255),
    public_id           VARCHAR(512)    NOT NULL,
    original_filename   VARCHAR(512),
    mime_type           VARCHAR(128)    NOT NULL,
    extension           VARCHAR(32),
    file_size_bytes     BIGINT,
    width               INTEGER,
    height              INTEGER,
    duration_seconds    NUMERIC(12, 3),
    resource_type       VARCHAR(32)     NOT NULL,
    media_type          VARCHAR(64)     NOT NULL,
    secure_url          TEXT,
    thumbnail_url       TEXT,
    checksum            VARCHAR(128),
    upload_status       VARCHAR(32)     NOT NULL,
    visibility          VARCHAR(32)     NOT NULL,
    provider_metadata   JSONB           NOT NULL DEFAULT '{}'::jsonb,
    created_at          TIMESTAMPTZ     NOT NULL,
    updated_at          TIMESTAMPTZ     NOT NULL,
    deleted_at          TIMESTAMPTZ,

    CONSTRAINT fk_media_owner_user
        FOREIGN KEY (owner_user_id) REFERENCES app_user (id)
);

CREATE INDEX IF NOT EXISTS idx_media_owner_user_id ON media (owner_user_id);
CREATE INDEX IF NOT EXISTS idx_media_upload_status ON media (upload_status);
CREATE INDEX IF NOT EXISTS idx_media_provider_public_id ON media (provider, public_id);
CREATE INDEX IF NOT EXISTS idx_media_deleted_at ON media (deleted_at);
