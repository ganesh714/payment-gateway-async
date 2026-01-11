# Async Payment Gateway

A robust, production-ready payment gateway simulation featuring asynchronous processing, webhook delivery with retries, and a developer-friendly dashboard.

## 🚀 Features

- **Async Payment Processing**: Handles high concurrency using Redis queues and background workers.
- **Webhooks**: Reliable event delivery with exponential backoff retries and HMAC-SHA256 signing.
- **Idempotency**: Prevents duplicate transactions using `Idempotency-Key` headers.
- **Developer Dashboard**: Manage API keys, view transaction analytics, and debug webhooks.
- **Embeddable SDK**: Drop-in JavaScript widget for seamless checkout integration.

---

## 🛠️ Setup & Installation

### Prerequisites
- Docker & Docker Compose
- Node.js (v18+) - *Optional, for local frontend dev*
- Java 17+ - *Optional, for local backend dev*

### Quick Start
1.  **Clone the repository**
    ```bash
    git clone <repository-url>
    cd payment-gateway-async
    ```

2.  **Start Services**
    ```bash
    docker-compose up -d --build
    ```
    This launches:
    - **API**: `http://localhost:8000`
    - **Worker**: Background job processor
    - **Dashboard**: `http://localhost:3000`
    - **Checkout SDK**: `http://localhost:3001`
    - **Postgres**: Database
    - **Redis**: Job Queue

3.  **Access Dashboard**
    - Go to [http://localhost:3000](http://localhost:3000).
    - The test credentials will be automatically loaded (or visible in the UI).

---

## 🔧 Environment Configuration

Configuration is managed via `docker-compose.yml` and environment variables.

| Variable | Service | Default | Description |
| :--- | :--- | :--- | :--- |
| `DATABASE_URL` | API/Worker | `jdbc:postgresql://postgres:5432/payment_gateway` | PostgreSQL JDBC URL |
| `SPRING_REDIS_URL` | API/Worker | `redis://redis:6379` | Redis Connection String |
| `TEST_MODE` | Worker | `false` | Enable simulated payment delays |
| `TEST_PAYMENT_SUCCESS` | Worker | `true` | Simulation success rate bool |

---

## 📚 API Documentation

### Authentication
All API requests require authentication headers:
- `X-Api-Key`: Your public key
- `X-Api-Secret`: Your secret key

### Endpoints

#### 1. Create Order
Initialize a transaction session.
- **POST** `/api/v1/orders`
- **Body**:
  ```json
  {
    "amount": 50000,
    "currency": "INR",
    "receipt": "receipt_123"
  }
  ```
- **Response**: Returns `orderId` used for frontend SDK.

#### 2. Initiate Payment
Usually called by the Frontend SDK/Widget.
- **POST** `/api/v1/payments`
- **Headers**: `Idempotency-Key: <unique-uuid>` (Recommended)
- **Body**:
  ```json
  {
    "order_id": "order_xyz...",
    "amount": 50000,
    "method": "upi",
    "vpa": "customer@upi"
  }
  ```

#### 3. Capture Payment
Manually capture an authorized payment (if auto-capture is off).
- **POST** `/api/v1/payments/{paymentId}/capture`
- **Body**: `{"amount": 50000}`

#### 4. Refund Payment
- **POST** `/api/v1/payments/{paymentId}/refunds`

---

## 📡 Webhook Integration

Configure webhooks in the **Dashboard** -> **Webhooks** tab.

### Verification
Verify the `X-Webhook-Signature` header using HMAC-SHA256 with your **Webhook Secret**.

```javascript
// Node.js Example
const crypto = require('crypto');
const signature = req.headers['x-webhook-signature'];
const expected = crypto
  .createHmac('sha256', process.env.WEBHOOK_SECRET)
  .update(JSON.stringify(req.body))
  .digest('hex');

if (signature === expected) {
  // Safe to process
}
```

### Retry Policy
- **Strategy**: Exponential Backoff
- **Intervals**: 30s, 60s, 120s, 300s, 600s (Max 5 retries)
- **Manual Retry**: You can manually trigger a retry from the Dashboard for failed events.

---

## 📦 SDK Integration

Add the checkout widget to your frontend.

### 1. Include Script
```html
<script src="http://localhost:3001/checkout.js"></script>
```

### 2. Initialize
```javascript
const gateway = new PaymentGateway({
  key: 'key_test_abc123', // Your Publishable Key
  orderId: 'order_12345', // from Backend
  onSuccess: (data) => console.log('Success:', data),
  onFailure: (err) => console.error('Error:', err)
});

// Open Modal
gateway.open();
```

---

## 🧪 Testing

### Automated Tests (Submission)
Run the provided `submission.yml` verification commands.
```bash
# Verify Health
curl -f http://localhost:8000/api/v1/test/merchant

# Verify Queue
curl -f http://localhost:8000/api/v1/test/jobs/status
```

### Manual Verification
1.  Open Dashboard.
2.  Go to **API Docs** -> copy the "Create Order" curl command.
3.  Execute it to get an `order_id`.
4.  Use `checkout.js` or `POST /api/v1/payments` to complete payment.
5.  Check **Webhooks** tab in Dashboard to see the `payment.success` event.
