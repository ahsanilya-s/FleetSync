import { NavLink } from 'react-router';
import { Home, Truck, Users, MapPin, Wrench, Bot, Settings, LogOut } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';

export function Sidebar() {
  const { logout } = useAuth();

  const navItems = [
    { to: '/dashboard', icon: Home, label: 'Dashboard' },
    { to: '/vehicles', icon: Truck, label: 'Vehicles' },
    { to: '/drivers', icon: Users, label: 'Drivers' },
    { to: '/trips', icon: MapPin, label: 'Trips' },
    { to: '/maintenance', icon: Wrench, label: 'Maintenance' },
    { to: '/ai', icon: Bot, label: 'AI Assistant' },
  ];

  return (
    <div className="w-64 h-screen bg-[#0F172A] text-white flex flex-col fixed left-0 top-0">
      {/* Logo */}
      <div className="px-6 py-6 border-b border-[#1E293B]">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 bg-[#3B82F6] rounded-lg flex items-center justify-center">
            <Truck className="h-6 w-6" />
          </div>
          <div>
            <h1 className="font-bold text-lg">FleetSync</h1>
            <p className="text-xs text-[#94A3B8]">Smart Fleet. Smarter Decisions.</p>
          </div>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors ${
                isActive
                  ? 'bg-[#3B82F6] text-white'
                  : 'text-[#94A3B8] hover:bg-[#1E293B] hover:text-white'
              }`
            }
          >
            <item.icon className="h-5 w-5" />
            <span>{item.label}</span>
          </NavLink>
        ))}
      </nav>

      {/* Bottom actions */}
      <div className="px-3 py-4 border-t border-[#1E293B] space-y-1">
        <NavLink
          to="/settings"
          className={({ isActive }) =>
            `flex items-center gap-3 px-3 py-2.5 rounded-lg transition-colors ${
              isActive
                ? 'bg-[#3B82F6] text-white'
                : 'text-[#94A3B8] hover:bg-[#1E293B] hover:text-white'
            }`
          }
        >
          <Settings className="h-5 w-5" />
          <span>Settings</span>
        </NavLink>
        <button
          onClick={logout}
          className="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-[#94A3B8] hover:bg-[#1E293B] hover:text-white transition-colors"
        >
          <LogOut className="h-5 w-5" />
          <span>Logout</span>
        </button>
      </div>
    </div>
  );
}
