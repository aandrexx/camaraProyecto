import { useAuth } from './context/AuthContext.jsx'
import AuthScreen from './components/AuthScreen.jsx'
import Dashboard from './components/Dashboard.jsx'

export default function App() {
  const { session } = useAuth()
  return session ? <Dashboard /> : <AuthScreen />
}
