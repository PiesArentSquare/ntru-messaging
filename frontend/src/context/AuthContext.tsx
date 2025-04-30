import { createContext, useContext, useEffect, useState } from 'react'
import { NTRU } from '../util/ntru.js'

interface AuthContextReturn {
    user: string | null
    login: (u: string, p: string) => Promise<boolean>
    logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextReturn>({
    user: null,
    login: async _ => false,
    logout: async () => {},
})
const authURL      = 'http://localhost:8080'
const handshakeURL = authURL + '/handshake'
const loginURL     = authURL + '/login'
const logoutURL    = authURL + '/logout'
const csrfURL      = authURL + '/csrf'
const meURL        = authURL + '/me'

export const AuthProvider = ({ children }: { children: any }) => {
    const [user, setUser] = useState<string | null>(null)

    const csrf = async () => {
        const res = await fetch(csrfURL, { credentials: 'include' })
        const csrfToken = await res.json()
        return csrfToken.token
    }

    const login = async (username: string, password: string) => {
        const crypto = new NTRU()
        const handshakeRes = await fetch(handshakeURL, {
            method: 'POST',
            headers: {
                'X-XSRF-TOKEN': await csrf(),
            },
            body: crypto.getPublicKey(),
            credentials: 'include',
        })
        crypto.setForeignKey(await handshakeRes.text())
        const res = await fetch(loginURL, {
            method: 'POST',
            headers: {
                'Content-Type': 'text/plain',
                'X-XSRF-TOKEN': await csrf(),
            },
            body: crypto.encrypt(JSON.stringify({ username, password })),
            credentials: 'include',
        })
        if (!res.ok || !await checkAuth()) {
            setUser(null)
            return false
        } else {
            return true
        }
    }

    const logout = async () => {
        await fetch(logoutURL, {
            method: 'POST',
            headers: {
                'X-XSRF-TOKEN': await csrf(),
            },
            credentials: 'include'
        })
        setUser(null)
    }

    const checkAuth = async () => {
        const res = await fetch(meURL, {
            headers: {
                'X-XSRF-TOKEN': await csrf(),
            },
            credentials: 'include'
        })
        if (res.ok) {
            const data = await res.json()
            setUser(data.username)
            return true
        } else {
            setUser(null)
            return false
        }
    }

    useEffect(() => {
        checkAuth()
    }, [])

    return <AuthContext.Provider value={{ user, login, logout }}>
        {children}
    </AuthContext.Provider>
}

export const useAuth = () => useContext(AuthContext)