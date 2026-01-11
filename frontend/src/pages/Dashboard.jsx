import { useEffect, useState } from 'react'

export default function Dashboard() {
    const [merchant, setMerchant] = useState(null)
    const [stats, setStats] = useState({ count: 0, volume: 0, successRate: 0 })

    useEffect(() => {
        async function loadData() {
            try {
                // Fetch Merchant Details
                const merchantRes = await fetch('/api/v1/test/merchant')
                if (!merchantRes.ok) throw new Error('Failed to fetch merchant')
                const merchantData = await merchantRes.json()
                console.log('Merchant Credentials Fetched:', merchantData)
                setMerchant(merchantData)

                // Fetch Transactions for Stats
                console.log('Fetching payments with headers:', {
                    'X-Api-Key': merchantData.apiKey,
                    'X-Api-Secret': merchantData.apiSecret
                })
                const paymentsRes = await fetch('/api/v1/payments', {
                    headers: {
                        'X-Api-Key': merchantData.apiKey,
                        'X-Api-Secret': merchantData.apiSecret
                    }
                })

                if (paymentsRes.ok) {
                    const data = await paymentsRes.json()
                    if (Array.isArray(data)) {
                        const total = data.length
                        const success = data.filter(p => p.status === 'success')
                        const volume = success.reduce((acc, curr) => acc + curr.amount, 0)
                        const rate = total > 0 ? ((success.length / total) * 100).toFixed(1) : 0
                        setStats({ count: total, volume, successRate: rate })
                    }
                }
            } catch (err) {
                console.error('Data loading error:', err)
            }
        }
        loadData()
    }, [])

    return (
        <div>
            <div className="header">
                <h1 className="title">Dashboard</h1>
                <p style={{ color: 'var(--text-muted)' }}>Welcome back, {merchant?.name || 'Merchant'}</p>
            </div>

            <div className="stats-grid">
                <div className="stat-card">
                    <div className="stat-label">Total Volume</div>
                    <div className="stat-value">₹{(stats.volume / 100).toLocaleString('en-IN')}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Transactions</div>
                    <div className="stat-value">{stats.count}</div>
                </div>
                <div className="stat-card">
                    <div className="stat-label">Success Rate</div>
                    <div className="stat-value" style={{ color: 'var(--success-text)' }}>{stats.successRate}%</div>
                </div>
            </div>

            <div className="card" style={{ padding: '1.5rem' }}>
                <h3 style={{ marginTop: 0, marginBottom: '1rem' }}>API Credentials</h3>
                <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)' }}>Use these keys to authenticate your API requests.</p>

                <div style={{ marginBottom: '1rem' }}>
                    <div className="form-label">Publishable Key</div>
                    <div className="api-box">{merchant?.apiKey || 'Loading...'}</div>
                </div>

                <div>
                    <div className="form-label">Secret Key</div>
                    <div className="api-box">{merchant?.apiSecret || 'Loading...'}</div>
                </div>
            </div>
        </div>
    )
}
