# JWT Authentication Microservices - Testing Guide

## Architecture Overview

- **Auth Service (Port 8082)**: 
  - **PUBLIC** - Client can login directly
  - Handles: Login, Register, Token Validation
  - Access: Direct from client OR via gateway

- **User Service (Port 8081)**: 
  - **INTERNAL** - Must access through gateway only
  - Handles: User data retrieval
  - Access: `http://localhost:8080/api/users` (via gateway)

- **Order Service (Port 8083)**: 
  - **INTERNAL** - Must access through gateway only
  - Handles: Order data retrieval
  - Access: `http://localhost:8080/api/orders` (via gateway)

- **API Gateway (Port 8080)**: 
  - Routes all requests to backend services
  - Forward `/api/users/**` → User Service (8081)
  - Forward `/api/orders/**` → Order Service (8083)
  - Forward `/auth/**` → Auth Service (8082)

## Access Patterns

| Endpoint | Access Via | Notes |
|---|---|---|
| **Login** | Direct to 8082 OR via Gateway | ✅ PUBLIC |
| **Get Orders** | Gateway only (8080) | ❌ NO direct access to 8083 |
| **Get Users** | Gateway only (8080) | ❌ NO direct access to 8081 |

```
┌─────────────────┐
│     CLIENT      │
└────────┬────────┘
         │
    ┌────┴──────────────────────┬──────────┐
    │                           │          │
    ▼ (Public)                  ▼ (Via Gateway)
 ┌────────────────┐          ┌──────────────┐
 │ Auth Service   │          │ API Gateway  │
 │   (8082)       │          │   (8080)     │
 │ - login ✅     │          │              │
 │ - register ✅  │          └──────┬───────┘
 └────────────────┘                 │
                          ┌─────────┴─────────┐
                          ▼                   ▼
                     ┌──────────┐        ┌──────────┐
                     │ User Svc │        │ Order Svc│
                     │ (8081)   │        │ (8083)   │
                     │ INTERNAL │        │ INTERNAL │
                     └──────────┘        └──────────┘
```

---

## Step 1: Build the Project

Open PowerShell in the project root and run:

```powershell
cd C:\Users\leanh\IdeaProjects\spring-gateway
mvn clean install -DskipTests
```

Wait for the build to complete successfully.

---

## Step 2: Start All Services

Open **4 separate PowerShell terminals** and run each command:

### Terminal 1 - Auth Service (Required - handles all authentication)
```powershell
cd C:\Users\leanh\IdeaProjects\spring-gateway\auth-service
mvn spring-boot:run
```
Expected output: `Tomcat started on port(s): 8082` and `Sample users initialized!`

### Terminal 2 - User Service
```powershell
cd C:\Users\leanh\IdeaProjects\spring-gateway\user-service
mvn spring-boot:run
```
Expected output: `Tomcat started on port(s): 8081`

### Terminal 3 - Order Service
```powershell
cd C:\Users\leanh\IdeaProjects\spring-gateway\order-service
mvn spring-boot:run
```
Expected output: `Tomcat started on port(s): 8083` and `Sample orders initialized!`

### Terminal 4 - API Gateway (Optional)
```powershell
cd C:\Users\leanh\IdeaProjects\spring-gateway\api-gateway
mvn spring-boot:run
```
Expected output: `Tomcat started on port(s): 8080`

### Terminal 5 - Eureka Server (Optional)
```powershell
cd C:\Users\leanh\IdeaProjects\spring-gateway\eureka-server
mvn spring-boot:run
```
Expected output: `Tomcat started on port(s): 8761` (not required for testing)

---

## Step 3: Open Postman

1. Download and install [Postman](https://www.postman.com/downloads/)
2. Create a new collection: `JWT Authentication Microservices`

---

## Testing Cases

### ✅ TEST CASE 1: User Login (PUBLIC - Direct to Auth Service)

**Method**: POST  
**URL**: `http://localhost:8082/auth/login` **(Direct, NOT via gateway)**

**Headers**:
```
Content-Type: application/json
```

**Request Body**:
```json
{
  "username": "john",
  "password": "password123"
}
```

**Expected Response** (Status: 200 OK):
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huIiwidXNlci1pZCI6MSwiaWF0IjoxNzAyNzc3NDAwLCJleHAiOjE3MDI3ODEwMDB9.xxxxx...",
  "userId": 1,
  "username": "john",
  "email": "john@example.com",
  "expiresIn": 3600
}
```

**💡 Copy the token value and save it - you'll need it for other tests!**

**Alternative Users to Test**:
- Username: `jane`, Password: `password456` (userId: 2)
- Username: `admin`, Password: `admin123` (userId: 3)

**⚠️ Note**: This endpoint is PUBLIC - no gateway needed!

---

### ✅ TEST CASE 2: Create Order via Gateway (JWT Required)

**Method**: POST  
**URL**: `http://localhost:8080/api/orders`

**Headers**:
```
Content-Type: application/json
Authorization: Bearer <PASTE_TOKEN_FROM_TEST_1>
```

**Request Body**:
```json
{
  "orderDetails": "New Order: Laptop + Monitor + Keyboard"
}
```

**Expected Response** (Status: 201 Created):
```json
{
  "id": 6,
  "userId": 1,
  "orderDetails": "New Order: Laptop + Monitor + Keyboard",
  "createdAt": "2024-12-16T19:30:45.123456"
}
```

**What to check**:
- ✅ Status is 201 Created
- ✅ userId in response is 1 (from token)
- ✅ Order details match your request
- ✅ Request goes through Gateway (port 8080) then routed to Order Service (8083)

---

### ✅ TEST CASE 3: Get All Orders via Gateway (JWT Required)

**Method**: GET  
**URL**: `http://localhost:8080/api/orders`

**Headers**:
```
Authorization: Bearer <PASTE_TOKEN_FROM_TEST_1>
```

**Expected Response** (Status: 200 OK):
```json
[
  {
    "id": 1,
    "userId": 1,
    "orderDetails": "Order 001: Laptop + Mouse",
    "createdAt": "2024-12-16T09:00:00"
  },
  {
    "id": 2,
    "userId": 1,
    "orderDetails": "Order 002: Keyboard",
    "createdAt": "2024-12-16T09:15:00"
  },
  {
    "id": 6,
    "userId": 1,
    "orderDetails": "New Order: Laptop + Monitor + Keyboard",
    "createdAt": "2024-12-16T19:30:45.123456"
  }
]
```

**What to check**:
- ✅ All orders belong to userId 1
- ✅ Shows both sample orders and newly created order
- ✅ Request goes through Gateway (port 8080)

---

### ✅ TEST CASE 4: Get Specific Order via Gateway (JWT Required)

**Method**: GET  
**URL**: `http://localhost:8080/api/orders/1`

**Headers**:
```
Authorization: Bearer <PASTE_TOKEN_FROM_TEST_1>
```

**Expected Response** (Status: 200 OK):
```json
{
  "id": 1,
  "userId": 1,
  "orderDetails": "Order 001: Laptop + Mouse",
  "createdAt": "2024-12-16T09:00:00"
}
```

---

### ✅ TEST CASE 5: Missing JWT Token via Gateway (Error Test)

**Method**: GET  
**URL**: `http://localhost:8080/api/orders`

**Headers**: (Leave empty - NO Authorization header)

**Expected Response** (Status: 401 Unauthorized):
```
Missing or invalid Authorization header
```

**What to check**:
- ✅ Error message appears
- ✅ Status is 401

---

### ✅ TEST CASE 6: Invalid JWT Token via Gateway (Error Test)

**Method**: GET  
**URL**: `http://localhost:8080/api/orders`

**Headers**:
```
Authorization: Bearer invalid_token_123456789
```

**Expected Response** (Status: 401 Unauthorized):
```
Token validation failed: Unable to read JSON value
```

---

### ✅ TEST CASE 7: Access Other User's Order via Gateway (Forbidden Test)

**Step 1**: Login as `john` (Test Case 1) and get token
**Step 2**: Try to access order ID 3 (which belongs to `jane`)

**Method**: GET  
**URL**: `http://localhost:8080/api/orders/3`

**Headers**:
```
Authorization: Bearer <JOHN_TOKEN>
```

**Expected Response** (Status: 403 Forbidden):
```json
You are not authorized to access this order
```

**What to check**:
- ✅ John cannot access Jane's order
- ✅ Status is 403 Forbidden

---

### ✅ TEST CASE 8: Get All Users via Gateway (No Auth Required)

**Method**: GET  
**URL**: `http://localhost:8080/api/users`

**Headers**: (None required)

**Expected Response** (Status: 200 OK):
```json
[
  {
    "id": 1,
    "username": "john",
    "password": "password123",
    "email": "john@example.com"
  },
  {
    "id": 2,
    "username": "jane",
    "password": "password456",
    "email": "jane@example.com"
  },
  {
    "id": 3,
    "username": "admin",
    "password": "admin123",
    "email": "admin@example.com"
  }
]
```

---

### ✅ TEST CASE 9: Test via API Gateway

You can also test through the API Gateway (Port 8080) which routes to backend services:

**Login via Gateway**:
```
POST http://localhost:8080/auth/login
```

**Create Order via Gateway**:
```
POST http://localhost:8080/orders
```

**Get Orders via Gateway**:
```
GET http://localhost:8080/orders
```

---

## Sample Data Initialized

### Users:
| ID | Username | Password | Email |
|---|---|---|---|
| 1 | john | password123 | john@example.com |
| 2 | jane | password456 | jane@example.com |
| 3 | admin | admin123 | admin@example.com |

### Orders:
| ID | UserId | Details | 
|---|---|---|
| 1 | 1 | Order 001: Laptop + Mouse |
| 2 | 1 | Order 002: Keyboard |
| 3 | 2 | Order 003: Monitor |
| 4 | 2 | Order 004: USB Cable |
| 5 | 3 | Order 005: Headphones |

---

## cURL Commands (Alternative to Postman)

If you prefer to use cURL instead:

### Login (Direct to Auth Service):
```bash
curl -X POST http://localhost:8082/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"john\",\"password\":\"password123\"}"
```

### Create Order (Via Gateway):
```bash
curl -X POST http://localhost:8080/api/orders ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer YOUR_TOKEN_HERE" ^
  -d "{\"orderDetails\":\"Laptop\"}"
```

### Get Orders (Via Gateway):
```bash
curl -X GET http://localhost:8080/api/orders ^
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Get Specific Order (Via Gateway):
```bash
curl -X GET http://localhost:8080/api/orders/1 ^
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Get All Users (Via Gateway):
```bash
curl -X GET http://localhost:8080/api/users
```

---

## Postman Collection (JSON Import)

You can import this JSON into Postman:

1. In Postman, click **Import** → **Paste Raw Text**
2. Copy and paste the JSON below:

```json
{
  "info": {
    "name": "JWT Authentication Microservices",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "1. User Login",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"username\": \"john\",\n  \"password\": \"password123\"\n}"
        },
        "url": {
          "raw": "http://localhost:8082/auth/login",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8082",
          "path": ["auth", "login"]
        }
      }
    },
    {
      "name": "2. Create Order",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          },
          {
            "key": "Authorization",
            "value": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"orderDetails\": \"New Order: Laptop + Monitor\"\n}"
        },
        "url": {
          "raw": "http://localhost:8080/api/orders",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "orders"]
        }
      }
    },
    {
      "name": "3. Get My Orders",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
          }
        ],
        "url": {
          "raw": "http://localhost:8080/api/orders",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "orders"]
        }
      }
    },
    {
      "name": "4. Get Order by ID",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
          }
        ],
        "url": {
          "raw": "http://localhost:8080/api/orders/1",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "orders", "1"]
        }
      }
    },
    {
      "name": "5. Get All Users",
      "request": {
        "method": "GET",
        "url": {
          "raw": "http://localhost:8080/api/users",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "users"]
        }
      }
    }
  ]
}
```

---

## Quick Testing Checklist

- [ ] Build project: `mvn clean install -DskipTests`
- [ ] Start all 4 services
- [ ] Test Case 1: Login and get JWT token
- [ ] Test Case 2: Create order with token
- [ ] Test Case 3: Get all orders for user
- [ ] Test Case 4: Get specific order
- [ ] Test Case 5: Error test - missing token
- [ ] Test Case 6: Error test - invalid token
- [ ] Test Case 7: Error test - forbidden access
- [ ] Test Case 8: Get users (no auth)

---

## Key Points to Remember

✅ **JWT Secret Key** (same across services):
```
mySecretKeyForJWTTokenGenerationAndValidation12345678901234567890
```

✅ **Token Expiration**: 3600 seconds (1 hour)

✅ **Authorization Header Format**:
```
Authorization: Bearer <YOUR_JWT_TOKEN>
```

✅ **Order Access Control**: Users can ONLY view/create their own orders

✅ **API Gateway Routes**:
- `/auth/**` → Auth Service (8082)
- `/api/auth/**` → Auth Service (8082) with /api prefix strip
- `/users/**` → User Service (8081)
- `/api/users/**` → User Service (8081) with /api prefix strip
- `/orders/**` → Order Service (8083)
- `/api/orders/**` → Order Service (8083) with /api prefix strip

---

## Troubleshooting

| Error | Solution |
|---|---|
| `Missing or invalid Authorization header` | Add JWT token to request header |
| `Token validation failed: Unable to read JSON value` | Token is invalid/corrupted |
| `Expired JWT token` | Generate new token by logging in again |
| `User ID not found in token` | Token format is incorrect |
| `You are not authorized to access this order` | Order belongs to different user |
| `Connection refused` | Make sure service is running on correct port |

---

## Architecture Diagram

```
┌─────────────┐
│  Postman    │
└──────┬──────┘
       │
       ├─→ POST /users/login ────────────→ User Service (8081)
       │                                    ├─ Check credentials
       │                                    └─ Generate JWT token
       │
       ├─→ POST /orders ──────────┐
       │   + Authorization header │
       │                          ↓
       │                    Order Service (8082)
       │                    ├─ Validate JWT
       │                    ├─ Extract userId
       │                    └─ Create order
       │
       └─→ GET /orders ───────────┐
           + Authorization header │
                                  ↓
                          Order Service (8082)
                          ├─ Validate JWT
                          ├─ Extract userId
                          └─ Return user's orders
```

Happy Testing! 🚀

