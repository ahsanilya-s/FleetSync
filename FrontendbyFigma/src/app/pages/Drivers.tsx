import { useState, useEffect } from 'react';
import { Search, Plus, Users as UsersIcon } from 'lucide-react';
import { Button } from '../components/Button';
import { Input } from '../components/Input';
import { Select } from '../components/Select';
import { Modal } from '../components/Modal';
import { StatusBadge } from '../components/StatusBadge';
import { EmptyState } from '../components/EmptyState';
import { Alert } from '../components/Alert';
import { api, Driver } from '../services/mockApi';
import { formatDistanceToNow } from 'date-fns';

export function Drivers() {
  const [drivers, setDrivers] = useState<Driver[]>([]);
  const [filteredDrivers, setFilteredDrivers] = useState<Driver[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  
  // Modal states
  const [showDriverModal, setShowDriverModal] = useState(false);
  
  // Form states
  const [formData, setFormData] = useState({
    fullName: '',
    licenseNumber: '',
    phone: '',
    email: '',
  });
  const [formError, setFormError] = useState('');
  const [formLoading, setFormLoading] = useState(false);

  useEffect(() => {
    loadDrivers();
  }, []);

  useEffect(() => {
    filterDrivers();
  }, [drivers, searchQuery, statusFilter]);

  const loadDrivers = async () => {
    try {
      const data = await api.getDrivers();
      setDrivers(data);
    } catch (error) {
      console.error('Failed to load drivers:', error);
    } finally {
      setLoading(false);
    }
  };

  const filterDrivers = () => {
    let filtered = [...drivers];

    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(
        d => d.fullName.toLowerCase().includes(query) || d.licenseNumber.toLowerCase().includes(query)
      );
    }

    if (statusFilter !== 'all') {
      filtered = filtered.filter(d => d.status === statusFilter);
    }

    setFilteredDrivers(filtered);
  };

  const openAddModal = () => {
    setFormData({
      fullName: '',
      licenseNumber: '',
      phone: '',
      email: '',
    });
    setFormError('');
    setShowDriverModal(true);
  };

  const handleSubmit = async () => {
    setFormError('');
    setFormLoading(true);

    try {
      await api.createDriver(formData);
      await loadDrivers();
      setShowDriverModal(false);
    } catch (error) {
      setFormError(error instanceof Error ? error.message : 'Operation failed');
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
            Drivers <span className="text-[#64748B]">({drivers.length})</span>
          </h1>
          <p className="text-[#64748B]">Manage your fleet drivers</p>
        </div>
        <Button onClick={openAddModal}>
          <Plus className="h-5 w-5 mr-2" />
          Register Driver
        </Button>
      </div>

      {/* Filters */}
      <div className="bg-white rounded-lg shadow-sm border border-[#E2E8F0] p-4">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="md:col-span-2 relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-[#94A3B8]" />
            <input
              type="text"
              placeholder="Search by name or license..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-[#E2E8F0] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#3B82F6]"
            />
          </div>
          <Select
            options={[
              { value: 'all', label: 'All Drivers' },
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
        {filteredDrivers.length === 0 ? (
          <EmptyState
            icon={<UsersIcon className="h-8 w-8 text-[#64748B]" />}
            title="No drivers found"
            description={searchQuery || statusFilter !== 'all' ? 'Try adjusting your filters' : 'No drivers registered yet. Add your first driver.'}
            action={!searchQuery && statusFilter === 'all' ? {
              label: 'Register Driver',
              onClick: openAddModal,
            } : undefined}
          />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-[#F8FAFC] border-b border-[#E2E8F0]">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#64748B] uppercase tracking-wider">ID</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#64748B] uppercase tracking-wider">Full Name</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#64748B] uppercase tracking-wider">License Number</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#64748B] uppercase tracking-wider">Phone</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#64748B] uppercase tracking-wider">Email</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#64748B] uppercase tracking-wider">Status</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#64748B] uppercase tracking-wider">Registered</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#E2E8F0]">
                {filteredDrivers.map((driver) => (
                  <tr key={driver.id} className="hover:bg-[#F8FAFC] transition-colors">
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-[#1E293B]">{driver.id}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-[#1E293B]">{driver.fullName}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-[#64748B] font-mono">{driver.licenseNumber}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-[#64748B]">{driver.phone}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-[#64748B]">{driver.email}</td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <StatusBadge status={driver.status} />
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-[#64748B]">
                      {formatDistanceToNow(new Date(driver.createdAt), { addSuffix: true })}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Add Driver Modal */}
      <Modal
        isOpen={showDriverModal}
        onClose={() => setShowDriverModal(false)}
        title="Register Driver"
        footer={
          <div className="flex flex-col gap-3">
            <div className="flex gap-3 justify-end">
              <Button variant="secondary" onClick={() => setShowDriverModal(false)} disabled={formLoading}>
                Cancel
              </Button>
              <Button onClick={handleSubmit} loading={formLoading}>
                Register Driver
              </Button>
            </div>
            <p className="text-xs text-[#64748B]">
              Note: Driver accounts are separate from login accounts. This registers a driver profile for trip assignment.
            </p>
          </div>
        }
      >
        <div className="space-y-4">
          {formError && <Alert variant="error">{formError}</Alert>}
          
          <Input
            label="Full Name"
            value={formData.fullName}
            onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
            placeholder="e.g., Ali Khan"
            required
          />

          <Input
            label="License Number"
            value={formData.licenseNumber}
            onChange={(e) => setFormData({ ...formData, licenseNumber: e.target.value })}
            placeholder="e.g., LIC-001"
            required
          />

          <Input
            label="Phone Number"
            type="tel"
            value={formData.phone}
            onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
            placeholder="e.g., 0300-1234567"
            required
          />

          <Input
            label="Email"
            type="email"
            value={formData.email}
            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
            placeholder="e.g., driver@fleetsync.com"
            required
          />
        </div>
      </Modal>
    </div>
  );
}
