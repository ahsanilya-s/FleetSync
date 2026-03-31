import { InputHTMLAttributes, forwardRef } from 'react';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  hint?: string;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, hint, className = '', ...props }, ref) => {
    return (
      <div className="w-full">
        {label && (
          <label className="block text-sm font-medium text-[#1E293B] mb-1.5">
            {label}
          </label>
        )}
        <input
          ref={ref}
          className={`w-full px-3 py-2 border rounded-lg bg-white text-[#1E293B] placeholder-[#94A3B8] focus:outline-none focus:ring-2 focus:ring-[#3B82F6] focus:border-transparent disabled:bg-[#F1F5F9] disabled:cursor-not-allowed ${
            error ? 'border-[#EF4444]' : 'border-[#E2E8F0]'
          } ${className}`}
          {...props}
        />
        {hint && !error && (
          <p className="mt-1 text-xs text-[#64748B]">{hint}</p>
        )}
        {error && (
          <p className="mt-1 text-xs text-[#EF4444]">{error}</p>
        )}
      </div>
    );
  }
);

Input.displayName = 'Input';
