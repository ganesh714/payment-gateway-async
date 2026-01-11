import React from 'react';

export default function ApiDocs() {
    return (
        <div className="p-6" data-test-id="api-docs">
            <h2 className="text-2xl font-bold mb-6">Integration Guide</h2>

            <div className="space-y-8">
                <section data-test-id="section-create-order" className="bg-white p-6 rounded-lg shadow">
                    <h3 className="text-xl font-semibold mb-4">1. Create Order</h3>
                    <p className="mb-4 text-gray-600">Create an order from your backend to initiate the payment flow.</p>
                    <pre className="bg-gray-900 text-white p-4 rounded overflow-x-auto" data-test-id="code-snippet-create-order">
                        <code>{`curl -X POST http://localhost:8000/api/v1/orders \\
  -H "X-Api-Key: YOUR_API_KEY" \\
  -H "X-Api-Secret: YOUR_API_SECRET" \\
  -H "Content-Type: application/json" \\
  -d '{
    "amount": 50000,
    "currency": "INR",
    "receipt": "receipt_123"
  }'`}</code>
                    </pre>
                </section>

                <section data-test-id="section-sdk-integration" className="bg-white p-6 rounded-lg shadow">
                    <h3 className="text-xl font-semibold mb-4">2. SDK Integration</h3>
                    <p className="mb-4 text-gray-600">Embed the payment gateway using our JavaScript SDK.</p>
                    <pre className="bg-gray-900 text-white p-4 rounded overflow-x-auto" data-test-id="code-snippet-sdk">
                        <code>{`<script src="http://localhost:3001/checkout.js"></script>
<script>
const checkout = new PaymentGateway({
  key: 'YOUR_API_KEY',
  orderId: 'ORDER_ID',
  onSuccess: (response) => {
    console.log('Payment ID:', response.paymentId);
  },
  onFailure: (error) => {
    console.error('Failed:', error);
  }
});
checkout.open();
</script>`}</code>
                    </pre>
                </section>

                <section data-test-id="section-webhook-verification" className="bg-white p-6 rounded-lg shadow">
                    <h3 className="text-xl font-semibold mb-4">3. Verify Webhook Signature</h3>
                    <p className="mb-4 text-gray-600">Verify webhook authenticity using HMAC-SHA256.</p>
                    <pre className="bg-gray-900 text-white p-4 rounded overflow-x-auto" data-test-id="code-snippet-webhook">
                        <code>{`const crypto = require('crypto');

function verifyWebhook(payload, signature, secret) {
  const expectedSignature = crypto
    .createHmac('sha256', secret)
    .update(JSON.stringify(payload))
    .digest('hex');
  
  return signature === expectedSignature;
}`}</code>
                    </pre>
                </section>
            </div>
        </div>
    );
}
