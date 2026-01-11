import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

export default function Login({ onLogin }) {
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const navigate = useNavigate()

    const handleSubmit = (e) => {
        e.preventDefault()
        // Hardcoded auth for demo
        if (email === 'test@example.com') { // simplified check
            onLogin()
            navigate('/dashboard')
        } else {
            alert('Invalid credentials')
        }
    }

    return (
        <div className="auth-container">
            <div className="auth-card">
                <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
                    <h1 style={{ color: 'var(--primary)', margin: 0 }}>PayGate</h1>
                    <p style={{ color: 'var(--text-muted)', marginTop: '0.5rem' }}>Sign in to your dashboard</p>
                </div>
                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label className="form-label">Email Address</label>
                        <input
                            type="email"
                            className="form-input"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            placeholder="test@example.com"
                            required
                        />
                    </div>
                    <div className="form-group">
                        <label className="form-label">Password</label>
                        <input
                            type="password"
                            className="form-input"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="••••••••"
                            required
                        />
                    </div>
                    <button type="submit" className="btn-primary">
                        Sign In
                    </button>
                </form>
                <div style={{ marginTop: '1.5rem', textAlign: 'center', fontSize: '0.875rem', color: 'var(--text-muted)' }}>
                    Use <strong>test@example.com</strong> and any password
                </div>
            </div>
        </div>
    )
}
