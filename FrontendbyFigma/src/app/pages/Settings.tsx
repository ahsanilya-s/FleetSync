import { useAuth } from '../contexts/AuthContext';
import { User, Mail, Shield } from 'lucide-react';

export function Settings() {
  const { user } = useAuth();

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-[#1E293B]">Settings</h1>
        <p className="text-[#64748B]">Manage your account settings</p>
      </div>

      <div className="bg-white rounded-lg shadow-sm border border-[#E2E8F0] p-6">
        <h3 className="text-lg font-semibold text-[#1E293B] mb-6">Account Information</h3>
        
        <div className="space-y-4">
          <div className="flex items-start gap-4 p-4 bg-[#F8FAFC] rounded-lg">
            <div className="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center flex-shrink-0">
              <User className="h-5 w-5 text-[#3B82F6]" />
            </div>
            <div className="flex-1">
              <p className="text-sm text-[#64748B] mb-1">Username</p>
              <p className="text-base font-medium text-[#1E293B]">{user?.username}</p>
            </div>
          </div>

          <div className="flex items-start gap-4 p-4 bg-[#F8FAFC] rounded-lg">
            <div className="w-10 h-10 bg-emerald-100 rounded-lg flex items-center justify-center flex-shrink-0">
              <Mail className="h-5 w-5 text-emerald-600" />
            </div>
            <div className="flex-1">
              <p className="text-sm text-[#64748B] mb-1">Email</p>
              <p className="text-base font-medium text-[#1E293B]">{user?.email}</p>
            </div>
          </div>

          <div className="flex items-start gap-4 p-4 bg-[#F8FAFC] rounded-lg">
            <div className="w-10 h-10 bg-amber-100 rounded-lg flex items-center justify-center flex-shrink-0">
              <Shield className="h-5 w-5 text-amber-600" />
            </div>
            <div className="flex-1">
              <p className="text-sm text-[#64748B] mb-1">Role</p>
              <p className="text-base font-medium text-[#1E293B]">{user?.role}</p>
            </div>
          </div>
        </div>
      </div>

      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
        <p className="text-sm text-blue-800">
          This is a demo application. In a production environment, you would be able to update your profile, change your password, and manage notification preferences here.
        </p>
      </div>
    </div>
  );
}
