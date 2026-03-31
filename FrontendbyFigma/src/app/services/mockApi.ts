// Real API service — connects to Spring Boot backend at http://localhost:8080
// All requests use HTTP Basic Auth: Authorization: Basic <base64(username:password)>

export type VehicleStatus = 'AVAILABLE' | 'ON_TRIP';
export type FuelType = 'DIESEL' | 'PETROL' | 'ELECTRIC' | 'HYBRID' | 'UNKNOWN';
export type VehicleType = 'Mini Van' | 'Cargo Van' | 'Pickup Truck' | 'Box Truck';
export type TripStatus = 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
export type MaintenanceType = 'OIL_CHANGE' | 'TIRE_ROTATION' | 'BRAKE_INSPECTION' | 'ENGINE_CHECK' | 'BATTERY_CHECK' | 'GENERAL_SERVICE' | 'OTHER';

export interface Vehicle {
  id: number;
  name: string;
  type: VehicleType;
  plateNumber: string;
  capacity: number;
  fuelType: FuelType;
  status: VehicleStatus;
  createdAt: string;
}

export interface Driver {
  id: number;
  fullName: string;
  licenseNumber: string;
  phone: string;
  email: string;
  status: VehicleStatus;
  // backend returns createdAt — alias registeredAt for UI compatibility
  createdAt: string;
  registeredAt: string;
}

export interface Trip {
  id: number;
  driverId: number;
  vehicleId: number;
  origin: string;
  destination: string;
  status: TripStatus;
  startTime: string | null;
  endTime: string | null;
  createdAt: string;
}

export interface MaintenanceRecord {
  id: number;
  vehicleId: number;
  date: string;
  type: MaintenanceType;
  description: string;
  cost: number;
  nextServiceDate: string | null;
  nextServiceMileage: number | null;
  createdAt: string;
  // alias for UI compatibility
  loggedAt: string;
}

// Credentials are set once after login and used for every request
let basicAuthHeader: string | null = null;

export function setCredentials(credentials: string | null) {
  basicAuthHeader = credentials;
}

// Core fetch wrapper — attaches Basic Auth header and handles error responses
async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  };

  if (basicAuthHeader) {
    headers['Authorization'] = `Basic ${basicAuthHeader}`;
  }

  const res = await fetch(path, { ...options, headers });

  if (!res.ok) {
    // Try to extract ProblemDetail message from backend
    let message = `Request failed: ${res.status}`;
    try {
      const body = await res.json();
      if (body.detail) message = body.detail;
    } catch {
      // ignore parse error
    }
    throw new Error(message);
  }

  // 204 No Content — return empty
  if (res.status === 204) return undefined as T;

  return res.json();
}

// Normalize driver — backend returns createdAt, UI expects registeredAt too
function normalizeDriver(d: any): Driver {
  return { ...d, registeredAt: d.createdAt };
}

// Normalize maintenance — backend returns createdAt, UI expects loggedAt too
function normalizeMaintenance(m: any): MaintenanceRecord {
  return { ...m, loggedAt: m.createdAt };
}

export const api = {
  // ── Vehicles ──────────────────────────────────────────────────────────────

  getVehicles: (): Promise<Vehicle[]> =>
    request<Vehicle[]>('/api/vehicles'),

  createVehicle: (vehicle: Omit<Vehicle, 'id' | 'createdAt' | 'status'>): Promise<void> =>
    request<void>('/api/vehicles', {
      method: 'POST',
      body: JSON.stringify(vehicle),
    }),

  updateVehicle: (id: number, vehicle: Omit<Vehicle, 'id' | 'createdAt' | 'status'>): Promise<void> =>
    request<void>(`/api/vehicles/${id}`, {
      method: 'PUT',
      body: JSON.stringify(vehicle),
    }),

  deleteVehicle: (id: number): Promise<void> =>
    request<void>(`/api/vehicles/${id}`, { method: 'DELETE' }),

  // ── Drivers ───────────────────────────────────────────────────────────────

  getDrivers: async (): Promise<Driver[]> => {
    const data = await request<any[]>('/api/drivers');
    return data.map(normalizeDriver);
  },

  createDriver: (driver: { fullName: string; licenseNumber: string; phone: string; email: string }): Promise<void> =>
    request<void>('/api/drivers', {
      method: 'POST',
      body: JSON.stringify(driver),
    }),

  // ── Trips ─────────────────────────────────────────────────────────────────

  getTrips: async (driverId?: number, vehicleId?: number): Promise<Trip[]> => {
    const params = new URLSearchParams();
    if (driverId !== undefined) params.set('driverId', String(driverId));
    if (vehicleId !== undefined) params.set('vehicleId', String(vehicleId));
    const query = params.toString() ? `?${params}` : '';
    return request<Trip[]>(`/api/trips${query}`);
  },

  createTrip: (trip: { driverId: number; vehicleId: number; origin: string; destination: string }): Promise<void> =>
    request<void>('/api/trips', {
      method: 'POST',
      body: JSON.stringify(trip),
    }),

  updateTripStatus: (id: number, status: TripStatus): Promise<void> =>
    request<void>(`/api/trips/${id}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status }),
    }),

  // ── Maintenance ───────────────────────────────────────────────────────────

  getMaintenance: async (vehicleId?: number): Promise<MaintenanceRecord[]> => {
    if (vehicleId !== undefined) {
      const data = await request<any[]>(`/api/maintenance/${vehicleId}`);
      return data.map(normalizeMaintenance);
    }
    // No "get all" endpoint — return empty; pages that need all records fetch per vehicle
    return [];
  },

  createMaintenance: (record: {
    vehicleId: number;
    date: string;
    type: MaintenanceType;
    description: string;
    cost: number;
    nextServiceDate: string | null;
    nextServiceMileage: number | null;
  }): Promise<void> =>
    request<void>('/api/maintenance', {
      method: 'POST',
      body: JSON.stringify(record),
    }),

  getMaintenanceUpcoming: async (days = 30, currentMileage?: number): Promise<MaintenanceRecord[]> => {
    const params = new URLSearchParams({ days: String(days) });
    if (currentMileage !== undefined) params.set('currentMileage', String(currentMileage));
    const data = await request<any[]>(`/api/maintenance/upcoming?${params}`);
    return data.map(normalizeMaintenance);
  },

  getMaintenanceAlerts: async (days = 7): Promise<MaintenanceRecord[]> => {
    const data = await request<any[]>(`/api/maintenance/alerts?days=${days}`);
    return data.map(normalizeMaintenance);
  },

  // ── AI Chat ───────────────────────────────────────────────────────────────

  sendAiMessage: async (message: string): Promise<string> => {
    const res = await request<{ reply: string }>('/api/ai/chat', {
      method: 'POST',
      body: JSON.stringify({ message }),
    });
    return res.reply;
  },
};
