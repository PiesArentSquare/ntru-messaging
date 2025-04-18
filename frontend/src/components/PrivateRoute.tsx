import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default ({children}: {children: any}) => {
    const { user } = useAuth()
    if (user == null)
        return <Navigate to='/login' replace />
    else
        return children
}