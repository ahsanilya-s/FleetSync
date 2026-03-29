import { createBrowserRouter, Navigate } from 'react-router';
import { Login } from './pages/Login';
import { Register } from './pages/Register';
import { Dashboard } from './pages/Dashboard';
import { Vehicles } from './pages/Vehicles';
import { Drivers } from './pages/Drivers';
import { Trips } from './pages/Trips';
import { Maintenance } from './pages/Maintenance';
import { AIAssistant } from './pages/AIAssistant';
import { MyTrips } from './pages/MyTrips';
import { Settings } from './pages/Settings';
import { NotFound } from './pages/NotFound';
import { Layout } from './components/Layout';

export const router = createBrowserRouter([
  {
    path: '/login',
    element: <Login />,
  },
  {
    path: '/register',
    element: <Register />,
  },
  {
    path: '/my-trips',
    element: <MyTrips />,
  },
  {
    path: '/',
    element: <Layout />,
    children: [
      {
        index: true,
        element: <Navigate to="/dashboard" replace />,
      },
      {
        path: 'dashboard',
        element: <Dashboard />,
      },
      {
        path: 'vehicles',
        element: <Vehicles />,
      },
      {
        path: 'drivers',
        element: <Drivers />,
      },
      {
        path: 'trips',
        element: <Trips />,
      },
      {
        path: 'maintenance',
        element: <Maintenance />,
      },
      {
        path: 'ai',
        element: <AIAssistant />,
      },
      {
        path: 'settings',
        element: <Settings />,
      },
    ],
  },
  {
    path: '*',
    element: <NotFound />,
  },
]);
