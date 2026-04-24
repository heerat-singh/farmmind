# FarmMind — Features Specification
> For Claude Code: This is the source of truth for what gets built. Never build a feature not listed here without confirming with the founder. Never skip a listed feature without flagging it.

---

## Feature Status Legend
- 🔴 **P0** — Must have for MVP launch. App cannot ship without this.
- 🟡 **P1** — Ship within 30 days of launch. Core value prop.
- 🟢 **P2** — Phase 2 / Marketplace features.
- ⚪ **P3** — Future / nice-to-have. Do not build yet.

---

## Module 1: Authentication & Onboarding

### F-001 — User Registration 🔴
**As a** farmer, **I want to** create an account with my email and password **so that** my conversations and farm data are saved.
- Fields: Full name, email, password, preferred language (en/pa/hi)
- Email verification required before first use
- Password: min 8 chars, 1 uppercase, 1 number
- Error states: email already exists, weak password, network error
- **Backend:** POST /api/v1/auth/register → Cognito signup

### F-002 — User Login 🔴
- Email + password login
- "Remember me" option (extend session to 30 days)
- Forgot password flow (email OTP via Cognito)
- **Backend:** POST /api/v1/auth/login → Cognito auth

### F-003 — Onboarding Flow 🔴
**First-time users see a 4-step onboarding:**
1. Language selection (English / ਪੰਜਾਬੀ / हिंदी)
2. Farm location (map pin or GPS auto-detect)
3. Farm type selection (Vegetable / Grain / Orchard / Greenhouse / Mixed / Other)
4. Primary crops (multi-select from list + custom add)

- Data saved to `farms` table on completion
- Skippable (but prompted again on first AI question)

### F-004 — Google Sign-In 🟡
- Social login via Cognito + Google OAuth
- Auto-fills name, profile photo
- Links to existing account if same email

### F-005 — Profile Management 🟡
- Edit name, phone, language, notification preferences
- View subscription tier + expiry
- Delete account (PIPEDA compliance — full data erasure within 30 days)

---

## Module 2: Farm Profile

### F-010 — Create Farm Profile 🔴
- Farm name, GPS location (map picker), province, acreage, farm type
- Climate zone auto-derived from GPS (hardcoded zone lookup table for Canadian provinces)
- One user can have multiple farms (e.g., different plots)

### F-011 — Add / Manage Crops 🔴
- Add crop: crop name (dropdown + custom), variety, planting date
- Expected harvest date auto-suggested based on crop + planting date + climate zone
- Mark crop as harvested / remove crop
- Edit crop details

### F-012 — Farm Weather Widget 🔴
- Shows on farm profile screen
- Current conditions + 7-day forecast for farm GPS coordinates
- Frost warning badge (highlighted red if frost predicted in 5 days)
- Data from Tomorrow.io API, cached 30 min per location
- Renders offline using last cached data

---

## Module 3: AI Crop Advisor (Core Feature)

### F-020 — Text Question 🔴
**As a** farmer, **I want to** type a farming question **so that** I get expert advice instantly.
- Chat-style interface (like iMessage/WhatsApp)
- User message on right, AI response on left
- Supports multi-turn conversation (context maintained)
- AI response includes source attribution ("Based on OMAFRA guidelines...")
- Copy response button
- Share response button (WhatsApp, SMS)

**Acceptance criteria:**
- Response time < 5 seconds for text questions
- Response is specific to the farmer's farm profile and current weather
- Response is in the user's selected language

### F-021 — Photo Diagnosis 🔴 *(P0 — this is a key differentiator)*
**As a** farmer, **I want to** take a photo of a sick plant **so that** the AI can diagnose what's wrong.
- Camera capture or photo library upload
- Image compressed to max 2MB before upload
- Upload via S3 presigned URL
- AI response identifies: disease/pest name, severity, recommended treatment, prevention
- Photo stored in conversation history
- **Limit:** Free tier: 0 photo diagnoses. Grower: 20/mo. Pro: unlimited.

### F-022 — Voice Input 🔴 *(P0 — field workers can't type while working)*
**As a** farmer working in the field, **I want to** speak my question **so that** I don't have to type with dirty hands.
- Hold-to-record button (like WhatsApp voice notes)
- Audio sent to Whisper API for transcription
- Transcription shown to user before sending (editable)
- Transcribed text then sent to Claude as normal question
- Supports: English, Punjabi, Hindi (Whisper is multilingual)
- **Limit:** Free tier: 5 voice questions/month. Grower+: unlimited.

### F-023 — Conversation History 🔴
- All conversations saved and searchable
- Grouped by date
- Search conversations by keyword
- Delete individual conversation
- Conversations tied to a specific farm (can switch farms)

### F-024 — Question Limit Enforcement 🔴
- Free tier: 10 text questions/month
- Remaining count shown in UI ("7 questions left this month")
- When limit hit: paywall screen with upgrade prompt
- Counter resets on 1st of each month
- Grower/Pro: no limit shown

### F-025 — Smart Suggestions 🟡
- After each AI response, show 2–3 follow-up question suggestions
- Examples: "What pesticide should I use?", "How do I prevent this next season?"
- Tap to auto-send as next question

### F-026 — Seasonal Alerts 🟡
- Push notifications triggered by weather events:
  - Frost alert: "Frost predicted at your farm on [date]. Here's how to protect your [crop]."
  - Heavy rain alert: "Heavy rain in 48 hours — consider [action] for your [crop]."
  - Planting reminder: "Based on your climate zone, it's time to [action]."
- User can configure which alerts they receive
- Delivered via AWS SNS → mobile push

### F-027 — AI Response Feedback 🟡
- 👍 / 👎 on every AI response
- Thumbs down: optional reason (Wrong info / Not helpful / Didn't understand my question)
- Feedback logged to DB for model improvement monitoring

### F-028 — Export Conversation 🟢 (Phase 2)
- Export conversation as PDF
- Useful for sharing with agronomist or keeping records

---

## Module 4: Subscription & Payments

### F-030 — Subscription Tiers Display 🔴
- Pricing screen accessible from: paywall, settings, onboarding
- Shows: Free / Grower ($19/mo) / Pro ($49/mo)
- Feature comparison table per tier
- Monthly and annual toggle (annual = 2 months free)

### F-031 — Stripe Checkout 🔴
- Tap "Upgrade" → Stripe hosted checkout (handles card, Apple Pay, Google Pay)
- On success: Stripe webhook → update user.subscription_tier in DB
- Confirmation screen + email receipt

### F-032 — Subscription Management 🟡
- View current plan, renewal date
- Upgrade / downgrade between Grower and Pro
- Cancel subscription (takes effect at end of billing period)
- Reactivate cancelled subscription

### F-033 — Annual Billing 🟡
- Annual plans: Grower $182/yr (save $46), Pro $470/yr (save $118)
- Toggle on pricing screen

---

## Module 5: Marketplace (Phase 2)

### F-040 — Farmer Listing Creation 🟢
- Add produce listing: crop, variety, quantity, unit, price, description, photos (max 5), pickup/delivery options, availability dates
- Draft → publish workflow
- Edit and deactivate listings

### F-041 — Stripe Connect Onboarding for Farmers 🟢
- Farmers must complete Stripe Connect onboarding to receive payments
- Guided flow: bank account, tax info, identity verification
- Payout schedule: weekly to farmer's bank account

### F-042 — Marketplace Browse (Buyer) 🟢
- Browse produce by: location (map view + list), crop type, farm name
- Filter: radius from buyer's location, crop type, price range, organic/conventional
- Farm profile page: about the farm, ratings, all active listings

### F-043 — Order Placement 🟢
- Add to cart from listing
- Select quantity, pickup date
- Stripe payment (buyer pays platform, platform splits to farmer via Connect)
- Order confirmation + email receipt

### F-044 — Order Management 🟢
- Farmer: view incoming orders, confirm/reject, mark as ready
- Buyer: view order status, order history, reorder
- Push notifications: order confirmed, order ready for pickup

### F-045 — Reviews & Ratings 🟢
- Buyers rate farmers after order completion (1–5 stars + text)
- Rating shown on farm profile
- Farmers cannot delete reviews (trust integrity)

### F-046 — AI Demand Prediction for Farmers 🟢
- After 3+ months of marketplace data: "Based on order trends, list heirloom tomatoes this week — demand is high in your area."
- Shown as a banner suggestion on the farmer's marketplace dashboard

---

## Module 6: Settings & Utility

### F-050 — Notification Settings 🟡
- Toggle: frost alerts, rain alerts, planting reminders, marketplace orders (Phase 2)
- Quiet hours setting

### F-051 — Language Switch 🔴
- Change language from settings at any time
- UI updates immediately, no reload required
- AI responses in new language from next question

### F-052 — Dark Mode 🟡
- Follows device system setting
- Manual override in settings

### F-053 — Offline Banner 🟡
- When no internet: banner shown at top "You're offline. Last weather data from [time]."
- Conversation history still viewable (read-only)
- Questions queued and sent when connection restores

### F-054 — App Version & Changelog 🟡
- Settings → About → version number
- "What's new" screen on first launch after update

---

## Module 7: Admin (Internal — not in mobile app)

### F-060 — Usage Dashboard 🟡
- Web-only admin panel
- Total users, DAU/MAU, questions asked today, subscriptions by tier
- AI cost per user per day (token tracking)
- Error rate from ai-service

### F-061 — Knowledge Base Management 🟡
- View all knowledge chunks
- Add new chunk manually
- Trigger re-ingestion from OMAFRA/USDA sources
- Search chunks by crop or category

### F-062 — User Support Lookup 🟢
- Look up any user by email
- View their conversations, subscription, farm profile
- Manually adjust subscription tier (for support resolution)

---

## Non-Functional Requirements

| Requirement | Target |
|---|---|
| AI response time (text) | < 5 seconds p95 |
| AI response time (photo) | < 12 seconds p95 |
| App cold start time | < 3 seconds |
| API uptime | 99.5% monthly |
| Max image upload size | 10 MB (compressed to 2 MB before upload) |
| Supported iOS versions | iOS 15+ |
| Supported Android versions | Android 10+ |
| Accessibility | WCAG 2.1 AA for web; React Native Accessibility API for mobile |
| Data residency | All data stored in AWS ca-central-1 (Canadian data sovereignty) |

---

*Features version: 1.0 | April 2026*
