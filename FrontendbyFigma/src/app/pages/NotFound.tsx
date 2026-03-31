import { useNavigate } from 'react-router';
import { Button } from '../components/Button';
import { Truck } from 'lucide-react';

export function NotFound() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#F8FAFC] to-[#E2E8F0] flex items-center justify-center p-4">
      <div className="text-center max-w-md">
        <div className="mb-8">
          <div className="inline-block relative">
            <h1 className="text-9xl font-bold text-[#0F172A] mb-4">404</h1>
            <Truck className="absolute -right-8 top-1/2 -translate-y-1/2 h-16 w-16 text-[#3B82F6] animate-bounce" />
          </div>
        </div>

        <h2 className="text-2xl font-semibold text-[#1E293B] mb-2">
          Looks like this route went off the map.
        </h2>
        <p className="text-[#64748B] mb-8">
          The page you're looking for doesn't exist or has been moved.
        </p>

        {/* Illustration */}
        <div className="mb-8">
          <svg
            className="w-64 h-32 mx-auto"
            viewBox="0 0 200 80"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            {/* Road */}
            <path
              d="M0 60 L150 60 L200 80"
              stroke="#94A3B8"
              strokeWidth="3"
              strokeDasharray="10 5"
            />
            {/* Cliff */}
            <path
              d="M150 60 L150 80"
              stroke="#64748B"
              strokeWidth="2"
            />
          </svg>
        </div>

        <Button onClick={() => navigate('/dashboard')}>
          Back to Dashboard
        </Button>
      </div>
    </div>
  );
}
