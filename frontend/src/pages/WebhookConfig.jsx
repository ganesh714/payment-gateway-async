import React, { useState, useEffect } from 'react';

const CodeBlock = ({ code, language = 'bash' }) => {
    const [copied, setCopied] = useState(false);

    const handleCopy = () => {
        navigator.clipboard.writeText(code);
        setCopied(true);
        setTimeout(() => setCopied(false), 2000);
    };

    return (
        <div className="code-block-container" style={{ margin: 0 }}>
            <div className="code-display" style={{ background: '#f3f4f6', color: '#374151', padding: '0.5rem 0.75rem', borderRadius: '0.375rem', border: '1px solid #e5e7eb', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <code style={{ fontFamily: 'monospace', fontSize: '0.875rem' }}>{code}</code>
                <button
                    onClick={handleCopy}
                    className="btn-link text-xs"
                    style={{ fontSize: '0.75rem', textTransform: 'uppercase', marginLeft: '1rem', whiteSpace: 'nowrap' }}
                >
                    {copied ? 'Copied!' : 'Copy'}
                </button>
            </div>
        </div>
    );
};

export default function WebhookConfig() {
    const [merchant, setMerchant] = useState(null);
    const [webhookUrl, setWebhookUrl] = useState('');
    const [webhookSecret, setWebhookSecret] = useState('');
    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [showSecret, setShowSecret] = useState(false);
    const [confirmRegen, setConfirmRegen] = useState(false);

    useEffect(() => {
        fetchData();
    }, []);

    const getAuthHeaders = (creds) => {
        if (!creds) return {};
        return {
            'X-Api-Key': creds.apiKey,
            'X-Api-Secret': creds.apiSecret,
            'Content-Type': 'application/json'
        };
    };

    const fetchData = async () => {
        try {
            // 1. Fetch Credentials first (public endpoint)
            const credsRes = await fetch('/api/v1/test/merchant');
            if (!credsRes.ok) throw new Error('Failed to fetch credentials');
            const creds = await credsRes.json();
            setMerchant(creds);

            const headers = getAuthHeaders(creds);

            // 2. Fetch Merchant Config
            const res = await fetch('/api/v1/merchants/me', { headers });
            if (res.ok) {
                const data = await res.json();
                setWebhookUrl(data.webhook_url || '');
                setWebhookSecret(data.webhook_secret || 'Not Configured');
            }

            // 3. Fetch Logs
            const logsRes = await fetch('/api/v1/webhooks', { headers });
            if (logsRes.ok) {
                const logsData = await logsRes.json();
                setLogs(logsData.data || []);
            }
        } catch (err) {
            console.error('Failed to fetch webhook config', err);
        } finally {
            setLoading(false);
        }
    };

    const handleSave = async () => {
        setSaving(true);
        try {
            const res = await fetch('/api/v1/merchants/me/webhook', {
                method: 'PUT',
                headers: getAuthHeaders(merchant),
                body: JSON.stringify({ webhook_url: webhookUrl }),
            });
            if (res.ok) alert('Webhook URL updated successfully');
            else alert('Failed to update webhook URL');
        } catch (err) {
            alert('Error saving webhook URL');
        } finally {
            setSaving(false);
        }
    };

    const handleRegenerateSecret = async () => {
        if (!confirmRegen) {
            setConfirmRegen(true);
            return;
        }

        try {
            const res = await fetch('/api/v1/merchants/me/webhook/secret', {
                method: 'POST',
                headers: getAuthHeaders(merchant)
            });
            if (res.ok) {
                const data = await res.json();
                setWebhookSecret(data.secret);
                alert('Secret regenerated');
                setConfirmRegen(false);
            }
        } catch (err) {
            alert('Failed to regenerate secret');
        }
    };

    const handleRetry = async (logId) => {
        try {
            await fetch(`/api/v1/webhooks/${logId}/retry`, {
                method: 'POST',
                headers: getAuthHeaders(merchant)
            });
            alert('Retry initiated');
            // Refresh logs
            const logsRes = await fetch('/api/v1/webhooks', { headers: getAuthHeaders(merchant) });
            if (logsRes.ok) {
                const logsData = await logsRes.json();
                setLogs(logsData.data || []);
            }
        } catch (err) {
            alert('Failed to retry webhook');
        }
    };

    if (loading) return <div className="p-6">Loading...</div>;

    return (
        <div className="p-6 max-w-5xl mx-auto">
            <div className="header mb-6">
                <h2 className="title">Webhook Configuration</h2>
                <p className="text-gray-500 mt-4">Manage real-time event notifications.</p>
            </div>

            <div className="card p-6 mb-6">
                <div className="flex justify-between items-center mb-4">
                    <h3 className="text-lg font-bold">Endpoint Details</h3>
                    <span className={`status-badge ${webhookUrl ? 'status-active' : 'status-inactive'}`}>
                        {webhookUrl ? 'Active' : 'Inactive'}
                    </span>
                </div>

                <div className="space-y-4">
                    <div>
                        <label className="form-label">Webhook URL</label>
                        <div className="flex gap-2">
                            <input
                                type="url"
                                className="input-field"
                                placeholder="https://your-server.com/webhooks"
                                value={webhookUrl}
                                onChange={(e) => setWebhookUrl(e.target.value)}
                            />
                            <button
                                onClick={handleSave}
                                disabled={saving}
                                className="btn btn-primary"
                                style={{ whiteSpace: 'nowrap' }}
                            >
                                {saving ? 'Saving...' : 'Save URL'}
                            </button>
                        </div>
                    </div>

                    <div>
                        <label className="form-label">Signing Secret</label>
                        <p className="text-sm text-gray-500 mb-2">Used to verify that events originated from us (HMAC-SHA256).</p>
                        <div className="flex items-center gap-2 bg-gray-50 p-2 rounded border" style={{ background: '#f9fafb', padding: '0.5rem', borderRadius: '0.375rem', border: '1px solid #e5e7eb' }}>
                            <div className="flex-1 font-mono text-sm break-all" style={{ flex: 1, wordBreak: 'break-all' }}>
                                {showSecret ? webhookSecret : '••••••••••••••••••••••••••••••••'}
                            </div>
                            <button onClick={() => setShowSecret(!showSecret)} className="btn btn-secondary text-sm">
                                {showSecret ? 'Hide' : 'Show'}
                            </button>
                            <button
                                onClick={() => {
                                    navigator.clipboard.writeText(webhookSecret);
                                    alert('Copied!');
                                }}
                                className="btn btn-secondary text-sm"
                            >
                                Copy
                            </button>
                        </div>
                        <div className="mt-4">
                            {confirmRegen ? (
                                <div className="flex items-center gap-2">
                                    <span className="text-sm text-red-600" style={{ color: '#dc2626' }}>Are you sure? Old secrets will stop working immediately.</span>
                                    <button onClick={handleRegenerateSecret} className="btn btn-danger text-sm">Yes, Roll Key</button>
                                    <button onClick={() => setConfirmRegen(false)} className="btn btn-secondary text-sm">Cancel</button>
                                </div>
                            ) : (
                                <button onClick={handleRegenerateSecret} className="btn btn-link text-sm text-red-600" style={{ color: '#dc2626' }}>
                                    Roll Signing Key
                                </button>
                            )}
                        </div>
                    </div>
                </div>
            </div>

            <div className="card">
                <div className="p-6 border-b" style={{ borderBottom: '1px solid #e5e7eb' }}>
                    <h3 className="text-lg font-bold">Recent Deliveries</h3>
                </div>
                <div className="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>Status</th>
                                <th>Event</th>
                                <th>Attempted At</th>
                                <th>Duration</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {logs.length === 0 ? (
                                <tr>
                                    <td colSpan="5" className="text-center text-gray-500" style={{ textAlign: 'center' }}>No webhook logs found.</td>
                                </tr>
                            ) : (
                                logs.map((log) => (
                                    <tr key={log.id}>
                                        <td>
                                            <span className={`status-badge ${log.status === 'SUCCESS' ? 'status-active' : 'badge-failed'}`} style={log.status === 'SUCCESS' ? {} : { background: '#fef2f2', color: '#b91c1c' }}>
                                                {log.status}
                                            </span>
                                            <div className="text-xs text-gray-500 mt-1" style={{ fontSize: '0.75rem' }}>{log.response_code || '-'}</div>
                                        </td>
                                        <td>
                                            <div className="font-medium">{log.event_type}</div>
                                            <div className="text-xs text-gray-500 font-mono">{log.id.substring(0, 8)}...</div>
                                        </td>
                                        <td className="text-sm">
                                            {new Date(log.created_at).toLocaleString()}
                                        </td>
                                        <td className="text-sm font-mono">
                                            {log.duration_ms ? `${log.duration_ms}ms` : '-'}
                                        </td>
                                        <td>
                                            {log.status === 'FAILED' && (
                                                <button onClick={() => handleRetry(log.id)} className="btn btn-secondary text-xs">
                                                    Retry
                                                </button>
                                            )}
                                            <button className="btn btn-link text-xs ml-2">Details</button>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}
