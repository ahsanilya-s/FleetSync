Design a complete, production-ready web application UI for FleetSync — a fleet management dashboard for logistics and delivery companies. The application is a React frontend (running on port 5173) that connects to a Spring Boot REST API (running on port 8080).

Brand & Visual Identity
App name: FleetSync
Tagline: Smart Fleet. Smarter Decisions.
Logo concept: A truck/van icon combined with a sync/refresh arrow, suggesting real-time fleet tracking
Color palette:
Primary: Deep Navy Blue #0F172A
Accent: Electric Blue #3B82F6
Success/Available: Emerald Green #10B981
Warning/On-Trip: Amber #F59E0B
Danger/Overdue: Red #EF4444
Background: Slate Gray #F8FAFC
Card background: White #FFFFFF
Text primary: #1E293B
Text secondary: #64748B
Typography: Inter or Geist Sans — clean, modern, data-dense
Style: Professional SaaS dashboard — dark sidebar, light content area, card-based layout, subtle shadows, rounded corners (8px), data tables with hover states
Icons: Lucide or Heroicons style — outline icons for nav, solid for status badges
Application Structure
The app has two user roles with different access levels:

Role	Access
MANAGER	Full access to all pages
DRIVER	Only the "My Trips" page to update trip status
Authentication uses HTTP Basic Auth — credentials are stored in the browser after login and sent with every API request as Authorization: Basic <base64(username:password)>.

Pages to Design
Design the following 10 screens in full detail:

SCREEN 1 — Login Page
Route: /login
Access: Public (no auth required)

Layout:

Centered card on a dark navy background with subtle truck/road illustration or gradient
FleetSync logo + tagline at the top of the card
Form fields:
Username input (placeholder: "Enter your username")
Password input with show/hide toggle
Role is NOT selected here — it is determined by the backend based on the registered account
"Sign In" primary button (full width, blue)
Link below: "Don't have an account? Register"
Error state: red inline alert banner — "Invalid username or password"
Loading state: spinner inside the button while authenticating
Notes:

No JWT, no token storage — credentials are stored in memory/sessionStorage and sent as Basic Auth header on every request
After login, redirect MANAGER to /dashboard, redirect DRIVER to /my-trips
SCREEN 2 — Register Page
Route: /register
Access: Public

Layout:

Same centered card style as login
Form fields:
Username (3–50 chars, letters/numbers/underscores only — show hint text)
Email
Password (min 8 chars — show strength indicator)
Role dropdown: "Manager" or "Driver"
"Create Account" primary button
Link: "Already have an account? Sign In"
Validation errors shown inline under each field:
"Username already taken" → red text under username field
"Email already in use" → red text under email field
"Password must be at least 8 characters"
Success state: green checkmark + "Account created! Redirecting to login..."
SCREEN 3 — Manager Dashboard (Home)
Route: /dashboard
Access: MANAGER only

This is the main overview page. It should feel like a command center.

Layout: Dark sidebar (left) + light content area (right)

Sidebar navigation items:

🏠 Dashboard (active)
🚐 Vehicles
👤 Drivers
🗺️ Trips
🔧 Maintenance
🤖 AI Assistant
─────────────
⚙️ Settings (bottom)
🚪 Logout (bottom)
Content area — 4 KPI stat cards (top row):

Total Vehicles — number + breakdown: X Available (green dot), X On Trip (amber dot)
Total Drivers — number + breakdown: X Available, X On Trip
Active Trips — count of SCHEDULED + IN_PROGRESS trips
Maintenance Alerts — count of overdue/due-soon vehicles (red badge if > 0)
Content area — 2 panels (middle row):

Left panel: Recent Trips table (last 5 trips) — columns: Trip ID, Driver, Vehicle, Origin → Destination, Status badge, Created At
Right panel: Upcoming Maintenance list — vehicle name, maintenance type, due date, days remaining (color-coded: red if overdue, amber if ≤7 days, green if >7 days)
Content area — bottom row:

Fleet Status Overview — horizontal bar or donut chart showing vehicle status distribution (Available vs On Trip)
AI Quick Ask — small chat input box with placeholder "Ask FleetSync AI anything about your fleet..." and a send button
Status badges design:

AVAILABLE → green pill badge
ON_TRIP → amber pill badge
SCHEDULED → blue pill badge
IN_PROGRESS → amber pill badge with pulsing dot
COMPLETED → green pill badge
CANCELLED → gray pill badge
SCREEN 4 — Vehicles Page
Route: /vehicles
Access: MANAGER only

Layout: Full-width table page with top action bar

Top bar:

Page title "Vehicles" with vehicle count
Search input (filter by name or plate number)
Filter dropdown: All / Available / On Trip
"+ Add Vehicle" primary button (opens modal)
Table columns:

ID
Name
Type (Mini Van / Cargo Van / Pickup Truck / Box Truck)
Plate Number
Capacity
Fuel Type (DIESEL / PETROL / ELECTRIC / HYBRID)
Status (colored badge)
Created At
Actions: Edit (pencil icon) | Delete (trash icon, red on hover)
Empty state: Illustration of an empty garage + "No vehicles registered yet. Add your first vehicle."

Add/Edit Vehicle Modal:

Modal title: "Add New Vehicle" or "Edit Vehicle"
Fields:
Vehicle Name (text input)
Type (dropdown: Mini Van, Cargo Van, Pickup Truck, Box Truck)
Plate Number (text input — shows "Plate number already registered" error inline)
Capacity (number input, min 1)
Fuel Type (dropdown: Diesel, Petrol, Electric, Hybrid, Unknown)
Cancel + Save buttons
Loading state on Save button
Delete confirmation dialog:

"Are you sure you want to delete [Vehicle Name]?"
Warning: "This will also delete all trips and maintenance records for this vehicle."
Cancel + "Delete" (red) buttons
SCREEN 5 — Drivers Page
Route: /drivers
Access: MANAGER only

Layout: Same structure as Vehicles page

Top bar:

Page title "Drivers" with driver count
Search input (filter by name or license)
Filter dropdown: All / Available / On Trip
"+ Register Driver" primary button
Table columns:

ID
Full Name
License Number
Phone
Email
Status (AVAILABLE green / ON_TRIP amber badge)
Registered At
Actions: View Trips (link icon)
Register Driver Modal:

Fields:
Full Name
License Number (unique — shows "License number already registered" error)
Phone Number
Email (unique — shows "Email already registered" error)
Cancel + Register buttons
Note displayed in modal footer: "Driver accounts are separate from login accounts. This registers a driver profile for trip assignment."

SCREEN 6 — Trips Page
Route: /trips
Access: MANAGER only

Layout: Table with filters + assign trip panel

Top bar:

Page title "Trips"
Filter by status: All / Scheduled / In Progress / Completed / Cancelled (tab-style filter)
Filter by Driver (dropdown)
Filter by Vehicle (dropdown)
"+ Assign Trip" primary button
Table columns:

Trip ID
Driver (name)
Vehicle (name + plate)
Origin → Destination (with arrow icon between them)
Status (colored badge with pulsing dot for IN_PROGRESS)
Start Time (— if not started)
End Time (— if not ended)
Created At
Actions: Update Status button (only shown for SCHEDULED and IN_PROGRESS trips)
Assign Trip Modal:

Fields:
Driver (searchable dropdown — only shows AVAILABLE drivers, shows "No available drivers" if all are ON_TRIP)
Vehicle (searchable dropdown — only shows AVAILABLE vehicles)
Origin (text input)
Destination (text input)
Conflict error states:
"Driver is already on an active trip" → red alert in modal
"Vehicle is already on an active trip" → red alert in modal
Cancel + Assign buttons
Update Trip Status Modal:

Shows current status
Shows allowed next transitions:
If SCHEDULED: buttons "Start Trip" (→ IN_PROGRESS) and "Cancel Trip" (→ CANCELLED)
If IN_PROGRESS: buttons "Complete Trip" (→ COMPLETED) and "Cancel Trip" (→ CANCELLED)
Confirmation message: "Starting this trip will mark the driver and vehicle as On Trip."
Completion message: "Completing this trip will free up the driver and vehicle."
Trip Status Lifecycle visual (shown at top of Update Status modal):

SCHEDULED → IN_PROGRESS → COMPLETED
                        ↘ CANCELLED
         ↘ CANCELLED
SCREEN 7 — Maintenance Page
Route: /maintenance
Access: MANAGER only

Layout: Two-tab layout — "History" and "Alerts"

Tab 1: History

Vehicle selector dropdown at top (select a vehicle to view its maintenance history)
Table columns: ID, Date, Type, Description, Cost (PKR), Next Service Date, Next Service Mileage, Logged At
"+ Log Maintenance" button
Log Maintenance Modal:

Fields:
Vehicle (dropdown — all vehicles)
Date (date picker)
Type (dropdown: OIL_CHANGE, TIRE_ROTATION, BRAKE_INSPECTION, ENGINE_CHECK, BATTERY_CHECK, GENERAL_SERVICE, OTHER)
Description (textarea, optional)
Cost (number input, min 0)
Next Service Date (date picker, optional)
Next Service Mileage (number input, optional)
Cancel + Log Record buttons
Tab 2: Alerts & Upcoming

Two sub-sections:
Overdue / Due Soon (≤7 days) — red/amber cards showing vehicle name, maintenance type, due date, days overdue
Upcoming (next 30 days) — list with days remaining
"Check by Mileage" input: enter current mileage to also surface mileage-based upcoming maintenance
Each alert card has a "Log Service Now" shortcut button
SCREEN 8 — AI Assistant Page
Route: /ai
Access: MANAGER only

Layout: Full-page chat interface — similar to ChatGPT but fleet-themed

Design:

Dark header bar: "FleetSync AI" with a robot/sparkle icon and subtitle "Powered by OpenAI — Ask anything about your fleet"
Chat message area (scrollable):
AI messages: left-aligned, dark navy bubble, white text, FleetSync AI avatar
User messages: right-aligned, blue bubble, white text
Timestamps on each message
Input area (bottom, sticky):
Text input: "Ask about your fleet..." (max 2000 chars, char counter shown)
Send button (blue, arrow icon)
Keyboard shortcut hint: "Press Enter to send"
Loading state: animated typing indicator (three dots) while waiting for OpenAI response
Error state: "AI service is temporarily unavailable. Please try again later." — amber alert banner
Suggested questions (shown when chat is empty):

"Which vehicle has the highest maintenance cost this month?"
"Summarise overall fleet performance."
"Which drivers are currently available?"
"Are any vehicles overdue for maintenance?"
"Suggest the best vehicle for a long-distance trip."
Note at bottom: "FleetSync AI has access to your live fleet data including vehicles, drivers, trips, and maintenance records."

SCREEN 9 — My Trips Page (Driver View)
Route: /my-trips
Access: DRIVER role only

Layout: Simplified single-page view — no sidebar, just a top navbar

Top navbar:

FleetSync logo (left)
"Welcome, [username]" (right)
Logout button
Content:

Page title: "My Assigned Trips"
Cards (not a table) — each trip shown as a card:
Trip ID + status badge
Vehicle: [name] ([plate number])
Route: [Origin] → [Destination] (with map pin icons)
Scheduled at: [created_at]
Start time: [start_time or "Not started yet"]
End time: [end_time or "—"]
Action button:
If SCHEDULED: "Start Trip" button (green)
If IN_PROGRESS: "Complete Trip" button (blue) + "Cancel" (red, smaller)
If COMPLETED/CANCELLED: no button, just the status badge
Confirmation dialog before status change:

"Are you sure you want to start this trip?"
"Are you sure you want to mark this trip as completed?"
Empty state: "No trips assigned to you yet."

SCREEN 10 — 404 / Error Page
Route: * (catch-all)

Layout:

Centered on page
Large "404" in navy
Subtitle: "Looks like this route went off the map."
FleetSync truck illustration driving off a cliff (playful)
"Back to Dashboard" button
Component Library to Design
Design these reusable components as a separate component page in Figma:

Status Badge — variants: AVAILABLE, ON_TRIP, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
KPI Stat Card — icon + label + number + sub-breakdown
Data Table Row — default, hover, selected states
Modal — with header, body, footer (cancel + confirm buttons)
Primary Button — default, hover, loading, disabled states
Secondary Button — same states
Danger Button — for delete/cancel actions
Form Input — default, focused, error, disabled states
Dropdown Select — default, open, selected states
Alert Banner — success (green), error (red), warning (amber), info (blue)
Sidebar Nav Item — default, active, hover states
Empty State — illustration + message + CTA button
Confirmation Dialog — title + message + cancel + confirm
Loading Spinner — inline and full-page variants
Chat Bubble — user variant and AI variant
Responsive Breakpoints
Design for:

Desktop (1440px) — primary design target, full sidebar visible
Tablet (768px) — sidebar collapses to icon-only rail
Mobile (375px) — sidebar becomes bottom navigation, tables become cards
Key UX Rules to Follow
Status colors are consistent everywhere — always use the same color for the same status across all pages
Disabled states for unavailable actions — e.g., "Assign Trip" button is disabled if no AVAILABLE drivers or vehicles exist
Inline validation — form errors appear under the specific field, not just at the top
Optimistic UI — show loading state immediately on button click, don't wait for API response to disable the button
Conflict errors are prominent — 409 Conflict errors (duplicate plate, driver already on trip) shown as red alert banners inside modals, not just console errors
Empty states on every table — never show a blank table, always show an illustration + message + CTA
Confirmation before destructive actions — delete vehicle, cancel trip always require a confirmation dialog
Trip status transitions are guided — never show all possible statuses as options, only show valid next transitions based on current status
AI chat is always accessible — small "Ask AI" floating button visible on Dashboard page as a shortcut to the AI page
Role-based UI — DRIVER users see a completely different, simplified interface with no sidebar and only their own trips
API Integration Reference (for developer handoff annotations)
Annotate each screen with the API calls it makes:

Screen	API Calls
Login	Validates by calling GET /api/vehicles with credentials — if 200 = valid, if 401 = invalid
Register	POST /api/auth/register
Dashboard	GET /api/vehicles, GET /api/drivers, GET /api/trips, GET /api/maintenance/alerts?days=7
Vehicles	GET /api/vehicles, POST /api/vehicles, PUT /api/vehicles/{id}, DELETE /api/vehicles/{id}
Drivers	GET /api/drivers, POST /api/drivers
Trips	GET /api/trips, POST /api/trips, PUT /api/trips/{id}/status
Maintenance	POST /api/maintenance, GET /api/maintenance/{vehicleId}, GET /api/maintenance/upcoming, GET /api/maintenance/alerts
AI Assistant	POST /api/ai/chat with body { "message": "..." }
My Trips (Driver)	GET /api/trips?driverId={id}, PUT /api/trips/{id}/status
Data Models for Realistic Mockup Content
Use this realistic sample data in all mockup screens:

Vehicles:

Van A | Mini Van | ABC-123 | Diesel | AVAILABLE
Van B | Cargo Van | XYZ-456 | Petrol | ON_TRIP
Truck C | Box Truck | LHR-789 | Diesel | AVAILABLE
Drivers:

Ali Khan | LIC-001 | 0300-1234567 | AVAILABLE
Sara Ahmed | LIC-002 | 0321-9876543 | ON_TRIP
Trips:

Trip #1 | Ali Khan → Van A | Lahore → Karachi | COMPLETED
Trip #2 | Sara Ahmed → Van B | Islamabad → Lahore | IN_PROGRESS
Trip #3 | Ali Khan → Van A | Karachi → Multan | SCHEDULED
Maintenance Alerts:

Van B — OIL_CHANGE — Due: 3 days (amber)
Truck C — BRAKE_INSPECTION — Overdue by 2 days (red)
AI Chat Example:

User: "Which vehicle needs maintenance most urgently?"
AI: "Truck C (LHR-789) is overdue for a Brake Inspection by 2 days. Van B (XYZ-456) is due for an Oil Change in 3 days. I recommend scheduling Truck C immediately."
Figma File Organization
Organize the Figma file into these pages:

Cover — project title, tech stack, author
Design System — colors, typography, spacing, icons
Components — all reusable components with variants
Auth Screens — Login, Register
Manager — Dashboard
Manager — Vehicles
Manager — Drivers
Manager — Trips
Manager — Maintenance
Manager — AI Assistant
Driver — My Trips
Error States — 404, 503 (AI unavailable), 401 (session expired)
Mobile Screens — key screens at 375px width
Developer Handoff — annotated screens with API calls, component names, spacing values