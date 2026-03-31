import { createBrowserRouter, Navigate, Outlet } from 'react-router';
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
import { useAuth, UserRole } from './contexts/AuthContext';

function ProtectedRoute({ role }: { role?: UserRole }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (role && user.role !== role) {
    return <Navigate to={user.role === 'MANAGER' ? '/dashboard' : '/my-trips'} replace />;
  }
  return <Outlet />;
}

// Redirects already-authenticated users away from /login and /register
function PublicRoute() {
  const { user } = useAuth();
  if (user) return <Navigate to={user.role === 'MANAGER' ? '/dashboard' : '/my-trips'} replace />;
  return <Outlet />;
}

export const router = createBrowserRouter([
  {
    element: <PublicRoute />,
    children: [
      { path: '/login', element: <Login /> },
      { path: '/register', element: <Register /> },
    ],
  },
  {
    path: '/my-trips',
    element: <ProtectedRoute role="DRIVER" />,
    children: [{ index: true, element: <MyTrips /> }],
  },
  {
    path: '/',
    element: <ProtectedRoute role="MANAGER" />,
    children: [
      {
        element: <Layout />,
        children: [
          { index: true, element: <Navigate to="/dashboard" replace /> },
          { path: 'dashboard', element: <Dashboard /> },
          { path: 'vehicles', element: <Vehicles /> },
          { path: 'drivers', element: <Drivers /> },
          { path: 'trips', element: <Trips /> },
          { path: 'maintenance', element: <Maintenance /> },
          { path: 'ai', element: <AIAssistant /> },
          { path: 'settings', element: <Settings /> },
        ],
      },
    ],
  },
  {
    path: '*',
    element: <NotFound />,
  },
]);
