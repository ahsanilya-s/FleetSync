import { TripStatus, VehicleStatus } from '../services/mockApi';

interface StatusBadgeProps {
  status: TripStatus | VehicleStatus;
  showPulse?: boolean;
}

export function StatusBadge({ status, showPulse }: StatusBadgeProps) {
  const getStatusStyles = () => {
    switch (status) {
      case 'AVAILABLE':
        return 'bg-emerald-100 text-emerald-700 border-emerald-200';
      case 'ON_TRIP':
        return 'bg-amber-100 text-amber-700 border-amber-200';
      case 'SCHEDULED':
        return 'bg-blue-100 text-blue-700 border-blue-200';
      case 'IN_PROGRESS':
        return 'bg-amber-100 text-amber-700 border-amber-200';
      case 'COMPLETED':
        return 'bg-emerald-100 text-emerald-700 border-emerald-200';
      case 'CANCELLED':
        return 'bg-gray-100 text-gray-700 border-gray-200';
      default:
        return 'bg-gray-100 text-gray-700 border-gray-200';
    }
  };

  const getStatusLabel = () => {
    switch (status) {
      case 'ON_TRIP':
        return 'On Trip';
      case 'IN_PROGRESS':
        return 'In Progress';
      default:
        return status.charAt(0) + status.slice(1).toLowerCase();
    }
  };

  return (
    <span
      className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium border ${getStatusStyles()}`}
    >
      {(status === 'IN_PROGRESS' || showPulse) && (
        <span className="relative flex h-2 w-2">
          <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-current opacity-75"></span>
          <span className="relative inline-flex rounded-full h-2 w-2 bg-current"></span>
        </span>
      )}
      {getStatusLabel()}
    </span>
  );
}
