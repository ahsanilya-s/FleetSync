import { useEffect } from 'react';
import { RouterProvider } from 'react-router';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import { router } from './routes';

function AppContent() {
  const { user, isAuthenticated } = useAuth();

  useEffect(() => {
    const currentPath = window.location.pathname;
    const publicPaths = ['/login', '/register'];

    if (!isAuthenticated) {
      // Only redirect to login if not already on a public page
      if (!publicPaths.includes(currentPath)) {
        window.location.href = '/login';
      }
    } else {
      // Redirect away from login/register once authenticated
      if (publicPaths.includes(currentPath)) {
        window.location.href = user?.role === 'MANAGER' ? '/dashboard' : '/my-trips';
      }

      // Prevent drivers from accessing manager-only pages
      if (user?.role === 'DRIVER') {
        const managerPages = ['/dashboard', '/vehicles', '/drivers', '/trips', '/maintenance', '/ai', '/settings'];
        if (managerPages.some(page => currentPath.startsWith(page))) {
          window.location.href = '/my-trips';
        }
      }
    }
  }, [isAuthenticated, user]);

  return <RouterProvider router={router} />;
}

export default function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}
