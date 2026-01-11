import React, { useState } from 'react';

const CodeBlock = ({ code, language = 'bash' }) => {
  const [copied, setCopied] = useState(false);

  const handleCopy = () => {
    navigator.clipboard.writeText(code);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="code-block-container">
      <div className="code-header">
        <span>{language}</span>
        <button
          onClick={handleCopy}
          className="btn-link text-xs"
          style={{ color: '#9ca3af', textTransform: 'uppercase', textDecoration: 'none' }}
        >
          {copied ? 'Copied!' : 'Copy Code'}
        </button>
      </div>
      <pre className="code-content">
        <code>{code}</code>
      </pre>
    </div>
  );
};

export default function ApiDocs() {
  return (
    <div className="p-6 max-w-5xl mx-auto">
      <div className="header mb-8">
        <h2 className="title">Integration Guide</h2>
        <p className="text-gray-500 mt-2 text-lg">Accept payments and handle events in 3 simple steps.</p>
      </div>

      <div className="space-y-8">
        {/* Step 1: Create Order */}
        <section className="doc-section">
          <h3 className="doc-step">
            <span className="step-number">1</span>
            Create an Order
          </h3>
          <p className="text-gray-500 mb-6 leading-relaxed">
            Call the API from your backend to generate a secure <code>orderId</code>. This ID tracks the transaction lifecycle.
          </p>

          <CodeBlock
            language="bash"
            code={`curl -X POST http://localhost:8000/api/v1/orders \\
  -H "X-Api-Key: YOUR_API_KEY" \\
  -H "X-Api-Secret: YOUR_API_SECRET" \\
  -H "Content-Type: application/json" \\
  -d '{
    "amount": 50000,
    "currency": "INR",
    "receipt": "receipt_#1234"
  }'`}
          />

          <div className="mt-4 p-4 rounded-lg border" style={{ background: '#eef2ff', borderColor: '#c7d2fe' }}>
            <h4 className="text-sm font-bold mb-1" style={{ color: '#312e81' }}>Note</h4>
            <p className="text-sm" style={{ color: '#3730a3' }}>
              Amounts are in the smallest currency unit (e.g., paise for INR).
              <code style={{ background: 'rgba(255,255,255,0.5)', padding: '0 4px', borderRadius: '4px' }}>50000</code> = ₹500.00.
            </p>
          </div>
        </section>

        {/* Step 2: SDK Integration */}
        <section className="doc-section">
          <h3 className="doc-step">
            <span className="step-number">2</span>
            Frontend Integration
          </h3>
          <p className="text-gray-500 mb-6 leading-relaxed">
            Include our JavaScript SDK on your checkout page to render the payment modal securely.
          </p>

          <CodeBlock
            language="html"
            code={`<!-- 1. Include the SDK -->
<script src="http://localhost:3001/checkout.js"></script>

<!-- 2. Initialize and Open -->
<script>
  const checkout = new PaymentGateway({
    key: 'YOUR_API_KEY', // Publishable Key
    orderId: 'ORDER_ID_FROM_BACKEND',
    
    onSuccess: (response) => {
      // paymentId, signature
      console.log('Payment Successful:', response);
      // Verify signature on your backend
    },
    
    onFailure: (error) => {
      console.error('Payment Failed:', error);
    }
  });

  document.getElementById('pay-btn').onclick = () => {
    checkout.open();
  };
</script>`}
          />
        </section>

        {/* Step 3: Webhook Verification */}
        <section className="doc-section">
          <h3 className="doc-step">
            <span className="step-number">3</span>
            Webhook Verification
          </h3>
          <p className="text-gray-500 mb-6 leading-relaxed">
            Secure your webhooks by verifying the HMAC-SHA256 signature sent in the <code>X-Webhook-Signature</code> header.
          </p>

          <CodeBlock
            language="javascript"
            code={`const crypto = require('crypto');

function verifyWebhook(payload, signature, secret) {
  // 1. Generate expected signature
  const expected = crypto
    .createHmac('sha256', secret)
    .update(JSON.stringify(payload))
    .digest('hex');
  
  // 2. Compare (constant-time comparison recommended)
  return signature === expected;
}`}
          />
        </section>
      </div>
    </div>
  );
}
