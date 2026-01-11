import React from 'react';

import React, { useState } from 'react';

const CodeBlock = ({ code, language = 'bash' }) => {
  const [copied, setCopied] = useState(false);

  const handleCopy = () => {
    navigator.clipboard.writeText(code);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="relative group mt-4 mb-6 rounded-lg overflow-hidden border border-gray-700 shadow-lg">
      <div className="bg-gray-800 px-4 py-2 flex justify-between items-center border-b border-gray-700">
        <span className="text-xs text-gray-400 font-mono uppercase">{language}</span>
        <button
          onClick={handleCopy}
          className="text-xs text-gray-300 hover:text-white transition-colors focus:outline-none"
        >
          {copied ? 'Copied!' : 'Copy Code'}
        </button>
      </div>
      <pre className="bg-gray-900 text-gray-100 p-4 overflow-x-auto text-sm font-mono leading-relaxed" data-test-id={`code-snippet-${language}`}>
        <code>{code}</code>
      </pre>
    </div>
  );
};

export default function ApiDocs() {
  return (
    <div className="p-6 max-w-5xl mx-auto" data-test-id="api-docs">
      <div className="header mb-10">
        <h2 className="title text-3xl">Integration Guide</h2>
        <p className="text-gray-500 mt-2 text-lg">Accept payments and handle events in 3 simple steps.</p>
      </div>

      <div className="space-y-12">
        {/* Step 1: Create Order */}
        <section data-test-id="section-create-order" className="bg-white p-8 rounded-xl border border-gray-200 shadow-sm relative overflow-hidden">
          <div className="absolute top-0 left-0 w-2 h-full bg-indigo-500"></div>
          <h3 className="text-2xl font-bold mb-3 text-gray-900 flex items-center">
            <span className="flex items-center justify-center w-8 h-8 rounded-full bg-indigo-100 text-indigo-600 text-sm font-bold mr-3">1</span>
            Create an Order
          </h3>
          <p className="text-gray-600 mb-6 leading-relaxed">
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

          <div className="bg-indigo-50 p-4 rounded-md border border-indigo-100">
            <h4 className="text-sm font-bold text-indigo-900 mb-1">Note</h4>
            <p className="text-sm text-indigo-800">
              Amounts are in the smallest currency unit (e.g., paise for INR).
              <code>50000</code> = ₹500.00.
            </p>
          </div>
        </section>

        {/* Step 2: SDK Integration */}
        <section data-test-id="section-sdk-integration" className="bg-white p-8 rounded-xl border border-gray-200 shadow-sm relative overflow-hidden">
          <div className="absolute top-0 left-0 w-2 h-full bg-indigo-500"></div>
          <h3 className="text-2xl font-bold mb-3 text-gray-900 flex items-center">
            <span className="flex items-center justify-center w-8 h-8 rounded-full bg-indigo-100 text-indigo-600 text-sm font-bold mr-3">2</span>
            Frontend Integration
          </h3>
          <p className="text-gray-600 mb-6 leading-relaxed">
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
        <section data-test-id="section-webhook-verification" className="bg-white p-8 rounded-xl border border-gray-200 shadow-sm relative overflow-hidden">
          <div className="absolute top-0 left-0 w-2 h-full bg-indigo-500"></div>
          <h3 className="text-2xl font-bold mb-3 text-gray-900 flex items-center">
            <span className="flex items-center justify-center w-8 h-8 rounded-full bg-indigo-100 text-indigo-600 text-sm font-bold mr-3">3</span>
            Webhook Verification
          </h3>
          <p className="text-gray-600 mb-6 leading-relaxed">
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
