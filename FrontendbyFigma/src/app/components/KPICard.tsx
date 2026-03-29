import { ReactNode } from 'react';
import { LucideIcon } from 'lucide-react';

interface KPICardProps {
  icon: LucideIcon;
  label: string;
  value: number | string;
  subtext?: string;
  iconBg?: string;
  iconColor?: string;
}

export function KPICard({ icon: Icon, label, value, subtext, iconBg = 'bg-blue-100', iconColor = 'text-blue-600' }: KPICardProps) {
  return (
    <div className="bg-white rounded-lg p-6 shadow-sm border border-[#E2E8F0]">
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <p className="text-sm text-[#64748B] mb-1">{label}</p>
          <p className="text-3xl font-semibold text-[#1E293B] mb-2">{value}</p>
          {subtext && (
            <p className="text-sm text-[#64748B]">{subtext}</p>
          )}
        </div>
        <div className={`${iconBg} ${iconColor} p-3 rounded-lg`}>
          <Icon className="h-6 w-6" />
        </div>
      </div>
    </div>
  );
}
