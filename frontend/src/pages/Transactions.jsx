import { useEffect, useState } from 'react'

export default function Transactions() {
    const [payments, setPayments] = useState([])

    useEffect(() => {
        async function loadTransactions() {
            try {
                // 1. Get credentials first
                const merchantRes = await fetch('/api/v1/test/merchant')
                if (!merchantRes.ok) return
                const merchantData = await merchantRes.json()

                // 2. Fetch payments with headers
                const res = await fetch('/api/v1/payments', {
                    headers: {
                        'X-Api-Key': merchantData.apiKey,
                        'X-Api-Secret': merchantData.apiSecret
                    }
                })
                if (res.ok) {
                    const data = await res.json()
                    setPayments(data)
                }
            } catch (err) {
                console.error(err)
            }
        }
        loadTransactions()
    }, [])

    return (
        <div>
            <div className="header">
                <h1 className="title">Transactions</h1>
            </div>

            <div className="card table-container">
                <table>
                    <thead>
                        <tr>
                            <th>Payment ID</th>
                            <th>Order ID</th>
                            <th>Amount</th>
                            <th>Method</th>
                            <th>Status</th>
                            <th>Date</th>
                        </tr>
                    </thead>
                    <tbody>
                        {payments.map(payment => (
                            <tr key={payment.id}>
                                <td style={{ fontFamily: 'monospace' }}>{payment.id}</td>
                                <td style={{ fontFamily: 'monospace', color: 'var(--text-muted)' }}>{payment.orderId.substring(0, 14)}...</td>
                                <td style={{ fontWeight: 600 }}>₹{(payment.amount / 100).toFixed(2)}</td>
                                <td>
                                    <span className="badge badge-method">{payment.method}</span>
                                </td>
                                <td>
                                    <span className={`badge ${payment.status === 'success' ? 'badge-success' : 'badge-failed'}`}>
                                        {payment.status}
                                    </span>
                                </td>
                                <td style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
                                    {new Date(payment.createdAt || Date.now()).toLocaleDateString()}
                                </td>
                            </tr>
                        ))}
                        {payments.length === 0 && (
                            <tr>
                                <td colSpan="6" style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>
                                    No transactions found
                                </td>
                            </tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    )
}
