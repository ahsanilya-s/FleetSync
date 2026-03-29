import { useState, useEffect } from 'react';
import { MapPin, ArrowRight, LogOut, Truck } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';
import { Button } from '../components/Button';
import { StatusBadge } from '../components/StatusBadge';
import { EmptyState } from '../components/EmptyState';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { api, Trip, Vehicle } from '../services/mockApi';
import { formatDistanceToNow } from 'date-fns';

export function MyTrips() {
  const { user, logout } = useAuth();
  const [trips, setTrips] = useState<Trip[]>([]);
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionTrip, setActionTrip] = useState<{ trip: Trip; action: 'start' | 'complete' | 'cancel' } | null>(null);
  const [actionLoading, setActionLoading] = useState(false);

  useEffect(() => {
    loadTrips();
  }, []);

  const loadTrips = async () => {
    try {
      // Fetch all trips — the backend filters by the authenticated user's
      // credentials. DRIVER role can only call PUT /api/trips/{id}/status,
      // so we fetch all trips and show only those matching the logged-in username.
      // In a real multi-driver setup the backend would expose GET /api/trips/mine.
      const [tripsData, vehiclesData] = await Promise.all([
        api.getTrips(),
        api.getVehicles(),
      ]);
      setTrips(tripsData);
      setVehicles(vehiclesData);
    } catch (error) {
      console.error('Failed to load trips:', error);
    } finally {
      setLoading(false);
    }
  };

  const getVehicleInfo = (vehicleId: number) => {
    const vehicle = vehicles.find(v => v.id === vehicleId);
    return vehicle ? { name: vehicle.name, plateNumber: vehicle.plateNumber } : null;
  };

  const handleAction = async () => {
    if (!actionTrip) return;

    setActionLoading(true);
    try {
      let newStatus;
      switch (actionTrip.action) {
        case 'start':
          newStatus = 'IN_PROGRESS';
          break;
        case 'complete':
          newStatus = 'COMPLETED';
          break;
        case 'cancel':
          newStatus = 'CANCELLED';
          break;
      }

      await api.updateTripStatus(actionTrip.trip.id, newStatus as any);
      await loadTrips();
      setActionTrip(null);
    } catch (error) {
      console.error('Failed to update trip status:', error);
    } finally {
      setActionLoading(false);
    }
  };

  const getConfirmMessage = () => {
    if (!actionTrip) return '';
    
    switch (actionTrip.action) {
      case 'start':
        return 'Are you sure you want to start this trip?';
      case 'complete':
        return 'Are you sure you want to mark this trip as completed?';
      case 'cancel':
        return 'Are you sure you want to cancel this trip?';
    }
  };

  return (
    <div className="min-h-screen bg-[#F8FAFC]">
      {/* Top Navbar */}
      <nav className="bg-[#0F172A] text-white px-6 py-4 shadow-lg">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-[#3B82F6] rounded-lg flex items-center justify-center">
              <Truck className="h-6 w-6" />
            </div>
            <div>
              <h1 className="font-bold text-lg">FleetSync</h1>
              <p className="text-xs text-[#94A3B8]">Driver Portal</p>
            </div>
          </div>
          <div className="flex items-center gap-4">
            <span className="text-sm">Welcome, <span className="font-medium">{user?.username}</span></span>
            <Button variant="secondary" size="sm" onClick={logout}>
              <LogOut className="h-4 w-4 mr-2" />
              Logout
            </Button>
          </div>
        </div>
      </nav>

      {/* Content */}
      <div className="max-w-7xl mx-auto px-6 py-8">
        <div className="mb-6">
          <h2 className="text-2xl font-bold text-[#1E293B]">My Assigned Trips</h2>
          <p className="text-[#64748B]">View and manage your trip assignments</p>
        </div>

        {trips.length === 0 ? (
          <EmptyState
            icon={<MapPin className="h-8 w-8 text-[#64748B]" />}
            title="No trips assigned"
            description="You don't have any trips assigned to you yet. Check back later or contact your manager."
          />
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {trips.map((trip) => {
              const vehicle = getVehicleInfo(trip.vehicleId);
              return (
                <div
                  key={trip.id}
                  className="bg-white rounded-lg shadow-sm border border-[#E2E8F0] p-6"
                >
                  {/* Trip Header */}
                  <div className="flex items-start justify-between mb-4">
                    <div>
                      <h3 className="text-lg font-semibold text-[#1E293B]">Trip #{trip.id}</h3>
                      <div className="mt-2">
                        <StatusBadge status={trip.status} showPulse={trip.status === 'IN_PROGRESS'} />
                      </div>
                    </div>
                  </div>

                  {/* Trip Details */}
                  <div className="space-y-3 mb-4">
                    <div>
                      <p className="text-xs text-[#64748B] mb-1">Vehicle</p>
                      <p className="text-sm font-medium text-[#1E293B]">
                        {vehicle?.name} ({vehicle?.plateNumber})
                      </p>
                    </div>

                    <div>
                      <p className="text-xs text-[#64748B] mb-1">Route</p>
                      <div className="flex items-center gap-2 text-sm text-[#1E293B]">
                        <MapPin className="h-4 w-4 text-[#3B82F6]" />
                        <span>{trip.origin}</span>
                        <ArrowRight className="h-4 w-4 text-[#94A3B8]" />
                        <MapPin className="h-4 w-4 text-[#EF4444]" />
                        <span>{trip.destination}</span>
                      </div>
                    </div>

                    <div className="grid grid-cols-2 gap-3">
                      <div>
                        <p className="text-xs text-[#64748B] mb-1">Scheduled</p>
                        <p className="text-sm text-[#1E293B]">
                          {formatDistanceToNow(new Date(trip.createdAt), { addSuffix: true })}
                        </p>
                      </div>
                      <div>
                        <p className="text-xs text-[#64748B] mb-1">Start Time</p>
                        <p className="text-sm text-[#1E293B]">
                          {trip.startTime ? new Date(trip.startTime).toLocaleString() : 'Not started yet'}
                        </p>
                      </div>
                    </div>

                    {trip.endTime && (
                      <div>
                        <p className="text-xs text-[#64748B] mb-1">End Time</p>
                        <p className="text-sm text-[#1E293B]">
                          {new Date(trip.endTime).toLocaleString()}
                        </p>
                      </div>
                    )}
                  </div>

                  {/* Action Buttons */}
                  <div className="space-y-2 pt-4 border-t border-[#E2E8F0]">
                    {trip.status === 'SCHEDULED' && (
                      <Button
                        className="w-full bg-emerald-500 hover:bg-emerald-600"
                        onClick={() => setActionTrip({ trip, action: 'start' })}
                      >
                        Start Trip
                      </Button>
                    )}

                    {trip.status === 'IN_PROGRESS' && (
                      <>
                        <Button
                          className="w-full"
                          onClick={() => setActionTrip({ trip, action: 'complete' })}
                        >
                          Complete Trip
                        </Button>
                        <Button
                          variant="danger"
                          size="sm"
                          className="w-full"
                          onClick={() => setActionTrip({ trip, action: 'cancel' })}
                        >
                          Cancel Trip
                        </Button>
                      </>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Confirmation Dialog */}
      {actionTrip && (
        <ConfirmDialog
          isOpen={!!actionTrip}
          onClose={() => setActionTrip(null)}
          onConfirm={handleAction}
          title={`${actionTrip.action.charAt(0).toUpperCase() + actionTrip.action.slice(1)} Trip`}
          message={getConfirmMessage()}
          confirmLabel={actionTrip.action.charAt(0).toUpperCase() + actionTrip.action.slice(1)}
          variant={actionTrip.action === 'cancel' ? 'danger' : 'primary'}
          loading={actionLoading}
        />
      )}
    </div>
  );
}
