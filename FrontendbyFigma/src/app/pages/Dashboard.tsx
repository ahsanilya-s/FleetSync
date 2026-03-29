import { useState, useEffect } from 'react';
import { Truck, Users, MapPin, Wrench, ArrowRight, Send } from 'lucide-react';
import { KPICard } from '../components/KPICard';
import { StatusBadge } from '../components/StatusBadge';
import { Button } from '../components/Button';
import { api, Vehicle, Driver, Trip, MaintenanceRecord } from '../services/mockApi';
import { formatDistanceToNow } from 'date-fns';

export function Dashboard() {
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [drivers, setDrivers] = useState<Driver[]>([]);
  const [trips, setTrips] = useState<Trip[]>([]);
  const [maintenanceAlerts, setMaintenanceAlerts] = useState<MaintenanceRecord[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [vehiclesData, driversData, tripsData, alertsData] = await Promise.all([
        api.getVehicles(),
        api.getDrivers(),
        api.getTrips(),
        api.getMaintenanceAlerts(30),
      ]);
      setVehicles(vehiclesData);
      setDrivers(driversData);
      setTrips(tripsData);
      setMaintenanceAlerts(alertsData);
    } catch (error) {
      console.error('Failed to load dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  const availableVehicles = vehicles.filter(v => v.status === 'AVAILABLE').length;
  const onTripVehicles = vehicles.filter(v => v.status === 'ON_TRIP').length;
  const availableDrivers = drivers.filter(d => d.status === 'AVAILABLE').length;
  const onTripDrivers = drivers.filter(d => d.status === 'ON_TRIP').length;
  const activeTrips = trips.filter(t => t.status === 'SCHEDULED' || t.status === 'IN_PROGRESS').length;
  const recentTrips = trips.slice(0, 5);

  const getDriverName = (driverId: number) => {
    return drivers.find(d => d.id === driverId)?.fullName || 'Unknown';
  };

  const getVehicleInfo = (vehicleId: number) => {
    const vehicle = vehicles.find(v => v.id === vehicleId);
    return vehicle ? `${vehicle.name} (${vehicle.plateNumber})` : 'Unknown';
  };

  const getDaysUntilService = (nextServiceDate: string | null) => {
    if (!nextServiceDate) return null;
    const days = Math.ceil((new Date(nextServiceDate).getTime() - new Date().getTime()) / (1000 * 60 * 60 * 24));
    return days;
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-[#1E293B]">Dashboard</h1>
        <p className="text-[#64748B]">Welcome back! Here's your fleet overview.</p>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <KPICard
          icon={Truck}
          label="Total Vehicles"
          value={vehicles.length}
          subtext={`${availableVehicles} Available · ${onTripVehicles} On Trip`}
          iconBg="bg-blue-100"
          iconColor="text-blue-600"
        />
        <KPICard
          icon={Users}
          label="Total Drivers"
          value={drivers.length}
          subtext={`${availableDrivers} Available · ${onTripDrivers} On Trip`}
          iconBg="bg-emerald-100"
          iconColor="text-emerald-600"
        />
        <KPICard
          icon={MapPin}
          label="Active Trips"
          value={activeTrips}
          subtext={`${trips.filter(t => t.status === 'SCHEDULED').length} Scheduled · ${trips.filter(t => t.status === 'IN_PROGRESS').length} In Progress`}
          iconBg="bg-amber-100"
          iconColor="text-amber-600"
        />
        <KPICard
          icon={Wrench}
          label="Maintenance Alerts"
          value={maintenanceAlerts.length}
          subtext={maintenanceAlerts.length > 0 ? 'Action required' : 'All up to date'}
          iconBg={maintenanceAlerts.length > 0 ? 'bg-red-100' : 'bg-emerald-100'}
          iconColor={maintenanceAlerts.length > 0 ? 'text-red-600' : 'text-emerald-600'}
        />
      </div>

      {/* Content Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Trips */}
        <div className="bg-white rounded-lg shadow-sm border border-[#E2E8F0] p-6">
          <h3 className="text-lg font-semibold text-[#1E293B] mb-4">Recent Trips</h3>
          <div className="space-y-3">
            {recentTrips.length === 0 ? (
              <p className="text-sm text-[#64748B] text-center py-8">No trips yet</p>
            ) : (
              recentTrips.map((trip) => (
                <div key={trip.id} className="flex items-center justify-between p-3 bg-[#F8FAFC] rounded-lg hover:bg-[#F1F5F9] transition-colors">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <span className="text-sm font-medium text-[#1E293B]">Trip #{trip.id}</span>
                      <StatusBadge status={trip.status} showPulse={trip.status === 'IN_PROGRESS'} />
                    </div>
                    <p className="text-xs text-[#64748B]">{getDriverName(trip.driverId)} · {getVehicleInfo(trip.vehicleId)}</p>
                    <div className="flex items-center gap-2 text-xs text-[#64748B] mt-1">
                      <span>{trip.origin}</span>
                      <ArrowRight className="h-3 w-3" />
                      <span>{trip.destination}</span>
                    </div>
                  </div>
                  <div className="text-xs text-[#94A3B8] ml-4">
                    {formatDistanceToNow(new Date(trip.createdAt), { addSuffix: true })}
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Upcoming Maintenance */}
        <div className="bg-white rounded-lg shadow-sm border border-[#E2E8F0] p-6">
          <h3 className="text-lg font-semibold text-[#1E293B] mb-4">Upcoming Maintenance</h3>
          <div className="space-y-3">
            {maintenanceAlerts.length === 0 ? (
              <p className="text-sm text-[#64748B] text-center py-8">No maintenance alerts</p>
            ) : (
              maintenanceAlerts.map((alert) => {
                const days = getDaysUntilService(alert.nextServiceDate);
                const isOverdue = days !== null && days < 0;
                const isDueSoon = days !== null && days <= 7 && days >= 0;
                const vehicle = vehicles.find(v => v.id === alert.vehicleId);

                return (
                  <div key={alert.id} className="flex items-start justify-between p-3 bg-[#F8FAFC] rounded-lg">
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-1">
                        <span className="text-sm font-medium text-[#1E293B]">{vehicle?.name}</span>
                        <span
                          className={`text-xs px-2 py-0.5 rounded-full ${
                            isOverdue
                              ? 'bg-red-100 text-red-700'
                              : isDueSoon
                              ? 'bg-amber-100 text-amber-700'
                              : 'bg-emerald-100 text-emerald-700'
                          }`}
                        >
                          {isOverdue ? `Overdue ${Math.abs(days!)} days` : `${days} days`}
                        </span>
                      </div>
                      <p className="text-xs text-[#64748B]">
                        {alert.type.replace('_', ' ')} - Due: {new Date(alert.nextServiceDate!).toLocaleDateString()}
                      </p>
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </div>
      </div>

      {/* Fleet Status & AI Quick Ask */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Fleet Status Chart */}
        <div className="lg:col-span-2 bg-white rounded-lg shadow-sm border border-[#E2E8F0] p-6">
          <h3 className="text-lg font-semibold text-[#1E293B] mb-4">Fleet Status Overview</h3>
          <div className="space-y-4">
            <div>
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm text-[#64748B]">Available Vehicles</span>
                <span className="text-sm font-medium text-[#1E293B]">{availableVehicles} / {vehicles.length}</span>
              </div>
              <div className="h-3 bg-[#F1F5F9] rounded-full overflow-hidden">
                <div
                  className="h-full bg-emerald-500 transition-all duration-500"
                  style={{ width: `${vehicles.length > 0 ? (availableVehicles / vehicles.length) * 100 : 0}%` }}
                />
              </div>
            </div>
            <div>
              <div className="flex items-center justify-between mb-2">
                <span className="text-sm text-[#64748B]">On Trip</span>
                <span className="text-sm font-medium text-[#1E293B]">{onTripVehicles} / {vehicles.length}</span>
              </div>
              <div className="h-3 bg-[#F1F5F9] rounded-full overflow-hidden">
                <div
                  className="h-full bg-amber-500 transition-all duration-500"
                  style={{ width: `${vehicles.length > 0 ? (onTripVehicles / vehicles.length) * 100 : 0}%` }}
                />
              </div>
            </div>

            <div className="pt-4 border-t border-[#E2E8F0] grid grid-cols-2 gap-4">
              <div className="text-center p-3 bg-emerald-50 rounded-lg">
                <p className="text-2xl font-bold text-emerald-600">{Math.round(vehicles.length > 0 ? (availableVehicles / vehicles.length) * 100 : 0)}%</p>
                <p className="text-xs text-[#64748B]">Available</p>
              </div>
              <div className="text-center p-3 bg-amber-50 rounded-lg">
                <p className="text-2xl font-bold text-amber-600">{Math.round(vehicles.length > 0 ? (onTripVehicles / vehicles.length) * 100 : 0)}%</p>
                <p className="text-xs text-[#64748B]">In Use</p>
              </div>
            </div>
          </div>
        </div>

        {/* AI Quick Ask */}
        <div className="bg-gradient-to-br from-[#3B82F6] to-[#2563EB] rounded-lg shadow-sm p-6 text-white">
          <h3 className="text-lg font-semibold mb-2">AI Quick Ask</h3>
          <p className="text-sm text-blue-100 mb-4">Get instant insights about your fleet</p>
          <div className="bg-white/10 rounded-lg p-3 mb-3">
            <input
              type="text"
              placeholder="Ask FleetSync AI anything..."
              className="w-full bg-transparent text-white placeholder-blue-200 focus:outline-none text-sm"
              onKeyDown={(e) => {
                if (e.key === 'Enter') {
                  window.location.href = '/ai';
                }
              }}
            />
          </div>
          <Button
            variant="secondary"
            size="sm"
            className="w-full"
            onClick={() => window.location.href = '/ai'}
          >
            <Send className="h-4 w-4 mr-2" />
            Open AI Assistant
          </Button>
        </div>
      </div>
    </div>
  );
}
