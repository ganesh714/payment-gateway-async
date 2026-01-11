import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Checkout from './pages/Checkout'

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Checkout />} />
                <Route path="/checkout" element={<Checkout />} />
                <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
        </BrowserRouter>
    )
}

export default App
