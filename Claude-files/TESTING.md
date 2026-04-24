# FarmMind — Testing Strategy
> For Claude Code: Every feature must have tests before it is considered done. "It works on my machine" is not a test.

---

## Testing Philosophy

**The solo founder rule:** You have no QA team. Tests are your QA team. Write them as if the next person to read them has never seen the codebase (because that person might be you in 3 months).

**Coverage targets:**
- Backend services: **80%+ line coverage** (enforced in CI)
- Critical paths (auth, payments, AI): **95%+ coverage**
- Mobile: **key user flows covered** with integration tests
- AI responses: **evaluated, not unit tested** (see Section 5)

---

## 1. Backend Testing — Java / Spring Boot

### 1.1 Unit Tests
**Tool:** JUnit 5 + Mockito + AssertJ
**Location:** `src/test/java/...` mirroring main package structure
**Run:** `mvn test`

**What to unit test:**
- All service layer methods
- DTO validation logic
- Utility/helper classes
- Prompt builder (verify correct placeholders filled)
- Rate limiter logic (free tier enforcement)

**Example — AdvisorService unit test:**
```java
@ExtendWith(MockitoExtension.class)
class AdvisorServiceTest {

    @Mock
    private KnowledgeRepository knowledgeRepo;
    @Mock
    private ClaudeClient claudeClient;
    @Mock
    private WeatherService weatherService;

    @InjectMocks
    private AdvisorService advisorService;

    @Test
    @DisplayName("Should include farm weather context in AI prompt")
    void shouldIncludeWeatherInPrompt() {
        // Given
        FarmProfile farm = FarmProfile.builder()
            .latitude(43.7315)
            .longitude(-79.7624)
            .climatezone("5b")
            .primaryCrop("tomatoes")
            .build();

        WeatherData weather = WeatherData.builder()
            .tempHighC(28)
            .tempLowC(14)
            .conditions("Sunny")
            .frostRisk(false)
            .build();

        when(weatherService.getForecast(anyDouble(), anyDouble())).thenReturn(weather);
        when(knowledgeRepo.findSimilar(any(), any(), any(), anyInt()))
            .thenReturn(List.of());
        when(claudeClient.complete(any(), any(), any()))
            .thenReturn(new ClaudeResponse("Test response", 150));

        // When
        AiResponse response = advisorService.answer(
            AiRequest.builder()
                .farm(farm)
                .question("Why are my tomato leaves curling?")
                .language("en")
                .build()
        );

        // Then
        verify(claudeClient).complete(
            argThat(prompt -> prompt.contains("28") && prompt.contains("Sunny")),
            any(),
            isNull()
        );
        assertThat(response.getText()).isEqualTo("Test response");
        assertThat(response.getTokensUsed()).isEqualTo(150);
    }

    @Test
    @DisplayName("Should throw RateLimitException when free user exceeds monthly limit")
    void shouldEnforceFreeTierLimit() {
        // Given
        User freeUser = User.builder()
            .subscriptionTier("free")
            .questionsThisMonth(10)  // limit reached
            .build();

        // When / Then
        assertThatThrownBy(() -> advisorService.checkRateLimit(freeUser))
            .isInstanceOf(RateLimitException.class)
            .hasMessage("Monthly question limit reached. Upgrade to ask unlimited questions.");
    }
}
```

### 1.2 Integration Tests
**Tool:** Spring Boot Test + Testcontainers (real PostgreSQL, real Redis in Docker)
**Location:** `src/test/java/.../integration/`
**Run:** `mvn verify -P integration`

**What to integration test:**
- Full HTTP request → controller → service → database round-trip
- Database queries (especially pgvector similarity search)
- Stripe webhook processing
- SQS message handling
- Auth token validation via Cognito (mocked with WireMock)

**Example — Conversation API integration test:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class ConversationControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withInitScript("schema.sql");

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ConversationRepository conversationRepo;

    private String authToken;

    @BeforeEach
    void setUp() {
        authToken = getTestUserToken(); // helper creates test user in Cognito mock
    }

    @Test
    @DisplayName("POST /conversations creates a new conversation and returns 201")
    void createConversationReturns201() {
        CreateConversationRequest request = new CreateConversationRequest("my-farm-id");

        ResponseEntity<ConversationDto> response = restTemplate.exchange(
            "/api/v1/conversations",
            HttpMethod.POST,
            new HttpEntity<>(request, authHeaders(authToken)),
            ConversationDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(conversationRepo.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("GET /conversations returns only the authenticated user's conversations")
    void conversationsAreUserScoped() {
        // Create conversations for two users
        createConversationForUser("user-1-token");
        createConversationForUser("user-1-token");
        createConversationForUser("user-2-token");

        // User 1 should only see their own 2
        ResponseEntity<List<ConversationDto>> response = restTemplate.exchange(
            "/api/v1/conversations",
            HttpMethod.GET,
            new HttpEntity<>(authHeaders("user-1-token")),
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getBody()).hasSize(2);
    }
}
```

### 1.3 Contract Tests (API)
**Tool:** Spring REST Docs (generates docs + validates contract)

Every public API endpoint must have a REST Docs test that:
1. Makes the real HTTP call
2. Validates the response structure
3. Generates a documentation snippet

This ensures your API docs are never out of date.

---

## 2. Mobile Testing — React Native / Expo

### 2.1 Unit Tests
**Tool:** Jest + React Native Testing Library
**Location:** `apps/mobile/src/__tests__/`
**Run:** `npm test`

**What to unit test:**
- Zustand store reducers / actions
- Utility functions (date formatting, token counter, language detection)
- Custom hooks (useWeather, useFarmProfile, useConversation)
- Input validation (registration form, farm setup form)

**Example — useConversation hook test:**
```javascript
import { renderHook, act } from '@testing-library/react-hooks';
import { useConversation } from '../hooks/useConversation';

// Mock the API service
jest.mock('../services/api', () => ({
  sendMessage: jest.fn(),
}));

describe('useConversation', () => {
  it('adds user message to local state immediately before API responds', async () => {
    const { result } = renderHook(() => useConversation('conv-123'));

    await act(async () => {
      result.current.sendMessage('Why are my leaves yellow?');
    });

    // User message should appear immediately (optimistic update)
    expect(result.current.messages[0]).toMatchObject({
      role: 'user',
      content: 'Why are my leaves yellow?',
    });
  });

  it('shows loading indicator while waiting for AI response', async () => {
    const { result } = renderHook(() => useConversation('conv-123'));

    act(() => {
      result.current.sendMessage('Test question');
    });

    expect(result.current.isLoading).toBe(true);
  });
});
```

### 2.2 Component Tests
**Tool:** React Native Testing Library
**What to test:** Every screen and reusable component

```javascript
import { render, fireEvent, waitFor } from '@testing-library/react-native';
import { ChatScreen } from '../screens/ChatScreen';

describe('ChatScreen', () => {
  it('renders send button as disabled when input is empty', () => {
    const { getByTestId } = render(<ChatScreen />);
    const sendButton = getByTestId('send-button');
    expect(sendButton).toBeDisabled();
  });

  it('enables send button when user types a message', () => {
    const { getByTestId } = render(<ChatScreen />);
    const input = getByTestId('message-input');
    const sendButton = getByTestId('send-button');

    fireEvent.changeText(input, 'Why are my crops wilting?');
    expect(sendButton).not.toBeDisabled();
  });

  it('clears input after message is sent', async () => {
    const { getByTestId } = render(<ChatScreen />);
    const input = getByTestId('message-input');

    fireEvent.changeText(input, 'Test question');
    fireEvent.press(getByTestId('send-button'));

    await waitFor(() => {
      expect(input.props.value).toBe('');
    });
  });

  it('shows paywall when free user hits question limit', () => {
    // Mock user with 10/10 questions used
    mockUserStore({ questionsRemaining: 0, tier: 'free' });

    const { getByTestId } = render(<ChatScreen />);
    expect(getByTestId('paywall-modal')).toBeVisible();
  });
});
```

### 2.3 End-to-End Tests
**Tool:** Detox (React Native E2E)
**Run on:** iOS Simulator + Android Emulator in CI
**Location:** `apps/mobile/e2e/`

**Critical flows to E2E test (P0):**
```javascript
// e2e/onboarding.test.js
describe('Onboarding Flow', () => {
  it('completes full onboarding and lands on chat screen', async () => {
    await device.launchApp({ newInstance: true });

    // Step 1: Language selection
    await element(by.id('language-english')).tap();
    await element(by.id('continue-button')).tap();

    // Step 2: Farm location
    await element(by.id('use-my-location')).tap();
    await element(by.id('continue-button')).tap();

    // Step 3: Farm type
    await element(by.id('farm-type-vegetable')).tap();
    await element(by.id('continue-button')).tap();

    // Step 4: Crops
    await element(by.id('crop-search-input')).typeText('Tomatoes');
    await element(by.id('crop-tomatoes')).tap();
    await element(by.id('finish-button')).tap();

    // Should land on chat screen
    await expect(element(by.id('chat-screen'))).toBeVisible();
  });
});

// e2e/askQuestion.test.js
describe('Asking an AI Question', () => {
  beforeEach(async () => {
    await loginAs('test-farmer@example.com');
  });

  it('sends a text question and receives a response', async () => {
    await element(by.id('message-input')).typeText('Why are my tomato leaves curling?');
    await element(by.id('send-button')).tap();

    // AI response should appear within 10 seconds
    await waitFor(element(by.id('ai-message-0')))
      .toBeVisible()
      .withTimeout(10000);

    await expect(element(by.id('ai-message-0'))).toBeVisible();
  });
});
```

---

## 3. AI Quality Testing

AI responses cannot be unit tested in the traditional sense. Instead, use an **evaluation framework**.

### 3.1 Evaluation Dataset
**Location:** `tests/ai-evals/questions.json`

Maintain a dataset of 50+ real farming questions with:
- The question
- The farm context (crop, location, weather)
- An "acceptable answer" rubric (not exact text — key facts that must be present)

```json
[
  {
    "id": "eval-001",
    "question": "My tomato leaves are curling upward and turning yellow. What's wrong?",
    "farm_context": {
      "crop": "tomatoes",
      "province": "Ontario",
      "weather": { "temp_high": 32, "recent_rain": false }
    },
    "must_mention": ["heat stress", "water", "irrigation"],
    "must_not_mention": ["fungicide"],
    "language": "en",
    "category": "disease_diagnosis"
  },
  {
    "id": "eval-002",
    "question": "ਮੇਰੇ ਟਮਾਟਰਾਂ ਦੇ ਪੱਤੇ ਪੀਲੇ ਕਿਉਂ ਹੋ ਰਹੇ ਹਨ?",
    "farm_context": { "crop": "tomatoes", "province": "Ontario" },
    "must_mention_lang": "pa",
    "must_mention": ["ਪਾਣੀ", "ਖਾਦ"],
    "language": "pa",
    "category": "disease_diagnosis"
  }
]
```

### 3.2 Eval Runner
**Location:** `tests/ai-evals/run_evals.py`

```python
# Run before any major prompt template change
python tests/ai-evals/run_evals.py --env staging

# Outputs:
# ✅ eval-001: PASS (mentioned heat stress, water, irrigation)
# ✅ eval-002: PASS (response in Punjabi, mentioned ਪਾਣੀ)
# ❌ eval-015: FAIL (recommended fungicide for bacterial issue)
# Score: 48/50 (96%) — PASS threshold: 90%
```

**Run evals:**
- Before every prompt template change
- Before every Claude API version upgrade
- Weekly in CI (scheduled job)

### 3.3 Regression Testing for AI
When a real user reports a bad answer (via 👎 feedback), add it to the eval dataset. This creates a growing regression suite from real usage.

---

## 4. Payment Testing

**Never test payments with real cards. Always use Stripe test mode.**

### Stripe Test Cards
```
Success:              4242 4242 4242 4242
Insufficient funds:   4000 0000 0000 9995
Card declined:        4000 0000 0000 0002
Requires 3DS auth:    4000 0027 6000 3184
```

### Webhook Testing
```bash
# Install Stripe CLI
stripe listen --forward-to localhost:8080/api/v1/webhooks/stripe

# Trigger test events
stripe trigger checkout.session.completed
stripe trigger invoice.payment_failed
stripe trigger customer.subscription.deleted
```

### Payment Integration Test Checklist
- [ ] Checkout session created correctly with correct tier pricing
- [ ] Webhook updates user subscription tier in DB on success
- [ ] Webhook handles duplicate events idempotently (Stripe retries webhooks)
- [ ] User subscription downgrades to free on cancellation
- [ ] Rate limits enforce correctly after subscription change

---

## 5. Performance Testing

**Tool:** k6 (open source load testing)
**Location:** `tests/performance/`
**Run before any major release**

```javascript
// tests/performance/chat-load-test.js
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '2m', target: 50 },   // Ramp up to 50 users
    { duration: '5m', target: 50 },   // Hold at 50
    { duration: '2m', target: 0 },    // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<5000'],  // 95% of requests < 5s
    http_req_failed: ['rate<0.01'],     // < 1% failure rate
  },
};

export default function () {
  const response = http.post(
    'https://staging.farmmind.ca/api/v1/messages',
    JSON.stringify({ question: 'Why are my leaves yellow?', conversationId: 'test-123' }),
    { headers: { Authorization: `Bearer ${__ENV.TEST_TOKEN}`, 'Content-Type': 'application/json' } }
  );

  check(response, {
    'status is 200': (r) => r.status === 200,
    'response has content': (r) => JSON.parse(r.body).content !== undefined,
  });

  sleep(3);
}
```

---

## 6. Security Testing

Run before every production deploy:

```bash
# Dependency vulnerability scan
mvn dependency-check:check  # OWASP for Java
npm audit                   # for mobile

# Static analysis
mvn sonar:sonar  # SonarQube

# Secret scanning (never commit keys)
git secrets --scan
```

**Manual security checklist (quarterly):**
- [ ] All API endpoints require auth (except /auth/register, /auth/login)
- [ ] Every DB query filters by userId (row-level security)
- [ ] S3 bucket is not public
- [ ] API keys rotated in AWS Secrets Manager
- [ ] Prompt injection: try "Ignore all previous instructions" — confirm AI stays in role
- [ ] Rate limiting: confirm free tier can't bypass with multiple accounts from same IP

---

## 7. CI Pipeline — Test Execution Order

```yaml
# Every pull request must pass ALL of these:

1. Lint (ESLint mobile, Checkstyle Java)
2. Unit tests (mvn test + npm test)
3. Integration tests (mvn verify -P integration)
4. Security scan (OWASP + npm audit)
5. Build Docker image
6. E2E tests on staging environment (Detox)
7. AI eval suite (must pass 90%+ threshold)

# Only after all pass → merge allowed → auto-deploy to staging
# Manual approval required → deploy to production
```

---

## 8. Test Data Management

### Seed Data (for local dev and integration tests)
**Location:** `src/test/resources/seed/`

```sql
-- seed/test-users.sql
INSERT INTO users (id, email, full_name, subscription_tier, language)
VALUES
  ('user-free-001', 'free@test.com', 'Test Free User', 'free', 'en'),
  ('user-grower-001', 'grower@test.com', 'Test Grower', 'grower', 'en'),
  ('user-pro-001', 'pro@test.com', 'Test Pro User', 'pro', 'pa');

-- seed/test-farms.sql
INSERT INTO farms (id, owner_id, name, latitude, longitude, province, climate_zone, farm_type)
VALUES
  ('farm-001', 'user-free-001', 'Singh Family Farm', 43.7315, -79.7624, 'Ontario', '6a', 'vegetable');
```

**Rule:** Never use production data in tests. Never commit real email addresses or phone numbers.

---

*Testing version: 1.0 | April 2026*
