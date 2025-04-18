import { useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { Navigate } from 'react-router-dom'

export default () => {
    const { user, login } = useAuth()
    const [username, setUsername] = useState('')
    const [password, setPassword] = useState('')

    const handleLogin = async (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault()
        if (!await login(username, password)) {
            console.log('login failed')
        }
    }

    if (user != null) {
        return <Navigate to='/' />
    } else {   
        return <form onSubmit={handleLogin} >
            <input type='text' value={username} onChange={e => setUsername(e.target.value)} placeholder='username' />
            <input type='password' value={password} onChange={e => setPassword(e.target.value)} placeholder='password' />
            <button type='submit'>Login</button>
        </form>
    }
}