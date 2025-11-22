# Auth Service API Guide

This document describes the authentication endpoints exposed by the **Auth Service** and how frontend clients should integrate with them through the API Gateway.

## Base URLs

| Environment | Gateway URL | Direct Service URL | Notes |
|-------------|-------------|--------------------|-------|
| Local (Docker) | `http://localhost:8080` | `http://localhost:8081` | Use the gateway URL (`/api/...`) so CORS, filters, and routing rules stay consistent with higher environments. |

> All examples below assume calls are made **through the API Gateway**.

## Authentication Model

- **Access token**: JWT, expires after **15 minutes** (`app.jwt.access-token-validity=900000`).
- **Refresh token**: JWT, expires after **30 days** (`app.jwt.refresh-token-validity=2592000000`).
- **Authorization header**: `Authorization: Bearer <access_token>` is required for any protected endpoint (e.g., `/api/auth/logout`, `/api/users/me`).
- Auth service responds with the common `ApiResponse<T>` envelope:
  ```json
  {
    "success": true,
    "message": "...",
    "data": { ... },
    "timestamp": "2025-11-22T08:15:30.123Z"
  }
  ```
- Only **one active session** is permitted per user. When the user logs in again (or refreshes), older sessions are automatically deactivated.

## Endpoints Summary

| Endpoint | Method | Auth Required | Description |
|----------|--------|---------------|-------------|
| `/api/auth/register` | POST | No | Creates a user and returns an initial token pair. |
| `/api/auth/login` | POST | No | Exchanges credentials for a token pair. |
| `/api/auth/refresh-token` | POST | No | Exchanges a valid refresh token for a new access/refresh pair and rotates the session. |
| `/api/auth/logout` | POST | Yes (access token) | Invalidates the active session for the supplied access token. |
| `/api/users/me` | GET | Yes (access token) | Returns the authenticated user profile. |

---

## 1. Register User — `POST /api/auth/register`
Creates a new account and logs the user in immediately.

### Request Body
```json
{
  "firstName": "Alice",
  "lastName": "Nguyen",
  "companyName": "Book Planet",
  "email": "alice@example.com",
  "mobileNo": "+1-555-555-5555",
  "password": "P@ssw0rd123",
  "role": "VENDOR" // optional, defaults to VENDOR
}
```

Validation highlights:
- `email` must be unique.
- `password` must be 8-255 chars.
- `mobileNo` accepts digits plus `+ - ( )` and spaces.

### Successful Response (`201 Created`)
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "user": {
      "id": "a8b3...",
      "firstName": "Alice",
      "lastName": "Nguyen",
      "companyName": "Book Planet",
      "email": "alice@example.com",
      "mobileNo": "+1-555-555-5555",
      "role": "VENDOR",
      "status": "ACTIVE",
      "createdAt": "2025-11-22T08:10:11Z",
      "updatedAt": "2025-11-22T08:10:11Z"
    },
    "tokens": {
      "accessToken": "<jwt>",
      "refreshToken": "<jwt>",
      "expiresIn": 900000
    }
  },
  "timestamp": "2025-11-22T08:10:11Z"
}
```

### Error Examples
- `409 Conflict` with `DuplicateResourceException` message when email or mobile number already exists.

---

## 2. Login — `POST /api/auth/login`
Authenticates an existing user and returns a fresh token pair.

### Request Body
```json
{
  "email": "alice@example.com",
  "password": "P@ssw0rd123"
}
```

### Successful Response (`200 OK`)
Same structure as the register endpoint (`AuthResponse`). `expiresIn` indicates milliseconds until the access token expires.

### Error Examples
- `401 Unauthorized` with message `"Invalid email or password"` for bad credentials.

---

## 3. Refresh Token — `POST /api/auth/refresh-token`
Rotates the token pair using a valid refresh token. The previous session is deactivated and a brand new session is created.

### Request Body
```json
{
  "refreshToken": "<refresh-jwt>"
}
```

### Successful Response (`200 OK`)
Returns the same `AuthResponse` payload as login/register, containing a brand new `accessToken`, `refreshToken`, and `expiresIn`.

### Failure Cases
- `401 Unauthorized` when the refresh token is invalid, expired, or already used (previous session deactivated).

---

## 4. Logout — `POST /api/auth/logout`
Invalidates the active session represented by the provided **access token**. Frontend must pass the access token in the `Authorization` header.

### Headers
```
Authorization: Bearer <access-token>
```

### Response (`200 OK`)
```json
{
  "success": true,
  "message": "Logout successful",
  "data": null,
  "timestamp": "2025-11-22T08:25:45Z"
}
```

> If the header is missing or malformed the endpoint is still idempotent and returns success, but no session is touched.

---

## 5. Current User Profile — `GET /api/users/me`
Returns the authenticated user’s profile information. Requires a valid access token.

### Headers
```
Authorization: Bearer <access-token>
```

### Response (`200 OK`)
```json
{
  "success": true,
  "message": "User profile retrieved successfully",
  "data": {
    "id": "a8b3...",
    "firstName": "Alice",
    "lastName": "Nguyen",
    "companyName": "Book Planet",
    "email": "alice@example.com",
    "mobileNo": "+1-555-555-5555",
    "role": "VENDOR",
    "status": "ACTIVE",
    "createdAt": "2025-11-22T08:10:11Z",
    "updatedAt": "2025-11-22T08:25:45Z"
  },
  "timestamp": "2025-11-22T08:26:01Z"
}
```

### Failure Cases
- `401 Unauthorized` when the access token is missing/expired/invalid.

---

## Integration Tips

1. **Use the gateway URL** (`http://localhost:8080`) so CORS and route filters are handled centrally.
2. **Persist the refresh token** securely on the frontend (e.g., httpOnly cookie or secure storage) and call `/api/auth/refresh-token` before the 15‑minute access token expires.
3. After every login/register/refresh response, **replace both tokens** locally. Older tokens become invalid due to the single-session policy.
4. Include a descriptive **`User-Agent`** header when possible—the auth service logs it as `deviceInfo` for security auditing.
5. Handle `ApiResponse.success === false` as an error even if the HTTP status is `200` (e.g., future validation responses).
6. On `401` from protected endpoints, trigger a refresh attempt once; if refresh also fails, redirect users to login.
