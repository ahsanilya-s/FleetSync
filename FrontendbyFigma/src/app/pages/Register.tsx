import { useState } from 'react';
import { Link } from 'react-router';
import { useAuth, UserRole } from '../contexts/AuthContext';
import { Button } from '../components/Button';
import { Input } from '../components/Input';
import { Select } from '../components/Select';
import { Alert } from '../components/Alert';
import { Truck, Eye, EyeOff } from 'lucide-react';

export function Register() {
  const { register } = useAuth();
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    role: 'MANAGER' as UserRole,
  });
  const [showPassword, setShowPassword] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);

  const validateForm = () => {
    const newErrors: Record<string, string> = {};

    // Username validation
    if (formData.username.length < 3 || formData.username.length > 50) {
      newErrors.username = 'Username must be 3-50 characters';
    }
    if (!/^[a-zA-Z0-9_]+$/.test(formData.username)) {
      newErrors.username = 'Username can only contain letters, numbers, and underscores';
    }

    // Password validation
    if (formData.password.length < 8) {
      newErrors.password = 'Password must be at least 8 characters';
    }

    // Email validation
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = 'Please enter a valid email address';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!validateForm()) return;

    setErrors({});
    setLoading(true);

    try {
      await register(formData.username, formData.email, formData.password, formData.role);
      setSuccess(true);
      // PublicRoute will automatically redirect once user is set in AuthContext
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Registration failed';
      if (errorMessage.toLowerCase().includes('username')) {
        setErrors({ username: errorMessage });
      } else if (errorMessage.toLowerCase().includes('email')) {
        setErrors({ email: errorMessage });
      } else {
        setErrors({ general: errorMessage });
      }
    } finally {
      setLoading(false);
    }
  };

  const getPasswordStrength = () => {
    const password = formData.password;
    if (password.length === 0) return null;
    if (password.length < 8) return { label: 'Too short', color: 'bg-red-500', width: '25%' };
    if (password.length < 12) return { label: 'Medium', color: 'bg-amber-500', width: '50%' };
    if (!/[A-Z]/.test(password) || !/[a-z]/.test(password) || !/[0-9]/.test(password)) {
      return { label: 'Medium', color: 'bg-amber-500', width: '50%' };
    }
    return { label: 'Strong', color: 'bg-emerald-500', width: '100%' };
  };

  const passwordStrength = getPasswordStrength();

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#0F172A] via-[#1E293B] to-[#0F172A] flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Logo and branding */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-[#3B82F6] rounded-2xl mb-4">
            <Truck className="h-8 w-8 text-white" />
          </div>
          <h1 className="text-3xl font-bold text-white mb-2">FleetSync</h1>
          <p className="text-[#94A3B8]">Create your account</p>
        </div>

        {/* Register card */}
        <div className="bg-white rounded-xl shadow-xl p-8">
          <h2 className="text-2xl font-semibold text-[#1E293B] mb-6">Register</h2>

          {success && (
            <Alert variant="success" className="mb-4">
              Account created! Redirecting...
            </Alert>
          )}

          {errors.general && (
            <Alert variant="error" className="mb-4">
              {errors.general}
            </Alert>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <Input
              label="Username"
              type="text"
              placeholder="Enter username"
              value={formData.username}
              onChange={(e) => setFormData({ ...formData, username: e.target.value })}
              error={errors.username}
              hint="3-50 characters, letters/numbers/underscores only"
              required
            />

            <Input
              label="Email"
              type="email"
              placeholder="Enter your email"
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              error={errors.email}
              required
            />

            <div>
              <div className="relative">
                <Input
                  label="Password"
                  type={showPassword ? 'text' : 'password'}
                  placeholder="Enter password"
                  value={formData.password}
                  onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                  error={errors.password}
                  required
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-[38px] text-[#64748B] hover:text-[#1E293B]"
                >
                  {showPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
                </button>
              </div>
              
              {passwordStrength && (
                <div className="mt-2">
                  <div className="h-1.5 bg-[#E2E8F0] rounded-full overflow-hidden">
                    <div
                      className={`h-full ${passwordStrength.color} transition-all duration-300`}
                      style={{ width: passwordStrength.width }}
                    />
                  </div>
                  <p className="text-xs text-[#64748B] mt-1">{passwordStrength.label}</p>
                </div>
              )}
            </div>

            <Select
              label="Role"
              value={formData.role}
              onChange={(e) => setFormData({ ...formData, role: e.target.value as UserRole })}
              options={[
                { value: 'MANAGER', label: 'Manager' },
                { value: 'DRIVER', label: 'Driver' },
              ]}
              required
            />

            <Button type="submit" className="w-full" loading={loading} disabled={success}>
              Create Account
            </Button>
          </form>

          <p className="mt-6 text-center text-sm text-[#64748B]">
            Already have an account?{' '}
            <Link to="/login" className="text-[#3B82F6] hover:underline font-medium">
              Sign In
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
