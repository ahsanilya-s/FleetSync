import { ReactNode } from 'react';
import { PackageOpen } from 'lucide-react';
import { Button } from './Button';

interface EmptyStateProps {
  icon?: ReactNode;
  title: string;
  description: string;
  action?: {
    label: string;
    onClick: () => void;
  };
}

export function EmptyState({ icon, title, description, action }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-12 px-4">
      <div className="w-16 h-16 bg-[#F1F5F9] rounded-full flex items-center justify-center mb-4">
        {icon || <PackageOpen className="h-8 w-8 text-[#64748B]" />}
      </div>
      <h3 className="text-lg font-semibold text-[#1E293B] mb-2">{title}</h3>
      <p className="text-sm text-[#64748B] text-center max-w-md mb-6">{description}</p>
      {action && (
        <Button onClick={action.onClick}>
          {action.label}
        </Button>
      )}
    </div>
  );
}
