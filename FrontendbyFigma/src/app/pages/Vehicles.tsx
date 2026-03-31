import { useState, useEffect } from 'react';
import { Search, Plus, Pencil, Trash2, Truck as TruckIcon } from 'lucide-react';
import { Button } from '../components/Button';
import { Input } from '../components/Input';
import { Select } from '../components/Select';
import { Modal } from '../components/Modal';
import { StatusBadge } from '../components/StatusBadge';
import { EmptyState } from '../components/EmptyState';
import { ConfirmDialog } from '../components/ConfirmDialog';
import { Alert } from '../components/Alert';
import { api, Vehicle, VehicleType, FuelType } from '../services/mockApi';
import { formatDistanceToNow } from 'date-fns';

export function Vehicles() {
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [filteredVehicles, setFilteredVehicles] = useState<Vehicle[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  
  // Modal states
  const [showVehicleModal, setShowVehicleModal] = useState(false);
  const [editingVehicle, setEditingVehicle] = useState<Vehicle | null>(null);
  const [deleteVehicle, setDeleteVehicle] = useState<Vehicle | null>(null);
  
  // Form states
  const [formData, setFormData] = useState({
    name: '',
    type: 'Mini Van' as VehicleType,
    plateNumber: '',
    capacity: '',
    fuelType: 'DIESEL' as FuelType,
  });
  const [formError, setFormError] = useState('');
  const [formLoading, setFormLoading] = useState(false);

  useEffect(() => {
    loadVehicles();
  }, []);

  useEffect(() => {
    filterVehicles();
  }, [vehicles, searchQuery, statusFilter]);

  const loadVehicles = async () => {
    try {
      const data = await api.getVehicles();
      setVehicles(data);
    } catch (error) {
      console.error('Failed to load vehicles:', error);
    } finally {
      setLoading(false);
    }
  };

  const filterVehicles = () => {
    let filtered = [...vehicles];

    // Search filter
    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(
        v => v.name.toLowerCase().includes(query) || v.plateNumber.toLowerCase().includes(query)
      );
    }

    // Status filter
    if (statusFilter !== 'all') {
      filtered = filtered.filter(v => v.status === statusFilter);
    }

    setFilteredVehicles(filtered);
  };

  const openAddModal = () => {
    setEditingVehicle(null);
    setFormData({
      name: '',
      type: 'Mini Van',
      plateNumber: '',
      capacity: '',
      fuelType: 'DIESEL',
    });
    setFormError('');
    setShowVehicleModal(true);
  };

  const openEditModal = (vehicle: Vehicle) => {
    setEditingVehicle(vehicle);
    setFormData({
      name: vehicle.name,
      type: vehicle.type,
      plateNumber: vehicle.plateNumber,
      capacity: vehicle.capacity.toString(),
      fuelType: vehicle.fuelType,
    });
    setFormError('');
    setShowVehicleModal(true);
  };

  const handleSubmit = async () => {
    setFormError('');
    setFormLoading(true);

    try {
      const vehicleData = {
        name: formData.name,
        type: formData.type,
        plateNumber: formData.plateNumber,
        capacity: parseInt(formData.capacity),
        fuelType: formData.fuelType,
      };

      if (editingVehicle) {
        await api.updateVehicle(editingVehicle.id, vehicleData);
      } else {
        await api.createVehicle(vehicleData);
      }

      await loadVehicles();
      setShowVehicleModal(false);
    } catch (error) {
      setFormError(error instanceof Error ? error.message : 'Operation failed');
    } finally {
      setFormLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteVehicle) return;

    setFormLoading(true);
    try {
      await api.deleteVehicle(deleteVehicle.id);
      await loadVehicles();
      setDeleteVehicle(null);
    } catch (error) {
      console.error('Failed to delete vehicle:', error);
    } finally {
      setFormLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-[#1E293B]">
            Vehicles <span className="text-[#64748B]">({vehicles.length})</span>
          </h1>
          <p className="text-[#64748B]">Manage your fleet vehicles</p>
        </div>
        <Button onClick={openAddModal}>
          <Plus className="h-5 w-5 mr-2" />
          Add Vehicle
        </Button>
      </div>

      {/* Filters */}
      <div className="bg-white rounded-lg shadow-sm border border-[#E2E8F0] p-4">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="md:col-span-2 relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-[#94A3B8]" />
            <input
              type="text"
              placeholder="Search by name or plate number..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-[#E2E8F0] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#3B82F6]"
            />
          </div>
          <Select
            options={[
              { value: 'all', label: 'All Vehicles' },
              { value: 'AVAILABLE', label: 'Available' },
              { value: 'ON_TRIP', label: 'On Trip' },
            ]}
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
          />
        </div>
      </div>

      {/* Table */}
      <div className="bg-white rounded-lg shadow-sm border border-[#E2E8F0] overflow-hidden">
        {filteredVehicles.length === 0 ? (
          <EmptyState
            icon={<TruckIcon className="h-8 w-8 text-[#64748B]" />}
            title="No vehicles found"
            description={searchQuery || statusFilter !== 'all' ? 'Try adjusting your filters' : 'No vehicles registered yet. Add your first vehicle.'}
            action={!searchQuery && statusFilter === 'all' ? {
              label: 'Add Vehicle',
              onClick: openAddModal,
            } : undefined}
          />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-[#F8FAFC] border-b border-[#E2E8F0]">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#64748B] uppercase tracking-wider">ID</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#64748B] uppercase tracking-wider">Name</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#64748B] uppercase tracking-wider">Type</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#64748B] uppercase tracking-wider">Plate Number</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#64748B] uppercase tracking-wider">Capacity</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#64748B] uppercase tracking-wider">Fuel Type</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#64748B] uppercase tracking-wider">Status</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#64748B] uppercase tracking-wider">Created</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#64748B] uppercase tracking-wider">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#E2E8F0]">
                {filteredVehicles.map((vehicle) => (
                  <tr key={vehicle.id} className="hover:bg-[#F8FAFC] transition-colors">
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-[#1E293B]">{vehicle.id}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-[#1E293B]">{vehicle.name}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-[#64748B]">{vehicle.type}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-[#64748B] font-mono">{vehicle.plateNumber}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-[#64748B]">{vehicle.capacity} kg</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-[#64748B]">{vehicle.fuelType}</td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <StatusBadge status={vehicle.status} />
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-[#64748B]">
                      {formatDistanceToNow(new Date(vehicle.createdAt), { addSuffix: true })}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => openEditModal(vehicle)}
                          className="p-2 text-[#64748B] hover:text-[#3B82F6] hover:bg-blue-50 rounded transition-colors"
                        >
                          <Pencil className="h-4 w-4" />
                        </button>
                        <button
                          onClick={() => setDeleteVehicle(vehicle)}
                          className="p-2 text-[#64748B] hover:text-[#EF4444] hover:bg-red-50 rounded transition-colors"
                        >
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Add/Edit Modal */}
      <Modal
        isOpen={showVehicleModal}
        onClose={() => setShowVehicleModal(false)}
        title={editingVehicle ? 'Edit Vehicle' : 'Add New Vehicle'}
        footer={
          <div className="flex gap-3 justify-end">
            <Button variant="secondary" onClick={() => setShowVehicleModal(false)} disabled={formLoading}>
              Cancel
            </Button>
            <Button onClick={handleSubmit} loading={formLoading}>
              {editingVehicle ? 'Save Changes' : 'Add Vehicle'}
            </Button>
          </div>
        }
      >
        <div className="space-y-4">
          {formError && <Alert variant="error">{formError}</Alert>}
          
          <Input
            label="Vehicle Name"
            value={formData.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            placeholder="e.g., Van A"
            required
          />

          <Select
            label="Type"
            value={formData.type}
            onChange={(e) => setFormData({ ...formData, type: e.target.value as VehicleType })}
            options={[
              { value: 'Mini Van', label: 'Mini Van' },
              { value: 'Cargo Van', label: 'Cargo Van' },
              { value: 'Pickup Truck', label: 'Pickup Truck' },
              { value: 'Box Truck', label: 'Box Truck' },
            ]}
          />

          <Input
            label="Plate Number"
            value={formData.plateNumber}
            onChange={(e) => setFormData({ ...formData, plateNumber: e.target.value })}
            placeholder="e.g., ABC-123"
            required
          />

          <Input
            label="Capacity (kg)"
            type="number"
            value={formData.capacity}
            onChange={(e) => setFormData({ ...formData, capacity: e.target.value })}
            placeholder="e.g., 1000"
            min="1"
            required
          />

          <Select
            label="Fuel Type"
            value={formData.fuelType}
            onChange={(e) => setFormData({ ...formData, fuelType: e.target.value as FuelType })}
            options={[
              { value: 'DIESEL', label: 'Diesel' },
              { value: 'PETROL', label: 'Petrol' },
              { value: 'ELECTRIC', label: 'Electric' },
              { value: 'HYBRID', label: 'Hybrid' },
              { value: 'UNKNOWN', label: 'Unknown' },
            ]}
          />
        </div>
      </Modal>

      {/* Delete Confirmation */}
      <ConfirmDialog
        isOpen={!!deleteVehicle}
        onClose={() => setDeleteVehicle(null)}
        onConfirm={handleDelete}
        title={`Delete ${deleteVehicle?.name}?`}
        message="Are you sure you want to delete this vehicle?"
        warning="This will also delete all trips and maintenance records for this vehicle."
        confirmLabel="Delete"
        loading={formLoading}
      />
    </div>
  );
}
