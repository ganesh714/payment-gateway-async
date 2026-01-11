import { useState, useEffect } from 'react'
import { BrowserRouter, Routes, Route, Navigate, Link, useLocation } from 'react-router-dom'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import Transactions from './pages/Transactions'
import WebhookConfig from './pages/WebhookConfig'
import ApiDocs from './pages/ApiDocs'

// Layout Component
const Layout = ({ children }) => {
    const location = useLocation()

    const isActive = (path) => location.pathname === path ? 'active' : ''

    return (
        <div className="app-layout">
            <aside className="sidebar">
                <div className="brand">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                        <path d="M2 17L12 22L22 17" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                        <path d="M2 12L12 17L22 12" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                    </svg>
                    PayGate
                </div>
                <nav>
                    <Link to="/dashboard" className={`nav-link ${isActive('/dashboard')}`}>Overview</Link>
                    <Link to="/dashboard/transactions" className={`nav-link ${isActive('/dashboard/transactions')}`}>Transactions</Link>
                    <Link to="/dashboard/webhooks" className={`nav-link ${isActive('/dashboard/webhooks')}`}>Webhooks</Link>
                    <Link to="/dashboard/docs" className={`nav-link ${isActive('/dashboard/docs')}`}>API Docs</Link>
                    {/* Add more links here later */}
                </nav>
            </aside>
            <main className="main-content">
                {children}
            </main>
        </div>
    )
}

function App() {
    const [isAuthenticated, setIsAuthenticated] = useState(false)

    // Simple check on mount (in real app, check token)
    useEffect(() => {
        const auth = localStorage.getItem('auth')
        if (auth) setIsAuthenticated(true)
    }, [])

    const handleLogin = () => {
        localStorage.setItem('auth', 'true')
        setIsAuthenticated(true)
    }

    const ProtectedRoute = ({ children }) => {
        if (!isAuthenticated) return <Navigate to="/login" />
        return <Layout>{children}</Layout>
    }

    return (
        <BrowserRouter>
            <Routes>
                <Route path="/login" element={<Login onLogin={handleLogin} />} />
                <Route path="/dashboard" element={
                    <ProtectedRoute>
                        <Dashboard />
                    </ProtectedRoute>
                } />
                <Route path="/dashboard/transactions" element={
                    <ProtectedRoute>
                        <Transactions />
                    </ProtectedRoute>
                } />
                <Route path="/dashboard/webhooks" element={
                    <ProtectedRoute>
                        <WebhookConfig />
                    </ProtectedRoute>
                } />
                <Route path="/dashboard/docs" element={
                    <ProtectedRoute>
                        <ApiDocs />
                    </ProtectedRoute>
                } />
                <Route path="/" element={<Navigate to="/dashboard" />} />
            </Routes>
        </BrowserRouter>
    )
}

export default App
