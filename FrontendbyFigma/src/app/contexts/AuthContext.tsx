import React, { createContext, useContext, useState, useEffect } from 'react';
import { setCredentials } from '../services/mockApi';

export type UserRole = 'MANAGER' | 'DRIVER';

export interface User {
  username: string;
  role: UserRole;
}

interface AuthContextType {
  user: User | null;
  login: (username: string, password: string) => Promise<void>;
  register: (username: string, email: string, password: string, role: UserRole) => Promise<void>;
  logout: () => void;
  isAuthenticated: boolean;
  credentials: string | null;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [credentials, setCredentialsState] = useState<string | null>(null);

  // Restore session on page reload
  useEffect(() => {
    const storedUser = sessionStorage.getItem('fleetsync_user');
    const storedCreds = sessionStorage.getItem('fleetsync_credentials');
    if (storedUser && storedCreds) {
      const parsed = JSON.parse(storedUser) as User;
      setUser(parsed);
      setCredentialsState(storedCreds);
      setCredentials(storedCreds); // wire into api module
    }
  }, []);

  const login = async (username: string, password: string) => {
    const basicAuth = btoa(`${username}:${password}`);

    // Verify credentials and fetch role in one request via /api/auth/me
    const meRes = await fetch('/api/auth/me', {
      headers: { Authorization: `Basic ${basicAuth}` },
    });

    if (meRes.status === 401) throw new Error('Invalid username or password');
    if (!meRes.ok) throw new Error('Login failed. Please try again.');

    const { role } = await meRes.json() as { username: string; role: string };

    const userData: User = { username, role: role as UserRole };
    setUser(userData);
    setCredentialsState(basicAuth);
    setCredentials(basicAuth);
    sessionStorage.setItem('fleetsync_user', JSON.stringify(userData));
    sessionStorage.setItem('fleetsync_credentials', basicAuth);
  };

  const register = async (username: string, email: string, password: string, role: UserRole) => {
    const res = await fetch('/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, email, password, role }),
    });

    if (!res.ok) {
      let message = 'Registration failed';
      try {
        const body = await res.json();
        if (body.detail) message = body.detail;
      } catch { /* ignore */ }
      throw new Error(message);
    }

    // Auto-login after successful registration
    await login(username, password);
  };

  const logout = () => {
    setUser(null);
    setCredentialsState(null);
    setCredentials(null);
    sessionStorage.removeItem('fleetsync_user');
    sessionStorage.removeItem('fleetsync_credentials');
  };

  return (
    <AuthContext.Provider value={{ user, login, register, logout, isAuthenticated: !!user, credentials }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within an AuthProvider');
  return context;
}
