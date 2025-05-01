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
        return (
            <div className='h-screen flex flex-row justify-center bg-brand-background-10 text-brand-text-900'>
                <form onSubmit={handleLogin} className='flex flex-col justify-center w-100'>
                    <span className='flex'><input type='text' className='w-0 min-w-0 flex-grow flex-shrink px-4 py-1 m-4 border-b-2 border-b-brand-text-900/50 placeholder:text-brand-text-900/50 outline-none hover:border-b-brand-primary-500 hover:placeholder:text-brand-primary-500 duration-200 transition-colors' value={username} onChange={e => setUsername(e.target.value)} placeholder='username' /></span>
                    <span className='flex'><input type='password' className='w-0 min-w-0 flex-grow flex-shrink px-4 py-1 m-4 border-b-2 border-b-brand-text-900/50 placeholder:text-brand-text-900/50 outline-none hover:border-b-brand-primary-500 hover:placeholder:text-brand-primary-500 duration-200 transition-colors' value={password} onChange={e => setPassword(e.target.value)} placeholder='password' /></span>
                    <button type='submit' className='p-4 m-4 bg-brand-accent-300 rounded-2xl hover:bg-brand-accent-400 duration-100 hover:cursor-pointer' >Login</button>
                </form>
            </div>
        )
    }
}