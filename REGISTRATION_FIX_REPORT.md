# Registration Fix Report

**Date:** 2025-07-13  
**Scope:** All issues found in the registration & authentication flow (frontend + backend)

---

## Issues Fixed

### Fix 1 — 🔴 `@Valid` errors returned no useful message to frontend
**File:** `src/main/java/com/fleetsync/fleetsync/user/UserController.java`

**Problem:** `@ExceptionHandler(IllegalArgumentException.class)` only caught service-level errors. Bean validation failures from `@Valid` threw `MethodArgumentNotValidException`, which was unhandled — the frontend received a raw 400 with no `detail` field and showed `"Registration failed"` for every validation error.

**Fix:** Added a second `@ExceptionHandler(MethodArgumentNotValidException.class)` that collects all field errors and returns them as a comma-separated `detail` string in a `ProblemDetail` 400 response.

---

### Fix 2 — 🔴 No route guards — protected pages accessible without authentication
**File:** `FrontendbyFigma/src/app/routes.tsx`

**Problem:** All routes (`/dashboard`, `/vehicles`, `/drivers`, etc.) were defined without any authentication check. A user could navigate directly to any page without logging in. `App.tsx` used `window.location.href` redirects which only fire after render, leaving a window where protected content could flash or API calls could fail with 401.

**Fix:** Added a `ProtectedRoute` component that reads `user` from `AuthContext`. If unauthenticated, redirects to `/login`. If wrong role (DRIVER accessing MANAGER routes), redirects to `/my-trips`. All protected routes now nest under `ProtectedRoute` in the router definition.

---

### Fix 3 — 🟡 Register success message hardcoded to "dashboard" for all roles
**File:** `FrontendbyFigma/src/app/pages/Register.tsx`

**Problem:** The success alert read `"Account created! Redirecting to dashboard..."` but DRIVER users are redirected to `/my-trips`, not `/dashboard`. This was misleading.

**Fix:** Changed message to `"Account created! Redirecting..."` — role-neutral and always accurate.

---

### Fix 4 — 🟡 Login role detection used fragile 2-request side-channel probe
**File:** `FrontendbyFigma/src/app/contexts/AuthContext.tsx`

**Problem:** After verifying credentials via `GET /api/trips`, the frontend made a second request to `GET /api/vehicles` to infer role — if it returned 200, user was MANAGER, otherwise DRIVER. A network error on the second request would silently misclassify a MANAGER as a DRIVER.

**Fix:** Added `GET /api/auth/me` endpoint to the backend (returns `{ username, role }`). Login now makes a single request to `/api/auth/me` which both verifies credentials and returns the actual role from the database.

---

### Fix 5 — 🟡 Invalid role value returned 409 Conflict instead of 400 Bad Request
**File:** `src/main/java/com/fleetsync/fleetsync/user/RegisterRequest.java`

**Problem:** The `role` field had `@NotBlank` but no format constraint. Sending `role: "ADMIN"` passed `@Valid` and reached `UserService`, which threw `IllegalArgumentException` — caught by the 409 handler. Wrong status code for a client input error.

**Fix:** Added `@Pattern(regexp = "^(MANAGER|DRIVER)$")` to the `role` field. Invalid values are now caught by `@Valid` before reaching the service and return 400 Bad Request via the new validation handler.

---

### Fix 6 — 🟢 `/api/auth/me` added to SecurityConfig permitted list
**File:** `src/main/java/com/fleetsync/fleetsync/config/SecurityConfig.java`

**Problem:** The new `/api/auth/me` endpoint needed an explicit rule in the security filter chain to allow any authenticated user (MANAGER or DRIVER) to access it.

**Fix:** Added `.requestMatchers("/api/auth/me").authenticated()` before the role-restricted rules.

---

## Files Changed

| File | Change |
|---|---|
| `UserController.java` | Added `MethodArgumentNotValidException` handler + `GET /api/auth/me` endpoint |
| `RegisterRequest.java` | Added `@Pattern` constraint on `role` field |
| `SecurityConfig.java` | Permitted `/api/auth/me` for authenticated users |
| `AuthContext.tsx` | Replaced 2-request role probe with single `/api/auth/me` call |
| `Register.tsx` | Fixed success message to be role-neutral |
| `routes.tsx` | Added `ProtectedRoute` component with role-based guards |
