# 💳 Payment Gateway System

> A robust, full-stack payment processing simulator inspired by Razorpay, featuring Merchant Onboarding, Order Management, and a Hosted Checkout experience.

---

## 🚀 Overview

This project is a comprehensive simulation of a modern payment gateway. It provides a complete ecosystem for merchants to manage orders and for users to complete payments securely. Built with a microservices-oriented architecture in mind, it leverages **Spring Boot** for high-performance backend processing and **React** for a responsive user interface.

### Key Features
- **Merchant Dashboard**: Real-time insights into transactions and API credentials.
- **Hosted Checkout**: A secure, customizable payment page for end-users.
- **Simulated Processing**: Realistic payment lifecycle with random success/failure scenarios (5-10s latency).
- **Robust Validation**: Implements Luhn algorithm for cards and VPA validation for UPI.

---

## 🛠️ Tech Stack

| Component | Technology | Description |
| :--- | :--- | :--- |
| **Backend** | ![Java](https://img.shields.io/badge/Java-ED8B00?style=flat-square&logo=openjdk&logoColor=white) **Spring Boot** | REST API, Order Logic, Payment Processing |
| **Frontend** | ![React](https://img.shields.io/badge/React-20232A?style=flat-square&logo=react&logoColor=61DAFB) **Vite** | Admin Dashboard & Checkout SPA |
| **Database** | ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=flat-square&logo=postgresql&logoColor=white) | Relational Data Store |
| **DevOps** | ![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white) | Containerization & Orchestration |

---

## 📂 Project Structure

```bash
payment-gateway-system/
├── backend/           # Spring Boot Application
├── frontend/          # React Admin Dashboard
├── checkout-page/     # Standalone Payment Page
├── docker-compose.yml # Orchestration Config
└── README.md          # Project Documentation
```

---

## 🏗️ Architecture

```mermaid
graph TD
    Client["Client App/Postman"] -->|API Calls| API["Backend API (8000)"]
    API -->|Auth & Data| DB[("PostgreSQL")]
    Browser["User Browser"] -->|View Dashboard| Dashboard["React Dashboard (3000)"]
    Browser -->|Process Payment| Checkout["Checkout Page (3001)"]
    Checkout -->|Submit Payment| API
    Dashboard -->|Fetch Stats| API
```

---

## 🏁 Getting Started

### Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop) installed and running.

### Installation & Run
1. **Clone the repository**:
   ```bash
   git clone https://github.com/ganesh714/payment-gateway-system.git
   cd payment-gateway-system
   ```

2. **Start the application**:
   ```bash
   docker-compose up -d --build
   ```

3. **Wait for initialization**:
   Ensure all containers are healthy. The backend may take a few moments to start.

---

## 🧪 Usage & Manual Testing

Follow these steps to experience the complete payment lifecycle.

### 1. Create a Test Order
Generate a new order ID using the API.

**Option A: Windows (PowerShell)**
```powershell
Invoke-RestMethod -Uri "http://localhost:8000/api/v1/orders" -Method Post -Headers @{"X-Api-Key"="key_test_abc123"; "X-Api-Secret"="secret_test_xyz789"; "Content-Type"="application/json"} -Body '{"amount": 75000, "currency": "INR", "receipt": "final_verification"}'
```

**Option B: Mac/Linux (curl)**
```bash
curl -X POST http://localhost:8000/api/v1/orders \
  -H "X-Api-Key: key_test_abc123" \
  -H "X-Api-Secret: secret_test_xyz789" \
  -H "Content-Type: application/json" \
  -d '{"amount": 75000, "currency": "INR", "receipt": "final_verification"}'
```

> **Note**: Copy the `id` returned from the response (e.g., `order_yVWbPfcNrb5Rpkxt`).

### 2. Process Payment
Open the hosted checkout page with your Order ID:

`http://localhost:3001/?order_id=YOUR_ORDER_ID`

1.  **Select Method**: Choose Credit Card or UPI.
2.  **Pay**: Click "Pay Now".
3.  **Wait**: Observe the simulated processing delay and final status.

### 3. Verify in Dashboard
Confirm the transaction was recorded.

1.  Access **[Merchant Dashboard](http://localhost:3000)**.
2.  **Login**:
    - Email: `test@example.com`
    - Password: *(Any secure password)*
3.  Navigate to **Transactions** to view the real-time status.

---

## 📸 Screenshots

**1. Merchant Dashboard**
> *Login, Admin Overview, and Transaction History.*
<p float="left">
  <img src="docs/screenshots/merchant_login.png" width="30%" />
  <img src="docs/screenshots/dashboard_overview.png" width="30%" /> 
  <img src="docs/screenshots/dashboard_transactions.png" width="30%" />
</p>

**2. Payment Experience**
> *Hosted checkout forms (UPI & Card), real-time processing, and success confirmation.*
<p float="left">
  <img src="docs/screenshots/UPI_form.png" width="22%" />
  <img src="docs/screenshots/Card_form.png" width="22%" />
  <img src="docs/screenshots/payment_processing.png" width="22%" />
  <img src="docs/screenshots/payment_successful.png" width="22%" /> 
</p>

---

## 🛡️ Automated Testing

The project now includes a suite of automated tests for the backend. You can run these using Docker without needing to install Maven locally.

**Run Backend Tests:**
```bash
docker run --rm -v "${PWD}/backend:/usr/src/app" -w /usr/src/app maven:3.9-eclipse-temurin-17 mvn test
```
*Note: On Windows PowerShell, use `${PWD}`. On Command Prompt, use `%cd%`.*

---

## 🔌 API Reference

| Method | Endpoint | Description | Auth Required |
| :--- | :--- | :--- | :---: |
| `GET` | `/health` | Check system availability | ❌ |
| `POST` | `/api/v1/orders` | Create a new payment order | ✅ |
| `POST` | `/api/v1/payments` | Process a payment | ✅ |
| `GET` | `/api/v1/payments/{id}` | Retrieve payment details | ✅ |
