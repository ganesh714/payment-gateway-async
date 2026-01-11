import React, { useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'

export default function Checkout() {
    const [searchParams] = useSearchParams()
    const orderId = searchParams.get('order_id')

    const [order, setOrder] = useState(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState(null)

    const [method, setMethod] = useState('upi')
    const [paymentState, setPaymentState] = useState('input') // input, processing, success, failed
    const [paymentResult, setPaymentResult] = useState(null)
    const [paymentError, setPaymentError] = useState(null)

    // Form States
    const [vpa, setVpa] = useState('')
    const [cardNumber, setCardNumber] = useState('')
    const [expiry, setExpiry] = useState('')
    const [cvv, setCvv] = useState('')
    const [holderName, setHolderName] = useState('')

    useEffect(() => {
        if (!orderId) {
            setError('Invalid Access: Missing Order ID')
            setLoading(false)
            return
        }

        const fetchOrder = async () => {
            try {
                const res = await fetch(`/api/v1/orders/${orderId}/public`)
                if (!res.ok) throw new Error('Order not found or expired')
                const data = await res.json()
                setOrder(data)
            } catch (err) {
                setError(err.message)
            } finally {
                setLoading(false)
            }
        }
        fetchOrder()
    }, [orderId])

    const handlePayment = async (e) => {
        e.preventDefault()
        setPaymentState('processing')
        setPaymentError(null)

        try {
            const payload = {
                order_id: orderId,
                method: method,
                ...(method === 'upi' ? { vpa } : {
                    card: {
                        number: cardNumber,
                        expiry_month: expiry.split('/')[0],
                        expiry_year: expiry.split('/')[1],
                        cvv,
                        holder_name: holderName
                    }
                })
            }

            const res = await fetch('/api/v1/payments/public', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            })

            if (!res.ok) {
                const errData = await res.json()
                throw new Error(errData.error?.description || 'Payment creation failed')
            }

            const payment = await res.json()
            pollStatus(payment.id)

        } catch (err) {
            setPaymentState('failed')
            setPaymentError(err.message)
        }
    }

    const [isEmbedded, setIsEmbedded] = useState(false)

    useEffect(() => {
        setIsEmbedded(searchParams.get('embedded') === 'true')
    }, [searchParams])

    const pollStatus = (paymentId) => {
        const interval = setInterval(async () => {
            try {
                const res = await fetch(`/api/v1/payments/${paymentId}/public`)
                if (res.ok) {
                    const data = await res.json()
                    if (data.status === 'success') {
                        clearInterval(interval)
                        setPaymentResult(data)
                        setPaymentState('success')
                        if (isEmbedded) {
                            window.parent.postMessage({ type: 'payment_success', data: data }, '*')
                        }
                    } else if (data.status === 'failed') {
                        clearInterval(interval)
                        setPaymentResult(data)
                        setPaymentState('failed')
                        setPaymentError(data.error_description || 'Payment failed')
                        if (isEmbedded) {
                            window.parent.postMessage({ type: 'payment_failed', data: data }, '*')
                        }
                    }
                }
            } catch (err) { }
        }, 2000)
    }

    if (loading) return <div className="checkout-wrapper"><div className="spinner"></div></div>
    if (error) return (
        <div className="checkout-wrapper">
            <div className="checkout-card" style={{ textAlign: 'center', padding: '2rem' }}>
                <h2 style={{ color: '#ef4444' }}>Error</h2>
                <p>{error}</p>
            </div>
        </div>
    )

    return (
        <div className="checkout-wrapper" data-test-id="checkout-container">
            <div className="checkout-card">
                {/* Order Summary Header */}
                <div className="order-summary" data-test-id="order-summary">
                    <div style={{ opacity: 0.8, fontSize: '0.875rem' }}>PAYING</div>
                    <div className="amount" data-test-id="order-amount">₹{(order.amount / 100).toFixed(2)}</div>
                    <div className="order-id" data-test-id="order-id">Order ID: {order.id}</div>
                </div>

                <div className="payment-section">
                    {paymentState === 'input' && (
                        <>
                            <div className="tabs" data-test-id="payment-methods">
                                <button
                                    className={`tab ${method === 'upi' ? 'active' : ''}`}
                                    onClick={() => setMethod('upi')}
                                    data-test-id="method-upi"
                                >
                                    UPI
                                </button>
                                <button
                                    className={`tab ${method === 'card' ? 'active' : ''}`}
                                    onClick={() => setMethod('card')}
                                    data-test-id="method-card"
                                >
                                    Card
                                </button>
                            </div>

                            {method === 'upi' && (
                                <form onSubmit={handlePayment} data-test-id="upi-form">
                                    <div className="input-group">
                                        <label className="label">UPI ID</label>
                                        <input
                                            className="input"
                                            placeholder="username@bank"
                                            value={vpa}
                                            onChange={e => setVpa(e.target.value)}
                                            required
                                            data-test-id="vpa-input"
                                        />
                                    </div>
                                    <button className="pay-btn" data-test-id="pay-button">Pay Now</button>
                                </form>
                            )}

                            {method === 'card' && (
                                <form onSubmit={handlePayment} data-test-id="card-form">
                                    <div className="input-group">
                                        <label className="label">Card Number</label>
                                        <input
                                            className="input"
                                            placeholder="0000 0000 0000 0000"
                                            value={cardNumber}
                                            onChange={e => setCardNumber(e.target.value)}
                                            required
                                            data-test-id="card-number-input"
                                        />
                                    </div>
                                    <div className="row">
                                        <div className="input-group">
                                            <label className="label">Expiry</label>
                                            <input
                                                className="input"
                                                placeholder="MM/YY"
                                                value={expiry}
                                                onChange={e => setExpiry(e.target.value)}
                                                required
                                                data-test-id="expiry-input"
                                            />
                                        </div>
                                        <div className="input-group">
                                            <label className="label">CVV</label>
                                            <input
                                                className="input"
                                                placeholder="123"
                                                value={cvv}
                                                onChange={e => setCvv(e.target.value)}
                                                required
                                                data-test-id="cvv-input"
                                            />
                                        </div>
                                    </div>
                                    <div className="input-group">
                                        <label className="label">Cardholder Name</label>
                                        <input
                                            className="input"
                                            placeholder="Name on Card"
                                            value={holderName}
                                            onChange={e => setHolderName(e.target.value)}
                                            required
                                            data-test-id="cardholder-name-input"
                                        />
                                    </div>
                                    <button className="pay-btn" data-test-id="pay-button">Pay Now</button>
                                </form>
                            )}
                        </>
                    )}

                    {paymentState === 'processing' && (
                        <div className="status-view" data-test-id="processing-state">
                            <div className="spinner"></div>
                            <h3>Processing Payment</h3>
                            <p style={{ color: '#6b7280' }}>Please do not close this window</p>
                        </div>
                    )}

                    {paymentState === 'success' && (
                        <div className="status-view" data-test-id="success-state">
                            <div style={{ color: '#10b981', fontSize: '4rem', lineHeight: 1, marginBottom: '1rem' }}>✓</div>
                            <h3 style={{ color: '#065f46', margin: 0 }}>Payment Successful</h3>
                            <p style={{ fontFamily: 'monospace', color: '#6b7280' }} data-test-id="payment-id">ID: {paymentResult?.id}</p>
                        </div>
                    )}

                    {paymentState === 'failed' && (
                        <div className="status-view" data-test-id="error-state">
                            <div style={{ color: '#ef4444', fontSize: '4rem', lineHeight: 1, marginBottom: '1rem' }}>✕</div>
                            <h3 style={{ color: '#991b1b', margin: 0 }}>Payment Failed</h3>
                            <p style={{ color: '#ef4444' }} data-test-id="error-message">{paymentError}</p>
                            <button className="pay-btn" onClick={() => setPaymentState('input')} data-test-id="retry-button">
                                Try Again
                            </button>
                        </div>
                    )}
                </div>
            </div>
        </div>
    )
}
