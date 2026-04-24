-- Enable extensions
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "vector";

-- Users (synced from Cognito)
CREATE TABLE users (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cognito_id              VARCHAR(255) UNIQUE NOT NULL,
    email                   VARCHAR(255) UNIQUE NOT NULL,
    full_name               VARCHAR(255),
    phone                   VARCHAR(20),
    language                VARCHAR(10) NOT NULL DEFAULT 'en',
    role                    VARCHAR(20) NOT NULL DEFAULT 'farmer',
    stripe_customer_id      VARCHAR(255),
    subscription_tier       VARCHAR(20) NOT NULL DEFAULT 'free',
    subscription_expires_at TIMESTAMPTZ,
    questions_this_month    INTEGER NOT NULL DEFAULT 0,
    voice_this_month        INTEGER NOT NULL DEFAULT 0,
    photos_this_month       INTEGER NOT NULL DEFAULT 0,
    usage_reset_at          TIMESTAMPTZ NOT NULL DEFAULT date_trunc('month', NOW()) + INTERVAL '1 month',
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Farm profiles
CREATE TABLE farms (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id        UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    latitude        DECIMAL(10, 8) NOT NULL,
    longitude       DECIMAL(11, 8) NOT NULL,
    province        VARCHAR(50),
    climate_zone    VARCHAR(20),
    acreage         DECIMAL(10, 2),
    farm_type       VARCHAR(50),
    is_active       BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_farms_owner_id ON farms(owner_id);

-- Crops on a farm
CREATE TABLE farm_crops (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id               UUID NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    crop_name             VARCHAR(100) NOT NULL,
    variety               VARCHAR(100),
    planting_date         DATE,
    expected_harvest_date DATE,
    acreage_planted       DECIMAL(8, 2),
    is_active             BOOLEAN NOT NULL DEFAULT true,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_farm_crops_farm_id ON farm_crops(farm_id);

-- Conversations
CREATE TABLE conversations (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    farm_id    UUID REFERENCES farms(id) ON DELETE SET NULL,
    title      VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_conversations_user_id ON conversations(user_id);
CREATE INDEX idx_conversations_user_id_created ON conversations(user_id, created_at DESC);

-- Messages
CREATE TABLE messages (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    role            VARCHAR(10) NOT NULL CHECK (role IN ('user', 'assistant')),
    content         TEXT NOT NULL,
    image_s3_key    VARCHAR(500),
    voice_s3_key    VARCHAR(500),
    tokens_used     INTEGER,
    processing_ms   INTEGER,
    feedback        SMALLINT CHECK (feedback IN (-1, 1)),
    feedback_reason VARCHAR(100),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_messages_conversation_id ON messages(conversation_id);
CREATE INDEX idx_messages_conversation_id_created ON messages(conversation_id, created_at ASC);

-- Knowledge base chunks (RAG)
CREATE TABLE knowledge_chunks (
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source    VARCHAR(100),
    category  VARCHAR(100),
    crop      VARCHAR(100),
    province  VARCHAR(50),
    title     VARCHAR(255),
    content   TEXT NOT NULL,
    embedding vector(1536),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX ON knowledge_chunks USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
CREATE INDEX idx_knowledge_chunks_crop ON knowledge_chunks(crop);
CREATE INDEX idx_knowledge_chunks_province ON knowledge_chunks(province);

-- Subscription events log
CREATE TABLE subscription_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id),
    stripe_event_id VARCHAR(255) UNIQUE,
    event_type      VARCHAR(50),
    tier            VARCHAR(20),
    amount_cents    INTEGER,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_subscription_events_user_id ON subscription_events(user_id);
