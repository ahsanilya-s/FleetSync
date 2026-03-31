# FleetSync - Fleet Management Dashboard

**Tagline:** Smart Fleet. Smarter Decisions.

## Overview

FleetSync is a comprehensive fleet management dashboard built with React, TypeScript, and Tailwind CSS. It provides a complete solution for logistics and delivery companies to manage their vehicles, drivers, trips, and maintenance schedules.

## Features

### Authentication & Role-Based Access
- **Two User Roles:**
  - **MANAGER:** Full access to all features (Dashboard, Vehicles, Drivers, Trips, Maintenance, AI Assistant, Settings)
  - **DRIVER:** Limited access to view and update their assigned trips only

- **HTTP Basic Auth:** Credentials are stored in sessionStorage and sent with API requests
- **Protected Routes:** Automatic redirects based on authentication and role

### Manager Features

#### 1. Dashboard
- Real-time KPI cards showing:
  - Total Vehicles (with Available/On Trip breakdown)
  - Total Drivers (with Available/On Trip breakdown)
  - Active Trips count
  - Maintenance Alerts count
- Recent trips table
- Upcoming maintenance alerts
- Fleet status overview with visual progress bars
- AI Quick Ask widget

#### 2. Vehicles Management
- View all vehicles in a sortable, filterable table
- Add new vehicles with details:
  - Name, Type (Mini Van, Cargo Van, Pickup Truck, Box Truck)
  - Plate Number (with duplicate validation)
  - Capacity, Fuel Type
- Edit existing vehicles
- Delete vehicles (with confirmation)
- Filter by status (Available/On Trip)
- Search by name or plate number

#### 3. Drivers Management
- View all drivers in a table
- Register new drivers with:
  - Full Name, License Number
  - Phone, Email
  - Automatic duplicate validation
- Filter by status
- Search by name or license number

#### 4. Trips Management
- View all trips with comprehensive details
- Assign new trips to available drivers and vehicles
- Conflict detection (prevents assigning already-busy drivers/vehicles)
- Update trip status with guided transitions:
  - SCHEDULED → IN_PROGRESS → COMPLETED
  - SCHEDULED/IN_PROGRESS → CANCELLED
- Filter by status, driver, or vehicle
- Real-time status updates

#### 5. Maintenance Tracking
- Two-tab interface:
  - **History:** Complete maintenance log per vehicle
  - **Alerts:** Overdue and upcoming maintenance
- Log maintenance with:
  - Vehicle, Date, Type (Oil Change, Tire Rotation, etc.)
  - Description, Cost
  - Next Service Date/Mileage
- Color-coded alerts:
  - Red: Overdue
  - Amber: Due within 7 days
  - Green: Future (8-30 days)

#### 6. AI Assistant
- ChatGPT-style interface
- Live fleet data access
- Suggested questions for quick insights
- Real-time AI responses about:
  - Fleet performance
  - Maintenance recommendations
  - Driver availability
  - Cost analysis

### Driver Features

#### My Trips Page
- Simplified view showing only assigned trips
- Trip cards with full details
- Quick actions:
  - Start Trip (SCHEDULED → IN_PROGRESS)
  - Complete Trip (IN_PROGRESS → COMPLETED)
  - Cancel Trip
- Confirmation dialogs before status changes

## Technology Stack

- **Frontend:** React 18.3.1 + TypeScript
- **Routing:** React Router 7.13.0 (Data Mode)
- **Styling:** Tailwind CSS 4.1.12
- **Icons:** Lucide React
- **Date Handling:** date-fns 3.6.0
- **Build Tool:** Vite 6.3.5

## Color Palette

- **Primary Navy:** #0F172A (Sidebar, headers)
- **Electric Blue:** #3B82F6 (Primary actions, accents)
- **Emerald Green:** #10B981 (Success, Available status)
- **Amber:** #F59E0B (Warning, On Trip status)
- **Red:** #EF4444 (Danger, Overdue alerts)
- **Background:** #F8FAFC (Page background)
- **Card Background:** #FFFFFF
- **Text Primary:** #1E293B
- **Text Secondary:** #64748B

## Demo Credentials

### Manager Account
- Username: `manager1`
- Password: `password123`
- Access: Full dashboard and all features

### Driver Account
- Username: `driver1`
- Password: `password123`
- Access: My Trips page only

## Mock Data

The application includes realistic mock data:

### Vehicles
- Van A (ABC-123) - Mini Van - Available
- Van B (XYZ-456) - Cargo Van - On Trip
- Truck C (LHR-789) - Box Truck - Available

### Drivers
- Ali Khan (LIC-001) - Available
- Sara Ahmed (LIC-002) - On Trip
- Hassan Malik (LIC-003) - Available

### Trips
- Trip #1: Ali Khan → Van A | Lahore → Karachi (Completed)
- Trip #2: Sara Ahmed → Van B | Islamabad → Lahore (In Progress)
- Trip #3: Ali Khan → Van A | Karachi → Multan (Scheduled)

### Maintenance Alerts
- Van B: Oil Change due in 3 days
- Truck C: Brake Inspection overdue by 2 days

## Key UX Features

1. **Consistent Status Colors:** Same color scheme across all pages
2. **Inline Validation:** Form errors appear under specific fields
3. **Conflict Prevention:** System prevents double-booking drivers/vehicles
4. **Empty States:** Helpful messages and CTAs when no data exists
5. **Confirmation Dialogs:** Required for destructive actions
6. **Optimistic UI:** Immediate loading states on interactions
7. **Responsive Design:** Works on desktop, tablet, and mobile
8. **Real-time Updates:** Status changes reflect immediately

## File Structure

```
src/
├── app/
│   ├── components/
│   │   ├── Alert.tsx
│   │   ├── Button.tsx
│   │   ├── ConfirmDialog.tsx
│   │   ├── EmptyState.tsx
│   │   ├── Input.tsx
│   │   ├── KPICard.tsx
│   │   ├── Layout.tsx
│   │   ├── LoadingSpinner.tsx
│   │   ├── Modal.tsx
│   │   ├── Select.tsx
│   │   ├── Sidebar.tsx
│   │   └── StatusBadge.tsx
│   ├── contexts/
│   │   └── AuthContext.tsx
│   ├── pages/
│   │   ├── AIAssistant.tsx
│   │   ├── Dashboard.tsx
│   │   ├── Drivers.tsx
│   │   ├── Login.tsx
│   │   ├── Maintenance.tsx
│   │   ├── MyTrips.tsx
│   │   ├── NotFound.tsx
│   │   ├── Register.tsx
│   │   ├── Settings.tsx
│   │   ├── Trips.tsx
│   │   └── Vehicles.tsx
│   ├── services/
│   │   └── mockApi.ts
│   ├── App.tsx
│   └── routes.tsx
└── styles/
    └── theme.css
```

## Component Library

### Reusable Components

1. **StatusBadge:** Colored pill badges for trip and vehicle statuses
2. **KPICard:** Metric cards with icon, label, value, and subtext
3. **Button:** Primary, Secondary, Danger, Ghost variants with loading states
4. **Modal:** Reusable dialog with header, body, and footer
5. **Input/Select:** Form controls with labels, errors, and hints
6. **Alert:** Success, Error, Warning, Info banners
7. **ConfirmDialog:** Confirmation modal for destructive actions
8. **EmptyState:** Placeholder for empty data tables
9. **LoadingSpinner:** Full-page or inline loading indicators

## Future Enhancements

- Real-time GPS tracking integration
- Advanced analytics and reporting
- Mobile app for drivers
- Push notifications for trip updates
- Document management (licenses, insurance)
- Route optimization
- Fuel consumption tracking
- Integration with actual Spring Boot backend

## Notes

This is a frontend-only implementation using mock API responses. In production, all API calls would connect to a real Spring Boot REST API running on port 8080.
