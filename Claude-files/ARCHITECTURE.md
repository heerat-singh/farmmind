# FarmMind — Architecture Document
> For Claude Code: Read this before making any structural changes to the codebase.

---

## 1. System Overview

FarmMind is a multi-platform application (iOS, Android, Web) with a Java/Spring Boot backend deployed on AWS. The system has two major subsystems that share infrastructure:

- **Phase 1:** AI Crop Advisor — conversational AI + photo/voice input
- **Phase 2:** FarmToTable Marketplace — farm listings, orders, payments

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                             │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │     React Native (Expo) — iOS | Android | Web             │  │
│  └───────────────────────────────────────────────────────────┘  │
└──────────────────────────────┬──────────────────────────────────┘
                               │ HTTPS / REST
┌──────────────────────────────▼──────────────────────────────────┐
│                    AWS API GATEWAY                              │
│              (Rate limiting, auth validation)                   │
└──────┬──────────────────────┬──────────────────────┬────────────┘
       │                      │                      │
┌──────▼──────┐    ┌──────────▼──────┐    ┌─────────▼──────────┐
│ api-gateway │    │   ai-service    │    │ marketplace-service │
│ Spring Boot │    │  Spring Boot    │    │   Spring Boot       │
│  (ECS)      │    │    (ECS)        │    │     (ECS) Ph2       │
└──────┬──────┘    └──────────┬──────┘    └─────────┬──────────┘
       │                      │                      │
┌──────▼──────────────────────▼──────────────────────▼──────────┐
│                    SHARED DATA LAYER                           │
│  ┌─────────────────┐  ┌─────────────┐  ┌───────────────────┐  │
│  │  PostgreSQL RDS  │  │ ElastiCache │  │     S3 Bucket     │  │
│  │  + pgvector      │  │   (Redis)   │  │  (photos, docs)   │  │
│  └─────────────────┘  └─────────────┘  └───────────────────┘  │
└────────────────────────────────────────────────────────────────┘
       │                      │
┌──────▼──────┐    ┌──────────▼──────┐
│ AWS Cognito │    │    AWS SQS      │
│   (Auth)    │    │ (Async jobs)    │
└─────────────┘    └─────────────────┘
```

---

## 2. Service Breakdown

### 2.1 api-gateway Service (Port 8080)
The main entry point for all non-AI requests.

**Responsibilities:**
- User registration, login (delegates to Cognito)
- Farm profile CRUD
- Conversation history storage and retrieval
- Subscription management (Stripe)
- User settings, language preference
- Proxies AI requests to ai-service asynchronously via SQS

**Key endpoints:**
```
POST   /api/v1/auth/register
POST   /api/v1/auth/login
GET    /api/v1/auth/me

POST   /api/v1/farms                    # Create farm profile
GET    /api/v1/farms/{id}
PUT    /api/v1/farms/{id}

GET    /api/v1/conversations            # Get conversation history
POST   /api/v1/conversations            # Start new conversation
GET    /api/v1/conversations/{id}/messages

POST   /api/v1/subscriptions/checkout  # Create Stripe checkout session
POST   /api/v1/webhooks/stripe          # Stripe webhook receiver

GET    /api/v1/weather                  # Proxy to Tomorrow.io (cached)
```

### 2.2 ai-service (Port 8081)
Handles all AI operations. Isolated so it can scale independently and be swapped out.

**Responsibilities:**
- Receive question + context from SQS
- Retrieve relevant knowledge from pgvector (RAG)
- Build prompt with farm context + weather + retrieved knowledge
- Call Claude API for response
- Call Claude Vision API for photo diagnosis
- Call Whisper API for voice transcription
- Store AI response back to PostgreSQL
- Push SSE (Server-Sent Events) notification to client that response is ready

**Internal flow:**
```
SQS Message Received
        ↓
Extract: userId, farmId, questionText, imageS3Key (optional), language
        ↓
Fetch farm profile from DB (crop types, location, acreage)
        ↓
Fetch weather forecast from cache (or Tomorrow.io)
        ↓
Vector search pgvector for top-5 relevant knowledge chunks
        ↓
Build system prompt (see prompt templates in /ai-service/prompts/)
        ↓
Call Claude API (with image if provided)
        ↓
Save response to conversations table
        ↓
Emit SSE event → client receives response
```

### 2.3 marketplace-service (Port 8082) — Phase 2
Handles all marketplace operations. Built separately from day one so Phase 2 is a clean addition.

**Responsibilities:**
- Farm listing CRUD
- Buyer account management
- Order lifecycle (placed → confirmed → fulfilled)
- Stripe Connect onboarding for farmers
- Payment splits (farmer gets 94%, platform keeps 6%)
- Delivery scheduling

---

## 3. Database Schema

### Core Tables (Phase 1)

```sql
-- Users (synced from Cognito)
CREATE TABLE users (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cognito_id  VARCHAR(255) UNIQUE NOT NULL,
    email       VARCHAR(255) UNIQUE NOT NULL,
    full_name   VARCHAR(255),
    phone       VARCHAR(20),
    language    VARCHAR(10) DEFAULT 'en',  -- en, pa, hi, fr
    role        VARCHAR(20) DEFAULT 'farmer',  -- farmer, buyer, admin
    stripe_customer_id VARCHAR(255),
    subscription_tier  VARCHAR(20) DEFAULT 'free',  -- free, grower, pro
    subscription_expires_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ DEFAULT NOW(),
    updated_at  TIMESTAMPTZ DEFAULT NOW()
);

-- Farm Profiles
CREATE TABLE farms (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id        UUID REFERENCES users(id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    latitude        DECIMAL(10, 8) NOT NULL,
    longitude       DECIMAL(11, 8) NOT NULL,
    province        VARCHAR(50),
    climate_zone    VARCHAR(20),  -- derived from lat/lng on create
    acreage         DECIMAL(10, 2),
    farm_type       VARCHAR(50),  -- vegetable, grain, orchard, greenhouse, mixed
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_at      TIMESTAMPTZ DEFAULT NOW()
);

-- Crops on a farm (many per farm)
CREATE TABLE farm_crops (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id     UUID REFERENCES farms(id) ON DELETE CASCADE,
    crop_name   VARCHAR(100) NOT NULL,  -- tomatoes, corn, soybeans
    variety     VARCHAR(100),
    planting_date DATE,
    expected_harvest_date DATE,
    acreage_planted DECIMAL(8,2),
    is_active   BOOLEAN DEFAULT true
);

-- Conversations
CREATE TABLE conversations (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID REFERENCES users(id) ON DELETE CASCADE,
    farm_id     UUID REFERENCES farms(id),
    title       VARCHAR(255),  -- auto-generated from first message
    created_at  TIMESTAMPTZ DEFAULT NOW()
);

-- Messages (one conversation has many messages)
CREATE TABLE messages (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID REFERENCES conversations(id) ON DELETE CASCADE,
    role            VARCHAR(10) NOT NULL,  -- 'user' or 'assistant'
    content         TEXT NOT NULL,
    image_s3_key    VARCHAR(500),   -- if user sent a photo
    voice_s3_key    VARCHAR(500),   -- original audio file
    tokens_used     INTEGER,
    processing_ms   INTEGER,
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

-- Knowledge base chunks (RAG)
CREATE TABLE knowledge_chunks (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    source      VARCHAR(100),   -- 'omafra', 'usda', 'extension'
    category    VARCHAR(100),   -- 'pest', 'disease', 'nutrition', 'planting'
    crop        VARCHAR(100),   -- null = applies to all crops
    province    VARCHAR(50),    -- null = applies everywhere
    title       VARCHAR(255),
    content     TEXT NOT NULL,
    embedding   vector(1536),   -- pgvector (AWS Titan embedding dimension)
    created_at  TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX ON knowledge_chunks USING ivfflat (embedding vector_cosine_ops);

-- Subscriptions log
CREATE TABLE subscription_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID REFERENCES users(id),
    stripe_event_id VARCHAR(255) UNIQUE,
    event_type      VARCHAR(50),  -- 'checkout.completed', 'invoice.paid', 'cancelled'
    tier            VARCHAR(20),
    amount_cents    INTEGER,
    created_at      TIMESTAMPTZ DEFAULT NOW()
);
```

### Phase 2 Additional Tables

```sql
-- Farm marketplace listings
CREATE TABLE listings (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    farm_id         UUID REFERENCES farms(id),
    crop_name       VARCHAR(100) NOT NULL,
    variety         VARCHAR(100),
    quantity_kg     DECIMAL(10, 2),
    price_per_kg    DECIMAL(8, 2),
    unit            VARCHAR(20) DEFAULT 'kg',  -- kg, lb, bunch, each
    description     TEXT,
    available_from  DATE,
    available_until DATE,
    pickup_available BOOLEAN DEFAULT true,
    delivery_available BOOLEAN DEFAULT false,
    image_s3_keys   TEXT[],  -- array of S3 keys
    is_active       BOOLEAN DEFAULT true,
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

-- Buyer orders
CREATE TABLE orders (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    buyer_id            UUID REFERENCES users(id),
    listing_id          UUID REFERENCES listings(id),
    quantity_kg         DECIMAL(10, 2) NOT NULL,
    total_amount_cents  INTEGER NOT NULL,
    platform_fee_cents  INTEGER NOT NULL,  -- 6%
    farmer_payout_cents INTEGER NOT NULL,  -- 94%
    status              VARCHAR(30) DEFAULT 'pending',
    -- pending, confirmed, ready, completed, cancelled
    stripe_payment_intent_id VARCHAR(255),
    notes               TEXT,
    pickup_date         DATE,
    created_at          TIMESTAMPTZ DEFAULT NOW(),
    updated_at          TIMESTAMPTZ DEFAULT NOW()
);
```

---

## 4. AI Architecture — RAG Pipeline

### Knowledge Base Ingestion (Run offline, scheduled monthly)

```
OMAFRA PDFs / USDA Web Pages
          ↓
 Python scraper / PDF parser
          ↓
 Text chunker (500 tokens/chunk, 50 token overlap)
          ↓
 AWS Bedrock Titan Embeddings API
          ↓
 pgvector INSERT into knowledge_chunks table
```

### Query-Time RAG Flow

```java
// Simplified pseudocode — full implementation in ai-service/rag/
public AiResponse answerFarmerQuestion(AiRequest request) {

    // 1. Embed the user's question
    float[] questionEmbedding = bedrockClient.embed(request.getQuestion());

    // 2. Vector search — find top 5 most relevant knowledge chunks
    List<KnowledgeChunk> context = knowledgeRepo.findSimilar(
        questionEmbedding,
        request.getFarm().getClimatezone(),
        request.getPrimaryCrop(),
        5  // top-k
    );

    // 3. Build the prompt
    String systemPrompt = promptBuilder.buildSystemPrompt(
        request.getFarm(),
        request.getWeather(),
        context,
        request.getLanguage()
    );

    // 4. Call Claude
    ClaudeResponse response = claudeClient.complete(
        systemPrompt,
        request.getMessages(),  // conversation history
        request.getImageBase64()  // null if no photo
    );

    return new AiResponse(response.getText(), response.getTokensUsed());
}
```

### System Prompt Template

```
You are FarmMind, an expert AI farming advisor specializing in crops grown in [PROVINCE], Canada.
You are currently advising a farmer with the following farm profile:
- Farm name: [FARM_NAME]
- Location: [CITY, PROVINCE]
- Climate zone: [ZONE]
- Current crops: [CROP_LIST]
- Farm size: [ACREAGE] acres
- Farm type: [TYPE]

Current weather at this farm:
- Today: [TEMP_HIGH]/[TEMP_LOW]°C, [CONDITIONS]
- Next 7 days: [WEEKLY_SUMMARY]
- Frost risk: [YES/NO — date if applicable]

Relevant agricultural guidance for this farm and crop:
[RETRIEVED_KNOWLEDGE_CHUNKS]

Instructions:
- Answer in [LANGUAGE]. If the farmer writes in Punjabi or Hindi, respond in kind.
- Give specific, actionable advice. Do not give vague answers.
- If you are recommending a pesticide or fertilizer, always mention the pre-harvest interval (PHI) and safety precautions.
- If you are uncertain, say so clearly and recommend the farmer contact their local OMAFRA extension office.
- Keep responses under 300 words unless the question requires more detail.
- Format your response with clear sections if answering a complex question.
```

---

## 5. Authentication Flow

```
Mobile App                AWS Cognito              api-gateway
     │                         │                        │
     │── Register/Login ──────▶│                        │
     │◀── JWT (id + access) ───│                        │
     │                         │                        │
     │── API Request + Bearer JWT ──────────────────────▶│
     │                         │◀─ Verify JWT ──────────│
     │                         │── Valid/Invalid ───────▶│
     │◀────────────────── Response ─────────────────────│
```

- **Access token:** Short-lived (1 hour). Sent with every request.
- **Refresh token:** Long-lived (30 days). Stored securely in device keychain.
- **Cognito User Pools:** Handles password reset, email verification, MFA.
- **Social login:** Add Google OAuth via Cognito in Phase 2 (farmers prefer Google login).

---

## 6. File Upload Flow (Photo Diagnosis)

```
Mobile App                api-gateway              S3              ai-service
     │                         │                   │                   │
     │── POST /upload/presign ─▶│                   │                   │
     │◀── Presigned S3 URL ─────│                   │                   │
     │                         │                   │                   │
     │── PUT image directly ──────────────────────▶│                   │
     │◀── 200 OK ────────────────────────────────────                   │
     │                         │                   │                   │
     │── POST /messages (s3Key)▶│                   │                   │
     │                         │── SQS message ────────────────────────▶│
     │                         │                   │  Claude fetches   │
     │                         │                   │◀── image from S3 ─│
     │◀──── SSE: response ready ─────────────────────────────────────── │
```

**Why presigned URLs?** The image goes directly from phone to S3, never through your server. This reduces bandwidth costs and server load significantly.

---

## 7. Scalability Design

### Phase 1 (0–1,000 users): Minimal infra
- 1x ECS Fargate task per service (auto-scales to 3x on load)
- RDS: db.t3.medium (~$50/mo)
- ElastiCache: cache.t3.micro (~$15/mo)
- Estimated total AWS cost: **$150–250/month**

### Phase 2 (1,000–10,000 users): Scale data layer
- RDS: db.r5.large with read replica
- ElastiCache: Multi-AZ
- ai-service: dedicated cluster with higher CPU (Claude API calls are CPU-light but I/O-heavy)
- Estimated total AWS cost: **$600–1,000/month**

### Cost control mechanisms
- Cache weather API responses per location for 30 minutes (reduces Tomorrow.io calls)
- Cache AI responses for identical questions from same farm context
- Rate limit free tier users at API Gateway level (not app level — harder to circumvent)
- Use SQS for AI jobs — prevents thundering herd if many users ask simultaneously

---

## 8. Security Considerations

| Threat | Mitigation |
|---|---|
| API key exposure | All keys in AWS Secrets Manager, never in code |
| Prompt injection | Input sanitization before building prompts; system prompt locked |
| Unauthorized data access | Row-level security: every DB query filters by userId |
| Photo content safety | Claude vision rejects non-plant images gracefully |
| Free tier abuse | IP + device fingerprint rate limiting at API Gateway |
| PIPEDA compliance (Canadian privacy law) | Data stored in ca-central-1; privacy policy on app; opt-in for data sharing |

---

## 9. CI/CD Pipeline

```yaml
# .github/workflows/deploy.yml (simplified)
on:
  push:
    branches: [main]

jobs:
  test:
    - Run: mvn test (backend)
    - Run: npm test (mobile)
    - Run: SonarQube scan

  build:
    - Docker build → push to AWS ECR

  deploy:
    - ECS update-service (zero-downtime rolling deploy)
    - Run DB migrations (Flyway)
    - Invalidate CloudFront cache
```

---

## 10. Disaster Recovery

- **RDS:** Automated daily snapshots, retained 7 days
- **S3:** Versioning enabled on uploads bucket
- **RTO (Recovery Time Objective):** 1 hour
- **RPO (Recovery Point Objective):** 24 hours
- **Multi-AZ:** Enabled for RDS in production (not dev/staging)

---

*Architecture version: 1.0 | April 2026*
