import React, { useEffect, useState } from 'react'
import './App.css'
import { Routes, Route } from 'react-router-dom'
import axios from 'axios'
import Login from './components/Screens/Login'
import CiServer from './components/Screens/CiServer'
import CustomersManagement from './components/Screens/CustomersManagement'
import CompanyManagement from './components/Screens/CompanyManagement'
import Profile from './components/Screens/Profile'
import ProtectRoutes from './components/ProtectedRouter/ProtectRoutes'
import Loading from './components/Loading/Loading'
import 'react-toastify/dist/ReactToastify.css';
import Layout from './components/ProtectedRouter/Layout'
axios.defaults.withCredentials = true
// test basha app-test.js
const App = () => {

  const [loading, setLoading] = useState(false);

  useEffect(() => {
    axios.interceptors.request.use((request) => {
      setLoading(true);
      return request;
    }, (err) => {
      setLoading(false);
      return Promise.reject(err)
    })

    axios.interceptors.response.use((response) => {
      setLoading(false);
      return response;
    }, (err) => {
      setLoading(false);
      return Promise.reject(err)
    })

  }, [])
  return (
    <div  className='min-vh-100'  
    style={{backgroundColor : "rgb(209 217 225)"}}
    >
      <Loading show={loading} />
      <Routes>
        <Route path='/' element={<Login />} />
        <Route path="/login" element={<Login />} />
        <Route element={<ProtectRoutes />}>
          <Route element={<Layout/>}>
          <Route path="/ciServers" element={<CiServer />} />
          <Route path="/customersManagement" element={<CustomersManagement />} />
          <Route path='/companyManagement' element={<CompanyManagement />} />
          <Route path='/profile' element={<Profile />} />
          </Route>
        </Route>
      </Routes>
    </div>
  )
}

export default App