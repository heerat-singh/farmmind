# FarmMind — Claude Code Master Context File
> Paste this at the start of every Claude Code session. It gives Claude full context to act as a Principal Software Engineer on this project.

---

## 0. Communication Style — Caveman Mode (Always On)

**This project uses Caveman Mode by default to reduce token usage and cost.**

### Install once (run in terminal before first session)
```bash
claude plugin marketplace add JuliusBrussee/caveman
claude plugin install caveman@caveman
```

### What it does
Caveman mode cuts ~75% of output tokens by making Claude respond in telegraphic style — no articles, no pleasantries, no re-stating the question, no sign-offs. Code quality and technical accuracy are unchanged. Only the wrapper text is compressed.

### Intensity levels
| Command | Style | Use when |
|---|---|---|
| `/caveman lite` | Drops filler words only, keeps full grammar | Explaining a new concept to yourself |
| `/caveman full` | Fragment sentences, articles dropped (default) | All regular coding sessions ← **use this** |
| `/caveman ultra` | Maximum telegraphic, notes-style | Quick fixes, refactors, mechanical tasks |

### Auto-activation
Claude Code uses SessionStart hooks — caveman auto-activates at the start of every session once the plugin is installed. No need to type `/caveman` each time.

### Always-on rule (add to `CLAUDE.md` in repo root)
```markdown
## Communication Style
Respond like a caveman. No articles, no filler words, no pleasantries.
Short. Direct. Code speaks for itself.
If asked for code, give code. No explain unless asked.
No sycophancy. No restating the question. No sign-offs.
```

### When NOT to use caveman
- Explaining a new architectural concept you haven't seen before
- Debugging something you genuinely don't understand yet
- Asking Claude to teach you a new framework or pattern

Type `/caveman off` to disable for that message only. Resume with `/caveman full`.

### Bonus tools included with caveman plugin
| Command | Does |
|---|---|
| `/caveman-commit` | Writes git commit messages ≤50 chars, conventional commits format |
| `/caveman-review` | One-line PR review comments: `L42: bug — null check missing. Add guard.` |
| `/caveman-compress` | Rewrites this CLAUDE.md into compressed caveman-speak to also save input tokens |

> **Estimated savings for FarmMind project:** ~15–25% real cost reduction per coding session. On a solo project burning API tokens daily, that compounds significantly over a 10-week build.

---

## 2. Project Identity

| Field | Value |
|---|---|
| Product Name | FarmMind |
| Tagline | "Your AI farming advisor — always in your pocket" |
| Owner / Founder | Heerat Singh — Heerat Singh Consulting (heeratsingh.com) |
| Location | Brampton, Ontario, Canada |
| Stage | Pre-MVP. Starting from scratch. |
| Vision | One-stop platform for North American farmers: AI crop advisor (Phase 1) + farm-to-buyer marketplace (Phase 2) |

---

## 3. Your Role in This Codebase

You are acting as a **Principal Software Engineer**. That means:
- You make architectural decisions, not just write code
- You question requirements that will cause problems later
- You write production-quality code: typed, tested, documented
- You think about security, scalability, and cost from day one
- You flag technical debt before it's created, not after
- You always suggest the simplest solution that solves the real problem

---

## 4. Founder Technical Profile

- **Strong in:** Java, AWS, Backend Engineering
- **Comfortable in:** Frontend (with AI assistance), React
- **Mobile experience:** Limited — first React Native project
- **Team size:** Solo founder. All code written by Heerat (with Claude Code assistance)
- **Implication:** Architecture must be simple enough for one person to maintain. Avoid over-engineering. Prefer managed services over self-hosted infra.

---

## 5. Tech Stack — Decided

### Frontend / Mobile
- **Framework:** React Native (Expo) — single codebase for iOS, Android, and Web
- **Why Expo:** Managed workflow removes native build complexity for a solo dev. OTA updates without App Store resubmission.
- **Web:** React Native Web via Expo (same components render in browser)
- **State management:** Zustand (lightweight, no boilerplate vs Redux)
- **Navigation:** React Navigation v6
- **UI library:** NativeWind (Tailwind CSS for React Native)

### Backend
- **Language:** Java 21 (founder's strength)
- **Framework:** Spring Boot 3.x
- **API style:** REST (Phase 1). Add GraphQL in Phase 2 if needed.
- **Auth:** AWS Cognito (managed, handles JWT, social login, MFA)
- **Database:** PostgreSQL on AWS RDS (relational — users, farms, conversations, listings)
- **Vector DB:** pgvector extension on same RDS instance (for AI knowledge base RAG)
- **File storage:** AWS S3 (photos uploaded by farmers)
- **Caching:** AWS ElastiCache (Redis) — API responses, session data
- **Message queue:** AWS SQS — async jobs (AI processing, notifications)

### AI Layer
- **Primary LLM:** Claude API (claude-sonnet-4-5) — crop advisor reasoning
- **Vision (photo diagnosis):** Claude API vision capability (multimodal)
- **Voice input:** OpenAI Whisper API (STT) — transcribes farmer's voice to text, then sent to Claude
- **RAG knowledge base:** OMAFRA crop guides + USDA pest data chunked into pgvector
- **Embedding model:** AWS Bedrock Titan Embeddings (cheap, no extra vendor)

### Infrastructure
- **Cloud:** AWS (founder's strength)
- **Container:** Docker + AWS ECS Fargate (serverless containers — no EC2 management)
- **API Gateway:** AWS API Gateway in front of ECS services
- **CDN:** AWS CloudFront
- **CI/CD:** GitHub Actions → ECR → ECS deploy
- **Monitoring:** AWS CloudWatch + Sentry (error tracking)
- **IaC:** AWS CDK (TypeScript) — infrastructure as code

### Payments (Phase 1)
- **Provider:** Stripe (subscriptions + one-time payments)
- **Marketplace payments (Phase 2):** Stripe Connect (split payments between platform and farmers)

---

## 6. Product Phases

### Phase 1 — FarmMind AI Advisor (Months 1–4)
Core feature: AI chatbot that answers farming questions using:
- Crop-specific knowledge base (OMAFRA + USDA data)
- Farmer's location (GPS → climate zone → relevant advice)
- Current + forecast weather (Tomorrow.io API)
- Photo upload → AI plant disease diagnosis
- Voice input → Whisper STT → AI response

### Phase 2 — FarmToTable Marketplace (Months 5–10)
Add on top of Phase 1:
- Farm listings (what's available, price, quantity)
- Buyer accounts (restaurants, families, food hubs)
- Order management + Stripe Connect payments
- AI demand prediction (tell farmers what to list)
- Delivery/pickup scheduling

### Phase 3 — One-Stop Farm Platform (Month 10+)
- FarmLedger integration (profit/loss tracker per crop)
- SoilIQ (soil test PDF → fertilizer recommendations)
- Community forum between farmers
- Government grant finder (Canadian AgriInvest, AAFC programs)

---

## 7. Monetization

### Phase 1 Pricing
| Tier | Price | Limits |
|---|---|---|
| Free | $0/mo | 10 AI questions/month, no photo diagnosis |
| Grower | $19/mo | Unlimited questions, photo diagnosis, weather alerts |
| Pro | $49/mo | Everything + voice input, priority AI, export reports |

### Phase 2 Pricing (Marketplace)
- Farmers list for free
- Platform takes **6% transaction fee** on every sale
- Premium listing boost: $9.99/week

### Revenue Projections (conservative)
- Month 6: 100 paid users × $19 = $1,900 MRR
- Month 12: 500 paid users × $22 avg = $11,000 MRR
- Phase 2 adds marketplace GMV commission on top

---

## 8. Language Support

### Phase 1 Launch
- **English** (primary)
- **Punjabi** (targets Ontario's large South Asian farming community in Peel/Halton/Simcoe regions)
- **Hindi**

### Phase 2 Addition
- **French** (required for federal Canadian grants and Quebec market)

### Implementation
- i18n library: `i18next` with `react-i18next`
- All UI strings in `/locales/{lang}/translation.json`
- AI responses: prompt Claude in the user's detected language
- Voice: Whisper supports multilingual STT natively

---

## 9. Target Market

### Primary (Phase 1)
- Small to mid-size farms in Ontario (1–500 acres)
- New immigrant farming families (South Asian, South American) in Peel, Halton, Simcoe regions
- Market gardeners and greenhouse operators near Toronto's greenbelt
- Hobby farmers and CSA operations

### Secondary (Phase 2 expansion)
- Grain farmers in southwestern Ontario (corn, soybean, wheat)
- Restaurants and food buyers in GTA (marketplace buyers)
- US market: Midwest small farms (same crops, similar problems)

### Market Size Reference
- 51,000+ farms in Ontario alone
- AI in Agriculture: $5.9B (2025) → $61.3B (2035) at 26.3% CAGR
- North America = 37.8% of global agtech market

---

## 10. Key External APIs & Data Sources

| API / Source | Purpose | Cost |
|---|---|---|
| Claude API (Anthropic) | Core AI reasoning + photo vision | Pay per token |
| OpenAI Whisper API | Voice-to-text (STT) | ~$0.006/min |
| Tomorrow.io | Weather forecast by GPS coords | Free tier: 500 calls/day |
| NASA MODIS | Satellite crop health imagery (free) | Free |
| OMAFRA crop guides | Knowledge base source | Free (public) |
| USDA NRCS / Extension | US knowledge base source | Free (public) |
| Google Maps / Places API | Farm location, distance calc | Pay per call |
| Stripe | Payments + marketplace | 2.9% + $0.30/txn |
| AWS Cognito | Auth | Free up to 50K MAU |

---

## 11. Repository Structure

```
farmmind/
├── apps/
│   └── mobile/                  # React Native (Expo) — iOS, Android, Web
│       ├── src/
│       │   ├── screens/
│       │   ├── components/
│       │   ├── hooks/
│       │   ├── store/           # Zustand state
│       │   ├── services/        # API calls
│       │   ├── locales/         # i18n translation files
│       │   └── utils/
│       ├── app.json
│       └── package.json
│
├── services/
│   ├── api-gateway/             # Spring Boot — main API
│   │   ├── src/main/java/com/farmmind/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── model/
│   │   │   ├── dto/
│   │   │   ├── config/
│   │   │   └── exception/
│   │   └── src/test/
│   │
│   ├── ai-service/              # Spring Boot — AI orchestration
│   │   ├── src/main/java/com/farmmind/ai/
│   │   │   ├── advisor/         # Claude API integration
│   │   │   ├── vision/          # Photo diagnosis
│   │   │   ├── voice/           # Whisper STT
│   │   │   ├── rag/             # Vector search + retrieval
│   │   │   └── weather/         # Tomorrow.io integration
│   │   └── src/test/
│   │
│   └── marketplace-service/     # Phase 2 — Spring Boot
│
├── infrastructure/
│   └── cdk/                     # AWS CDK (TypeScript)
│       ├── lib/
│       │   ├── networking-stack.ts
│       │   ├── database-stack.ts
│       │   ├── ecs-stack.ts
│       │   └── cognito-stack.ts
│       └── bin/
│
├── knowledge-base/
│   ├── ingestion/               # Scripts to scrape + chunk OMAFRA/USDA data
│   └── data/                    # Raw + processed knowledge base files
│
├── docs/
│   ├── CLAUDE_CONTEXT.md        # This file
│   ├── ARCHITECTURE.md
│   ├── FEATURES.md
│   └── TESTING.md
│
├── .github/
│   └── workflows/
│       ├── ci.yml               # Run tests on PR
│       └── deploy.yml           # Deploy to AWS on main merge
│
└── docker-compose.yml           # Local dev environment
```

---

## 12. Development Environment Setup

```bash
# Prerequisites
# - Java 21 (SDKMAN recommended: sdk install java 21-amzn)
# - Node.js 20+ (nvm recommended)
# - Docker Desktop
# - AWS CLI configured
# - Expo CLI: npm install -g expo-cli

# Clone and setup
git clone https://github.com/heeratsingh/farmmind
cd farmmind

# Start local infra (Postgres + Redis)
docker-compose up -d

# Backend
cd services/api-gateway
./mvnw spring-boot:run

# AI service
cd services/ai-service
./mvnw spring-boot:run

# Mobile/Web
cd apps/mobile
npm install
npx expo start --web   # Web browser
npx expo start         # Mobile (scan QR with Expo Go app)
```

---

## 13. Environment Variables Required

```env
# AI
ANTHROPIC_API_KEY=
OPENAI_API_KEY=          # For Whisper STT

# Weather
TOMORROW_IO_API_KEY=

# AWS
AWS_REGION=ca-central-1
AWS_COGNITO_USER_POOL_ID=
AWS_COGNITO_CLIENT_ID=
AWS_S3_BUCKET_NAME=farmmind-uploads
AWS_RDS_URL=
AWS_RDS_USERNAME=
AWS_RDS_PASSWORD=
AWS_ELASTICACHE_URL=

# Stripe
STRIPE_SECRET_KEY=
STRIPE_WEBHOOK_SECRET=

# Google Maps
GOOGLE_MAPS_API_KEY=
```

---

## 14. Open Questions for Next Claude Code Session

These are unresolved decisions — answer them before implementing the relevant module:

1. **Offline mode:** Should the mobile app work offline in fields with no signal? (Affects local storage strategy significantly)
2. **Push notifications:** What triggers a notification? (Frost alerts, new marketplace orders, AI response ready)
3. **Farm profile:** How complex is a farmer's profile? (Single crop or multi-crop? Acreage? Irrigation type?)
4. **Knowledge base updates:** How often does the OMAFRA/USDA data get re-ingested? (Automated pipeline or manual?)
5. **Marketplace trust:** How do buyers know a farm is legitimate? (Verification flow needed in Phase 2)
6. **Data privacy:** Farmers are sensitive about sharing field location and yield data — what's the privacy policy approach?
7. **App Store:** Apple charges $99/yr developer fee. Google charges $25 one-time. Budget for this before launch.
8. **Beta testers:** Do you have 10 farmers willing to test for free before launch? (Critical for product-market fit)

---

*File version: 1.0 | April 2026 | Maintained by Heerat Singh*
