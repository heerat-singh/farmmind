# FarmMind — Implementation, Marketing & Sales Plan
> The complete roadmap from zero to first $10K MRR

---

## Part 1: Implementation Plan (Build)

### Overview: 4 Phases

| Phase | Timeframe | Goal | Revenue Target |
|---|---|---|---|
| 0 — Foundation | Weeks 1–2 | Dev environment + infra up, no features | $0 |
| 1 — MVP | Weeks 3–10 | AI advisor live, users can sign up and ask questions | First 10 paying users |
| 2 — Growth | Months 3–6 | Polish, voice, photo, subscriptions fully working | $2K MRR |
| 3 — Marketplace | Months 6–12 | FarmToTable add-on live | $10K MRR |

---

### Phase 0 — Foundation (Weeks 1–2)

**Goal:** Everything set up so you can write features immediately.

#### Week 1: Infrastructure
- [ ] Create AWS account + enable billing alerts at $50, $100, $200
- [ ] Set up AWS CDK project (`farmmind/infrastructure/cdk/`)
- [ ] Deploy: VPC, RDS PostgreSQL (ca-central-1), ElastiCache Redis
- [ ] Set up AWS Cognito User Pool (email + password auth)
- [ ] Create S3 bucket with proper IAM roles
- [ ] Set up GitHub repo + branch protection (require PR + tests to merge to main)
- [ ] Set up GitHub Actions: run tests on every PR

#### Week 2: Skeleton Apps
- [ ] Spring Boot project: api-gateway service (health endpoint only)
- [ ] Spring Boot project: ai-service (health endpoint only)
- [ ] React Native Expo project: splash screen + blank screen
- [ ] Docker Compose for local dev (Postgres + Redis)
- [ ] Verify: mobile app can hit local backend and get 200 OK
- [ ] Set up Sentry (error tracking) in both backend services
- [ ] Register domain: farmmind.ca (or .com if available)
- [ ] Install Caveman plugin: `claude plugin marketplace add JuliusBrussee/caveman && claude plugin install caveman@caveman`
- [ ] Add caveman always-on rule to `CLAUDE.md` in repo root (see CLAUDE_CONTEXT.md §0)

**End of Phase 0 milestone:** `curl https://api.farmmind.ca/health` returns `{"status":"ok"}`

---

### Phase 1 — MVP (Weeks 3–10)

Build in this order. Each item is a PR. Do not skip steps.

#### Week 3: Auth + Farm Profile
- [ ] F-001: User registration endpoint (POST /auth/register)
- [ ] F-002: User login endpoint (POST /auth/login)
- [ ] F-010: Create farm profile endpoint
- [ ] F-011: Add crops to farm
- [ ] Mobile: Registration screen
- [ ] Mobile: Login screen
- [ ] Mobile: F-003 Onboarding flow (4 steps)

#### Week 4: Weather + Basic Chat UI
- [ ] Tomorrow.io integration in ai-service (cached in Redis)
- [ ] F-012: Weather widget endpoint (GET /weather?lat=&lng=)
- [ ] Mobile: Farm home screen with weather widget
- [ ] Mobile: Chat screen UI (messages list + input bar) — no AI yet, just UI
- [ ] Database: conversations + messages tables

#### Week 5–6: AI Core
- [ ] Knowledge base ingestion script (Python): scrape OMAFRA, chunk, embed, store in pgvector
- [ ] RAG retrieval: vector search endpoint
- [ ] Prompt builder: builds full system prompt from farm + weather + RAG context
- [ ] Claude API client (Java wrapper around Anthropic SDK)
- [ ] SQS: message queue between api-gateway and ai-service
- [ ] SSE: server-sent events to push AI response back to mobile
- [ ] End-to-end: farmer types question → AI responds ✅

#### Week 7: Photo Diagnosis
- [ ] F-021: S3 presigned URL endpoint (GET /upload/presign)
- [ ] Mobile: photo picker + camera capture + compression
- [ ] Mobile: upload directly to S3 via presigned URL
- [ ] ai-service: receive s3Key → fetch image → send to Claude Vision
- [ ] Mobile: display image in conversation history

#### Week 8: Voice Input
- [ ] Mobile: hold-to-record button (Expo Audio API)
- [ ] Upload audio to S3 (same presigned URL pattern)
- [ ] ai-service: Whisper API transcription
- [ ] Mobile: show transcription to user before sending
- [ ] Support: English, Punjabi, Hindi (Whisper handles automatically)

#### Week 9: Subscriptions
- [ ] Stripe account setup (test mode)
- [ ] F-030: Pricing screen in mobile app
- [ ] F-031: Stripe Checkout integration
- [ ] Stripe webhook handler: update subscription tier in DB
- [ ] F-024: Rate limit enforcement (10 questions/mo for free tier)
- [ ] Mobile: paywall screen when limit hit

#### Week 10: Polish + Launch Prep
- [ ] F-023: Conversation history screen
- [ ] F-025: Follow-up question suggestions
- [ ] F-051: Language switch (en/pa/hi) working end-to-end
- [ ] F-053: Offline banner
- [ ] F-027: AI feedback (👍 👎)
- [ ] App icon, splash screen, app name
- [ ] Submit to App Store + Google Play (takes 3–7 days for approval)
- [ ] Set up farmmind.ca website (landing page — see Marketing section)

**End of Phase 1 milestone:** App is live on App Store and Play Store. 10 real users have asked at least one question.

---

### Phase 2 — Growth (Months 3–6)

- [ ] F-026: Push notifications (frost alerts, rain alerts)
- [ ] F-004: Google Sign-In
- [ ] F-028: Export conversation as PDF
- [ ] F-033: Annual billing option
- [ ] F-052: Dark mode
- [ ] F-054: What's new screen
- [ ] F-060: Admin usage dashboard (internal web app)
- [ ] French language support (federal grant requirement)
- [ ] Performance: load test, optimize slow endpoints
- [ ] Add unit + integration tests to reach 80% coverage
- [ ] SEO: optimize farmmind.ca to rank for "AI farm advisor Ontario"

---

### Phase 3 — Marketplace (Months 6–12)

- [ ] F-040: Farmer listing creation
- [ ] F-041: Stripe Connect farmer onboarding
- [ ] F-042: Marketplace browse screen (map + list)
- [ ] F-043: Order placement + payment
- [ ] F-044: Order management (farmer + buyer dashboards)
- [ ] F-045: Reviews + ratings
- [ ] F-046: AI demand prediction for farmers
- [ ] Buyer onboarding flow (restaurants, families)
- [ ] marketplace-service Spring Boot project (new service)

---

## Part 2: Marketing Plan

### The Core Insight
You cannot out-market John Deere or Climate Corp. You win by going to places they never go — local farmers markets, Punjabi farming community Facebook groups, agricultural college events in Ontario. Your unfair advantage is: you're in Brampton, you're South Asian, and you care. Use that.

---

### Pre-Launch (While Building — Months 1–2)

#### Build in public on LinkedIn
Post weekly updates about building FarmMind. Not technical posts — story posts:
- "I moved from India to Brampton 1 year ago. Now I'm building an AI advisor for Ontario farmers. Here's why." (founding story — 1,000+ likes potential)
- "I walked into a farmers market in Mississauga and asked 10 farmers what their biggest problem is. Here's what they said."
- "Week 3 of building FarmMind: here's what the AI said when I asked it about tomato blight."

**Why this works:** Farmers aren't on LinkedIn, but agronomists, agricultural journalists, Ontario OMAFRA staff, and food industry people are. This builds credibility and gets press.

#### Waitlist landing page (farmmind.ca)
Build a single page with:
- One sentence value prop: "Ask any farming question. Get expert AI advice in seconds. In English, Punjabi, or Hindi."
- Email signup for early access
- 60-second demo video (screen recording of the app working)
- Target: 200 waitlist signups before launch

#### OMAFRA connection
Email your local OMAFRA extension office. Introduce yourself. Say you're building a tool to help farmers access their crop guides more easily. Ask if they'd be willing to try a beta. OMAFRA staff talk to hundreds of farmers — one warm referral from them is worth 100 cold ads.

---

### Launch (Month 3)

#### App Store Optimization (ASO)
- App title: "FarmMind — AI Farm Advisor"
- Subtitle: "Crop questions answered instantly"
- Keywords: farm advisor, crop disease, Ontario farmer, agriculture AI, Punjabi farming
- Screenshots: 5 screens showing real conversations in real farm settings
- Localize: English, Punjabi (ਪੰਜਾਬੀ), Hindi

#### Product Hunt Launch
- Schedule for a Tuesday at 12:01am PST
- Prepare your hunter (ask someone with PH followers to hunt it for you)
- Your story: "Immigrant software engineer builds AI farming advisor for Ontario's South Asian farming community"
- This is about awareness, not farmer signups — journalists and investors watch PH

#### Community Launch (highest ROI)
Post in these specific communities with a personal, non-spammy message:

| Community | Platform | Members | Message angle |
|---|---|---|---|
| Ontario Farmers | Facebook | 15K+ | "Built this for you — free beta, want to try?" |
| Punjabi Farmers of Canada | Facebook | 8K+ | Post in Punjabi — "ਤੁਹਾਡੇ ਲਈ ਬਣਾਇਆ" |
| r/farming | Reddit | 200K | Show the plant diagnosis feature with a real example |
| r/Ontario | Reddit | 400K | "I built an AI farming tool for Ontario — here's a demo" |
| Ontario Greenhouse Growers | LinkedIn Group | 5K | Focus on pest diagnosis feature |

**Script for community posts (adapt per community):**
> "Hey everyone — I'm Heerat, a software engineer in Brampton. I've been talking to Ontario farmers for the past few months and built something I think you'll find useful: FarmMind. You take a photo of a sick plant, or just type/speak your question in English, Punjabi, or Hindi, and it gives you expert advice instantly — like having an agronomist in your pocket. It's free to try. Would love 10 people to test it and tell me what's wrong with it. Link in comments."

---

### Growth (Months 3–6)

#### Content SEO (farmmind.ca/blog)
Publish 2 posts/month targeting what Ontario farmers actually Google:

| Blog Post Title | Target Keyword | Monthly Searches |
|---|---|---|
| "Tomato leaf curl: causes and fixes for Ontario farmers" | "tomato leaf curl Ontario" | Medium |
| "Corn blight identification guide with photos" | "corn blight Ontario" | Medium |
| "When to plant tomatoes in Ontario by region" | "when to plant tomatoes Ontario" | High |
| "Ontario frost dates by city — complete guide" | "Ontario frost dates" | High |
| "Best crops for Ontario's climate zone 6" | "Ontario climate zone 6 crops" | Medium |
| "How to read a soil test report in Canada" | "how to read soil test Canada" | Medium |

Each post ends with: "Get instant answers to questions like this with FarmMind — try it free."

#### Partnership with agricultural colleges
- Guelph University: largest agricultural college in Canada — contact their extension program
- Ridgetown Campus (University of Guelph): specialty crops — perfect fit
- Niagara College Horticultural: fruit and vegetable focus
- Offer FarmMind free to students for academic year in exchange for feedback and promotion

#### Local media
Ontario farm publications (high trust with farmers, low competition):
- **Ontario Farmer** magazine — pitch "Brampton tech founder builds AI advisor for local farms"
- **Country Guide** magazine — same pitch
- **The Western Producer** — covers all of Canadian ag tech
- **Brampton Guardian** — local human interest story

#### YouTube / TikTok (for Phase 2 growth)
Short videos: "Watch FarmMind diagnose this plant disease in real time"
- Film yourself walking through a farm (local Brampton/Mississauga farms)
- Show the app working live
- These rank on YouTube and TikTok for agricultural search terms

---

### Retention

#### Weekly email digest (for paid subscribers)
Every Monday: "Your farm this week"
- Weather forecast for their farm GPS
- In-season crop tips for their listed crops
- One question from the community ("This week a farmer asked: why do my pepper plants drop flowers? Here's what FarmMind said...")
- Built using AWS SES (cheap) + React Email templates

#### Push notifications (in-app)
- Frost alerts personalized to their GPS (highest open rate in agriculture apps)
- "Your tomatoes are approaching harvest window — tips for a great harvest"
- Seasonal reminders by crop and climate zone

---

## Part 3: Sales Plan

### Who You're Selling To

**Individual farmers (B2C):**
- Price: $0–$49/month
- Decision time: hours to days
- Buying trigger: problem with crops RIGHT NOW
- How they buy: see it work, sign up immediately
- Channel: app store, community posts, word of mouth

**Agricultural organizations (B2B — Phase 2):**
- Price: $500–$2,000/month (white-label or bulk seats)
- Decision time: 3–6 months (slow)
- Buying trigger: member service value, grant requirements
- How they buy: proposal + demo + trial
- Channel: direct outreach, conference presence

### B2C Sales Process

**The product IS the sales pitch.** If a farmer sees the photo diagnosis work on their actual sick plant, they convert. Your job is to get them to try it.

**Free tier strategy:**
- 10 questions/month is enough to feel the value
- Photo diagnosis is behind paywall (strategically — it's the most impressive feature)
- First photo diagnosis is FREE (bait): "Your first plant diagnosis is on us. ↗ Try it now."
- After that: upgrade prompt with clear value message

**Conversion triggers:**
1. First time hitting free limit → paywall → "Upgrade for $19 to continue"
2. First frost alert notification → "Upgrade to get unlimited alerts + expert advice all season"
3. End of free trial period → "Your 30-day free trial ends in 3 days"

**Annual plan push:**
- Show annual = 2 months free prominently
- Send email 2 weeks before monthly renewal: "Save $46 — switch to annual"

### B2B Outreach (Start Month 6)

**Target organizations:**
1. Ontario Federation of Agriculture (OFA) — 37,000 farmer members
2. Grape Growers of Ontario
3. Ontario Greenhouse Vegetable Growers
4. Canadian Horticultural Council
5. FCC (Farm Credit Canada) — financial tools for farmers

**Outreach email template:**
> Subject: AI farming advisor your members can use in their field — free pilot
>
> Hi [Name],
>
> I'm Heerat Singh, founder of FarmMind — an AI-powered crop advisor app used by Ontario farmers. Farmers ask questions in English, Punjabi, or Hindi and get expert advice instantly, including photo diagnosis of plant diseases.
>
> I'm reaching out because [Organization] serves thousands of Ontario farmers, many of whom can't afford a dedicated agronomist. I'd love to offer your members free access for 3 months as a pilot — no cost to the organization.
>
> After the pilot, we can discuss a white-label partnership where your members get FarmMind under your brand.
>
> Would you have 20 minutes for a demo call this week?
>
> Heerat Singh
> Founder, FarmMind | heeratsingh.com
> Brampton, ON

### Revenue Milestones & What Drives Them

| Milestone | Month | Driver |
|---|---|---|
| First paying user | Month 3 | Personal outreach, farmers market visit |
| 50 paid users — $1K MRR | Month 4 | Community posts, app store organic |
| 100 paid users — $2K MRR | Month 5 | Blog SEO starting to rank, word of mouth |
| 250 paid users — $5K MRR | Month 7 | Partnership with ag college, 1 B2B deal |
| 500 paid users — $10K MRR | Month 12 | Marketplace live, media coverage |

---

## Part 4: Canadian Grant Opportunities

**You should apply for these. Real money, non-dilutive.**

| Program | Amount | Eligibility | Apply By |
|---|---|---|---|
| AAFC AgriInnovate | Up to $10M (loan) | Ag tech products | Rolling |
| NRC IRAP | $50K–$500K | Tech SME in Canada | Rolling |
| Ontario Centre of Innovation | Up to $75K (matching) | Ontario tech startup | Rolling |
| SDTC (Sustainable Dev Tech Canada) | Up to $2M | Clean/ag tech | Quarterly |
| SR&ED Tax Credit | 15–35% of R&D spend | All Canadian tech | Annual |

**SR&ED is the most important.** As a Canadian corporation, you can claim 15–35% of your development costs (including your own salary if you pay yourself) back as a tax credit. File with your accountant every year. This can be worth $15,000–$40,000/year in your early stages.

**To maximize grants:** Adding French language support (already planned) makes you eligible for federal bilingualism-related programs. Building in the agricultural sustainability angle makes you eligible for climate-smart agriculture funding.

---

## Appendix: Weekly Habit During Build Phase

**Every Monday:**
- Review what you shipped last week
- Pick top 3 tasks for this week
- Check AWS cost dashboard — should be under $50/month during dev

**Every Saturday:**
- Visit a farmers market (Brampton, Mississauga, Guelph, St. Jacob's)
- Talk to 2–3 farmers — show them the app, ask what's missing
- Write down their exact words (not your interpretation)

**Every month:**
- Review usage metrics (questions asked, new signups, churn)
- Run AI eval suite — did the AI get worse?
- Check Stripe revenue
- Publish 1 SEO blog post on farmmind.ca

---

*Plan version: 1.0 | April 2026 | Founder: Heerat Singh*
