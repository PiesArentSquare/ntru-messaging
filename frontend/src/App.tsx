import { Route, BrowserRouter as Router, Routes } from 'react-router-dom'
import Chat from './pages/Chat'
import Login from './pages/Login'
import PrivateRoute from './components/PrivateRoute'

export default () => <Router>
    <Routes>
        <Route index element={<PrivateRoute><Chat /></PrivateRoute>} />
        <Route path='/login' element={<Login />} />
    </Routes>
</Router>