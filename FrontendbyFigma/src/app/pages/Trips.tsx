import { useState, useEffect } from 'react';
import { Plus, ArrowRight, MapPin as MapPinIcon } from 'lucide-react';
import { Button } from '../components/Button';
import { Select } from '../components/Select';
import { Modal } from '../components/Modal';
import { StatusBadge } from '../components/StatusBadge';
import { EmptyState } from '../components/EmptyState';
import { Alert } from '../components/Alert';
import { Input } from '../components/Input';
import { api, Trip, Vehicle, Driver, TripStatus } from '../services/mockApi';
import { formatDistanceToNow } from 'date-fns';

export function Trips() {
  const [trips, setTrips] = useState<Trip[]>([]);
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [drivers, setDrivers] = useState<Driver[]>([]);
  const [filteredTrips, setFilteredTrips] = useState<Trip[]>([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('all');
  const [driverFilter, setDriverFilter] = useState('all');
  const [vehicleFilter, setVehicleFilter] = useState('all');
  
  // Modal states
  const [showAssignModal, setShowAssignModal] = useState(false);
  const [updateStatusTrip, setUpdateStatusTrip] = useState<Trip | null>(null);
  
  // Form states
  const [formData, setFormData] = useState({
    driverId: '',
    vehicleId: '',
    origin: '',
    destination: '',
  });
  const [formError, setFormError] = useState('');
  const [formLoading, setFormLoading] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  useEffect(() => {
    filterTrips();
  }, [trips, statusFilter, driverFilter, vehicleFilter]);

  const loadData = async () => {
    try {
      const [tripsData, vehiclesData, driversData] = await Promise.all([
        api.getTrips(),
        api.getVehicles(),
        api.getDrivers(),
      ]);
      setTrips(tripsData);
      setVehicles(vehiclesData);
      setDrivers(driversData);
    } catch (error) {
      console.error('Failed to load data:', error);
    } finally {
      setLoading(false);
    }
  };

  const filterTrips = () => {
    let filtered = [...trips];

    if (statusFilter !== 'all') {
      filtered = filtered.filter(t => t.status === statusFilter);
    }
    if (driverFilter !== 'all') {
      filtered = filtered.filter(t => t.driverId === parseInt(driverFilter));
    }
    if (vehicleFilter !== 'all') {
      filtered = filtered.filter(t => t.vehicleId === parseInt(vehicleFilter));
    }

    setFilteredTrips(filtered);
  };

  const openAssignModal = () => {
    setFormData({
      driverId: '',
      vehicleId: '',
      origin: '',
      destination: '',
    });
    setFormError('');
    setShowAssignModal(true);
  };

  const handleAssignTrip = async () => {
    setFormError('');
    setFormLoading(true);

    try {
      await api.createTrip({
        driverId: parseInt(formData.driverId),
        vehicleId: parseInt(formData.vehicleId),
        origin: formData.origin,
        destination: formData.destination,
      });
      await loadData();
      setShowAssignModal(false);
    } catch (error) {
      setFormError(error instanceof Error ? error.message : 'Failed to assign trip');
    } finally {
      setFormLoading(false);
    }
  };

  const handleUpdateStatus = async (newStatus: TripStatus) => {
    if (!updateStatusTrip) return;

    setFormLoading(true);
    try {
      await api.updateTripStatus(updateStatusTrip.id, newStatus);
      await loadData();
      setUpdateStatusTrip(null);
    } catch (error) {
      console.error('Failed to update trip status:', error);
    } finally {
      setFormLoading(false);
    }
  };

  const getDriverName = (driverId: number) => drivers.find(d => d.id === driverId)?.fullName || 'Unknown';
  const getVehicleInfo = (vehicleId: number) => {
    const vehicle = vehicles.find(v => v.id === vehicleId);
    return vehicle ? `${vehicle.name} (${vehicle.plateNumber})` : 'Unknown';
  };

  const availableDrivers = drivers.filter(d => d.status === 'AVAILABLE');
  const availableVehicles = vehicles.filter(v => v.status === 'AVAILABLE');

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-[#1E293B]">Trips</h1>
          <p className="text-[#64748B]">Manage and assign fleet trips</p>
        </div>
        <Button onClick={openAssignModal}>
          <Plus className="h-5 w-5 mr-2" />
          Assign Trip
        </Button>
      </div>

      {/* Filters */}
      <div className="bg-white rounded-lg shadow-sm border border-[#E2E8F0] p-4">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div className="flex gap-2 md:col-span-4 overflow-x-auto pb-2">
            {['all', 'SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'].map((status) => (
              <button
                key={status}
                onClick={() => setStatusFilter(status)}
                className={`px-4 py-2 rounded-lg text-sm font-medium whitespace-nowrap transition-colors ${
                  statusFilter === status
                    ? 'bg-[#3B82F6] text-white'
                    : 'bg-[#F1F5F9] text-[#64748B] hover:bg-[#E2E8F0]'
                }`}
              >
                {status === 'all' ? 'All' : status.replace('_', ' ')}
              </button>
            ))}
          </div>
          <Select
            options={[
              { value: 'all', label: 'All Drivers' },
              ...drivers.map(d => ({ value: d.id.toString(), label: d.fullName })),
            ]}
            value={driverFilter}
            onChange={(e) => setDriverFilter(e.target.value)}
          />
          <Select
            options={[
              { value: 'all', label: 'All Vehicles' },
              ...vehicles.map(v => ({ value: v.id.toString(), label: v.name })),
            ]}
            value={vehicleFilter}
            onChange={(e) => setVehicleFilter(e.target.value)}
          />
        </div>
      </div>

      {/* Table */}
      <div className="bg-white rounded-lg shadow-sm border border-[#E2E8F0] overflow-hidden">
        {filteredTrips.length === 0 ? (
          <EmptyState
            icon={<MapPinIcon className="h-8 w-8 text-[#64748B]" />}
            title="No trips found"
            description="No trips match your filters. Try adjusting your selection."
          />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-[#F8FAFC] border-b border-[#E2E8F0]">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#64748B] uppercase tracking-wider">Trip ID</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#64748B] uppercase tracking-wider">Driver</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#64748B] uppercase tracking-wider">Vehicle</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#64748B] uppercase tracking-wider">Route</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#64748B] uppercase tracking-wider">Status</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#64748B] uppercase tracking-wider">Start Time</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#64748B] uppercase tracking-wider">End Time</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#64748B] uppercase tracking-wider">Created</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#64748B] uppercase tracking-wider">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#E2E8F0]">
                {filteredTrips.map((trip) => (
                  <tr key={trip.id} className="hover:bg-[#F8FAFC] transition-colors">
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-[#1E293B]">#{trip.id}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-[#64748B]">{getDriverName(trip.driverId)}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-[#64748B]">{getVehicleInfo(trip.vehicleId)}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-[#64748B]">
                      <div className="flex items-center gap-2">
                        <span>{trip.origin}</span>
                        <ArrowRight className="h-4 w-4 text-[#94A3B8]" />
                        <span>{trip.destination}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <StatusBadge status={trip.status} showPulse={trip.status === 'IN_PROGRESS'} />
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-[#64748B]">
                      {trip.startTime ? new Date(trip.startTime).toLocaleString() : '—'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-[#64748B]">
                      {trip.endTime ? new Date(trip.endTime).toLocaleString() : '—'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-[#64748B]">
                      {formatDistanceToNow(new Date(trip.createdAt), { addSuffix: true })}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                      {(trip.status === 'SCHEDULED' || trip.status === 'IN_PROGRESS') && (
                        <Button size="sm" variant="secondary" onClick={() => setUpdateStatusTrip(trip)}>
                          Update Status
                        </Button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Assign Trip Modal */}
      <Modal
        isOpen={showAssignModal}
        onClose={() => setShowAssignModal(false)}
        title="Assign Trip"
        footer={
          <div className="flex gap-3 justify-end">
            <Button variant="secondary" onClick={() => setShowAssignModal(false)} disabled={formLoading}>
              Cancel
            </Button>
            <Button onClick={handleAssignTrip} loading={formLoading}>
              Assign Trip
            </Button>
          </div>
        }
      >
        <div className="space-y-4">
          {formError && <Alert variant="error">{formError}</Alert>}
          
          {availableDrivers.length === 0 && (
            <Alert variant="warning">No available drivers. All drivers are currently on trips.</Alert>
          )}
          
          {availableVehicles.length === 0 && (
            <Alert variant="warning">No available vehicles. All vehicles are currently on trips.</Alert>
          )}

          <Select
            label="Driver"
            value={formData.driverId}
            onChange={(e) => setFormData({ ...formData, driverId: e.target.value })}
            options={[
              { value: '', label: 'Select a driver' },
              ...availableDrivers.map(d => ({ value: d.id.toString(), label: d.fullName })),
            ]}
            required
          />

          <Select
            label="Vehicle"
            value={formData.vehicleId}
            onChange={(e) => setFormData({ ...formData, vehicleId: e.target.value })}
            options={[
              { value: '', label: 'Select a vehicle' },
              ...availableVehicles.map(v => ({ value: v.id.toString(), label: `${v.name} (${v.plateNumber})` })),
            ]}
            required
          />

          <Input
            label="Origin"
            value={formData.origin}
            onChange={(e) => setFormData({ ...formData, origin: e.target.value })}
            placeholder="e.g., Lahore"
            required
          />

          <Input
            label="Destination"
            value={formData.destination}
            onChange={(e) => setFormData({ ...formData, destination: e.target.value })}
            placeholder="e.g., Karachi"
            required
          />
        </div>
      </Modal>

      {/* Update Status Modal */}
      {updateStatusTrip && (
        <Modal
          isOpen={!!updateStatusTrip}
          onClose={() => setUpdateStatusTrip(null)}
          title="Update Trip Status"
          size="sm"
        >
          <div className="space-y-4">
            <div className="p-4 bg-[#F8FAFC] rounded-lg">
              <p className="text-sm text-[#64748B] mb-1">Current Status</p>
              <StatusBadge status={updateStatusTrip.status} />
            </div>

            <div className="p-4 bg-blue-50 border border-blue-200 rounded-lg">
              <p className="text-xs font-medium text-blue-900 mb-2">Trip Status Lifecycle</p>
              <div className="text-xs text-blue-700">
                SCHEDULED → IN_PROGRESS → COMPLETED<br/>
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;↘ CANCELLED
              </div>
            </div>

            <div className="space-y-3">
              {updateStatusTrip.status === 'SCHEDULED' && (
                <>
                  <Button
                    className="w-full"
                    onClick={() => handleUpdateStatus('IN_PROGRESS')}
                    loading={formLoading}
                  >
                    Start Trip
                  </Button>
                  <p className="text-xs text-[#64748B]">Starting this trip will mark the driver and vehicle as On Trip.</p>
                  <Button
                    variant="danger"
                    className="w-full"
                    onClick={() => handleUpdateStatus('CANCELLED')}
                    loading={formLoading}
                  >
                    Cancel Trip
                  </Button>
                </>
              )}

              {updateStatusTrip.status === 'IN_PROGRESS' && (
                <>
                  <Button
                    className="w-full"
                    onClick={() => handleUpdateStatus('COMPLETED')}
                    loading={formLoading}
                  >
                    Complete Trip
                  </Button>
                  <p className="text-xs text-[#64748B]">Completing this trip will free up the driver and vehicle.</p>
                  <Button
                    variant="danger"
                    className="w-full"
                    onClick={() => handleUpdateStatus('CANCELLED')}
                    loading={formLoading}
                  >
                    Cancel Trip
                  </Button>
                </>
              )}
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
}
