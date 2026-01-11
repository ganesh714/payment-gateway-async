import './styles.css';

class PaymentGateway {
    constructor(options) {
        this.options = options || {};
        this.modal = null;
        this.iframe = null;

        if (!this.options.key) {
            console.error('PaymentGateway: API Key is required');
        }

        // Bind methods
        this.handleMessage = this.handleMessage.bind(this);
        this.close = this.close.bind(this);
    }

    open() {
        if (this.modal) return; // Already open

        // Create Modal Overlay
        this.modal = document.createElement('div');
        this.modal.id = 'payment-gateway-modal';
        this.modal.setAttribute('data-test-id', 'payment-modal');
        this.modal.className = 'modal-overlay';

        // Create Modal Content
        const content = document.createElement('div');
        content.className = 'modal-content';

        // Close Button
        const closeBtn = document.createElement('button');
        closeBtn.className = 'close-button';
        closeBtn.innerHTML = '×';
        closeBtn.setAttribute('data-test-id', 'close-modal-button');
        closeBtn.onclick = this.close;
        content.appendChild(closeBtn);

        // Iframe
        this.iframe = document.createElement('iframe');
        this.iframe.setAttribute('data-test-id', 'payment-iframe');

        // URL Construction
        // Default to localhost:3001 if no base URL provided (simulated environment)
        // The checkout app is served from the same origin on port 3001 in dev.
        // We assume the checkout page (React app) is at root / of port 3001
        // but the SDK is also served from there? Yes.
        // We need to point to the ROUTE of the checkout page.
        // Let's assume the checkout page is at /checkout or just /?
        // Looking at requirement snippet: src="http://localhost:3001/checkout?order_id=xxx&embedded=true"

        const baseUrl = this.options.baseUrl || 'http://localhost:3001';
        const checkoutUrl = new URL('/checkout', baseUrl); // Assuming /checkout route
        if (this.options.orderId) checkoutUrl.searchParams.append('order_id', this.options.orderId);
        checkoutUrl.searchParams.append('embedded', 'true');
        checkoutUrl.searchParams.append('key', this.options.key); // Pass key to checkout page

        this.iframe.src = checkoutUrl.toString();
        content.appendChild(this.iframe);

        this.modal.appendChild(content);
        document.body.appendChild(this.modal);

        // Listener
        window.addEventListener('message', this.handleMessage);
    }

    close() {
        if (this.modal) {
            document.body.removeChild(this.modal);
            this.modal = null;
            this.iframe = null;
            window.removeEventListener('message', this.handleMessage);

            if (this.options.onClose) {
                this.options.onClose();
            }
        }
    }

    handleMessage(event) {
        // Security check: validate origin if possible. 
        // For this project, we accept * (as per Common Mistakes: "While '*' origin is acceptable...")

        const { type, data } = event.data;

        if (type === 'payment_success') {
            if (this.options.onSuccess) {
                this.options.onSuccess(data);
            }
            this.close();
        } else if (type === 'payment_failed') {
            if (this.options.onFailure) {
                this.options.onFailure(data);
            }
            // Do we close on failure? Usually no, let user retry in iframe.
            // But if fatal, maybe. For now logic says just callback.
        } else if (type === 'close_modal') {
            this.close();
        }
    }
}

// Export for module usage
export default PaymentGateway;

// Expose globally for script tag usage
if (typeof window !== 'undefined') {
    window.PaymentGateway = PaymentGateway;
}
