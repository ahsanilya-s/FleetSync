import { useState, useEffect } from 'react';
import { Plus, Wrench } from 'lucide-react';
import { Button } from '../components/Button';
import { Select } from '../components/Select';
import { Input } from '../components/Input';
import { Modal } from '../components/Modal';
import { Alert } from '../components/Alert';
import { EmptyState } from '../components/EmptyState';
import { api, MaintenanceRecord, MaintenanceType, Vehicle } from '../services/mockApi';

export function Maintenance() {
  const [maintenance, setMaintenance] = useState<MaintenanceRecord[]>([]);
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [activeTab, setActiveTab] = useState<'history' | 'alerts'>('history');
  const [selectedVehicle, setSelectedVehicle] = useState('all');
  const [showLogModal, setShowLogModal] = useState(false);
  
  const [formData, setFormData] = useState({
    vehicleId: '',
    date: new Date().toISOString().split('T')[0],
    type: 'OIL_CHANGE' as MaintenanceType,
    description: '',
    cost: '',
    nextServiceDate: '',
    nextServiceMileage: '',
  });
  const [formError, setFormError] = useState('');
  const [formLoading, setFormLoading] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const vehiclesData = await api.getVehicles();
      setVehicles(vehiclesData);

      // Fetch maintenance history for every vehicle and merge
      const allRecords = await Promise.all(
        vehiclesData.map(v => api.getMaintenance(v.id))
      );
      setMaintenance(allRecords.flat());
    } catch (error) {
      console.error('Failed to load data:', error);
    }
  };

  const getVehicleName = (vehicleId: number) => {
    return vehicles.find(v => v.id === vehicleId)?.name || 'Unknown';
  };

  const filteredMaintenance = selectedVehicle === 'all'
    ? maintenance
    : maintenance.filter(m => m.vehicleId === parseInt(selectedVehicle));

  // Use real alert/upcoming endpoints for the Alerts tab
  const [alertRecords, setAlertRecords] = useState<MaintenanceRecord[]>([]);

  useEffect(() => {
    if (activeTab === 'alerts') {
      api.getMaintenanceAlerts(30).then(setAlertRecords).catch(console.error);
    }
  }, [activeTab]);

  const alerts = alertRecords;

  const getDaysUntilService = (date: string) => {
    return Math.ceil((new Date(date).getTime() - new Date().getTime()) / (1000 * 60 * 60 * 24));
  };

  const openLogModal = (vehicleId?: number) => {
    setFormData({
      vehicleId: vehicleId?.toString() || '',
      date: new Date().toISOString().split('T')[0],
      type: 'OIL_CHANGE',
      description: '',
      cost: '',
      nextServiceDate: '',
      nextServiceMileage: '',
    });
    setFormError('');
    setShowLogModal(true);
  };

  const handleSubmit = async () => {
    setFormError('');
    setFormLoading(true);

    try {
      await api.createMaintenance({
        vehicleId: parseInt(formData.vehicleId),
        date: formData.date,
        type: formData.type,
        description: formData.description,
        cost: parseFloat(formData.cost) || 0,
        nextServiceDate: formData.nextServiceDate || null,
        nextServiceMileage: formData.nextServiceMileage ? parseInt(formData.nextServiceMileage) : null,
      });
      await loadData();
      setShowLogModal(false);
    } catch (error) {
      setFormError(error instanceof Error ? error.message : 'Failed to log maintenance');
    } finally {
      setFormLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-[#1E293B]">Maintenance</h1>
          <p className="text-[#64748B]">Track vehicle maintenance and service alerts</p>
        </div>
        <Button onClick={() => openLogModal()}>
          <Plus className="h-5 w-5 mr-2" />
          Log Maintenance
        </Button>
      </div>

      {/* Tabs */}
      <div className="bg-white rounded-lg shadow-sm border border-[#E2E8F0]">
        <div className="border-b border-[#E2E8F0]">
          <div className="flex">
            <button
              onClick={() => setActiveTab('history')}
              className={`px-6 py-3 font-medium text-sm border-b-2 transition-colors ${
                activeTab === 'history'
                  ? 'border-[#3B82F6] text-[#3B82F6]'
                  : 'border-transparent text-[#64748B] hover:text-[#1E293B]'
              }`}
            >
              History
            </button>
            <button
              onClick={() => setActiveTab('alerts')}
              className={`px-6 py-3 font-medium text-sm border-b-2 transition-colors ${
                activeTab === 'alerts'
                  ? 'border-[#3B82F6] text-[#3B82F6]'
                  : 'border-transparent text-[#64748B] hover:text-[#1E293B]'
              }`}
            >
              Alerts & Upcoming
              {alerts.length > 0 && (
                <span className="ml-2 px-2 py-0.5 bg-red-100 text-red-700 text-xs rounded-full">
                  {alerts.length}
                </span>
              )}
            </button>
          </div>
        </div>

        <div className="p-6">
          {activeTab === 'history' ? (
            <div className="space-y-4">
              <Select
                options={[
                  { value: 'all', label: 'All Vehicles' },
                  ...vehicles.map(v => ({ value: v.id.toString(), label: v.name })),
                ]}
                value={selectedVehicle}
                onChange={(e) => setSelectedVehicle(e.target.value)}
              />

              {filteredMaintenance.length === 0 ? (
                <EmptyState
                  icon={<Wrench className="h-8 w-8 text-[#64748B]" />}
                  title="No maintenance records"
                  description="No maintenance has been logged yet."
                  action={{
                    label: 'Log Maintenance',
                    onClick: () => openLogModal(),
                  }}
                />
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full">
                    <thead className="bg-[#F8FAFC] border-b border-[#E2E8F0]">
                      <tr>
                        <th className="px-4 py-3 text-left text-xs font-medium text-[#64748B] uppercase">ID</th>
                        <th className="px-4 py-3 text-left text-xs font-medium text-[#64748B] uppercase">Vehicle</th>
                        <th className="px-4 py-3 text-left text-xs font-medium text-[#64748B] uppercase">Date</th>
                        <th className="px-4 py-3 text-left text-xs font-medium text-[#64748B] uppercase">Type</th>
                        <th className="px-4 py-3 text-left text-xs font-medium text-[#64748B] uppercase">Description</th>
                        <th className="px-4 py-3 text-left text-xs font-medium text-[#64748B] uppercase">Cost (PKR)</th>
                        <th className="px-4 py-3 text-left text-xs font-medium text-[#64748B] uppercase">Next Service</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-[#E2E8F0]">
                      {filteredMaintenance.map((record) => (
                        <tr key={record.id} className="hover:bg-[#F8FAFC]">
                          <td className="px-4 py-3 text-sm text-[#1E293B]">{record.id}</td>
                          <td className="px-4 py-3 text-sm text-[#64748B]">{getVehicleName(record.vehicleId)}</td>
                          <td className="px-4 py-3 text-sm text-[#64748B]">{record.date ? new Date(record.date).toLocaleDateString() : '—'}</td>
                          <td className="px-4 py-3 text-sm text-[#64748B]">{record.type.replace('_', ' ')}</td>
                          <td className="px-4 py-3 text-sm text-[#64748B]">{record.description || '—'}</td>
                          <td className="px-4 py-3 text-sm text-[#64748B]">{record.cost.toLocaleString()}</td>
                          <td className="px-4 py-3 text-sm text-[#64748B]">
                            {record.nextServiceDate ? new Date(record.nextServiceDate).toLocaleDateString() : '—'}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          ) : (
            <div className="space-y-6">
              {/* Overdue / Due Soon */}
              <div>
                <h3 className="text-sm font-semibold text-[#1E293B] mb-3">Overdue / Due Soon (≤7 days)</h3>
                <div className="grid gap-3">
                  {alerts.filter(a => {
                    const days = getDaysUntilService(a.nextServiceDate!);
                    return days <= 7;
                  }).length === 0 ? (
                    <p className="text-sm text-[#64748B] py-4">No urgent alerts</p>
                  ) : (
                    alerts
                      .filter(a => {
                        const days = getDaysUntilService(a.nextServiceDate!);
                        return days <= 7;
                      })
                      .map((alert) => {
                        const days = getDaysUntilService(alert.nextServiceDate!);
                        const isOverdue = days < 0;
                        return (
                          <div
                            key={alert.id}
                            className={`p-4 rounded-lg border ${
                              isOverdue ? 'bg-red-50 border-red-200' : 'bg-amber-50 border-amber-200'
                            }`}
                          >
                            <div className="flex items-start justify-between">
                              <div className="flex-1">
                                <div className="flex items-center gap-2 mb-1">
                                  <h4 className="font-medium text-[#1E293B]">{getVehicleName(alert.vehicleId)}</h4>
                                  <span
                                    className={`text-xs px-2 py-0.5 rounded-full ${
                                      isOverdue
                                        ? 'bg-red-100 text-red-700'
                                        : 'bg-amber-100 text-amber-700'
                                    }`}
                                  >
                                    {isOverdue ? `Overdue ${Math.abs(days)} days` : `Due in ${days} days`}
                                  </span>
                                </div>
                                <p className="text-sm text-[#64748B]">
                                  {alert.type.replace('_', ' ')} - Due: {new Date(alert.nextServiceDate!).toLocaleDateString()}
                                </p>
                              </div>
                              <Button size="sm" onClick={() => openLogModal(alert.vehicleId)}>
                                Log Service
                              </Button>
                            </div>
                          </div>
                        );
                      })
                  )}
                </div>
              </div>

              {/* Upcoming (8-30 days) */}
              <div>
                <h3 className="text-sm font-semibold text-[#1E293B] mb-3">Upcoming (next 30 days)</h3>
                <div className="space-y-2">
                  {alerts.filter(a => {
                    const days = getDaysUntilService(a.nextServiceDate!);
                    return days > 7 && days <= 30;
                  }).length === 0 ? (
                    <p className="text-sm text-[#64748B] py-4">No upcoming maintenance</p>
                  ) : (
                    alerts
                      .filter(a => {
                        const days = getDaysUntilService(a.nextServiceDate!);
                        return days > 7 && days <= 30;
                      })
                      .map((alert) => {
                        const days = getDaysUntilService(alert.nextServiceDate!);
                        return (
                          <div key={alert.id} className="flex items-center justify-between p-3 bg-[#F8FAFC] rounded-lg">
                            <div>
                              <p className="text-sm font-medium text-[#1E293B]">{getVehicleName(alert.vehicleId)}</p>
                              <p className="text-xs text-[#64748B]">
                                {alert.type.replace('_', ' ')} - {days} days remaining
                              </p>
                            </div>
                            <span className="text-xs text-[#64748B]">
                              {new Date(alert.nextServiceDate!).toLocaleDateString()}
                            </span>
                          </div>
                        );
                      })
                  )}
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Log Maintenance Modal */}
      <Modal
        isOpen={showLogModal}
        onClose={() => setShowLogModal(false)}
        title="Log Maintenance"
        footer={
          <div className="flex gap-3 justify-end">
            <Button variant="secondary" onClick={() => setShowLogModal(false)} disabled={formLoading}>
              Cancel
            </Button>
            <Button onClick={handleSubmit} loading={formLoading}>
              Log Record
            </Button>
          </div>
        }
      >
        <div className="space-y-4">
          {formError && <Alert variant="error">{formError}</Alert>}

          <Select
            label="Vehicle"
            value={formData.vehicleId}
            onChange={(e) => setFormData({ ...formData, vehicleId: e.target.value })}
            options={[
              { value: '', label: 'Select a vehicle' },
              ...vehicles.map(v => ({ value: v.id.toString(), label: v.name })),
            ]}
            required
          />

          <Input
            label="Date"
            type="date"
            value={formData.date}
            onChange={(e) => setFormData({ ...formData, date: e.target.value })}
            required
          />

          <Select
            label="Type"
            value={formData.type}
            onChange={(e) => setFormData({ ...formData, type: e.target.value as MaintenanceType })}
            options={[
              { value: 'OIL_CHANGE', label: 'Oil Change' },
              { value: 'TIRE_ROTATION', label: 'Tire Rotation' },
              { value: 'BRAKE_INSPECTION', label: 'Brake Inspection' },
              { value: 'ENGINE_CHECK', label: 'Engine Check' },
              { value: 'BATTERY_CHECK', label: 'Battery Check' },
              { value: 'GENERAL_SERVICE', label: 'General Service' },
              { value: 'OTHER', label: 'Other' },
            ]}
          />

          <div>
            <label className="block text-sm font-medium text-[#1E293B] mb-1.5">Description (Optional)</label>
            <textarea
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              placeholder="Additional notes..."
              className="w-full px-3 py-2 border border-[#E2E8F0] rounded-lg focus:outline-none focus:ring-2 focus:ring-[#3B82F6] resize-none"
              rows={3}
            />
          </div>

          <Input
            label="Cost (PKR)"
            type="number"
            value={formData.cost}
            onChange={(e) => setFormData({ ...formData, cost: e.target.value })}
            placeholder="0"
            min="0"
          />

          <Input
            label="Next Service Date (Optional)"
            type="date"
            value={formData.nextServiceDate}
            onChange={(e) => setFormData({ ...formData, nextServiceDate: e.target.value })}
          />

          <Input
            label="Next Service Mileage (Optional)"
            type="number"
            value={formData.nextServiceMileage}
            onChange={(e) => setFormData({ ...formData, nextServiceMileage: e.target.value })}
            placeholder="e.g., 50000"
            min="0"
          />
        </div>
      </Modal>
    </div>
  );
}
