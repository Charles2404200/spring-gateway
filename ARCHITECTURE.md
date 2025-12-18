# Spring Gateway Demo - Hướng dẫn Toàn bộ Hệ thống

## 📋 Tổng quan kiến trúc

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT                                   │
└───────────────────┬─────────────────────────────────────────────┘
                    │ HTTP Requests
                    ▼
┌─────────────────────────────────────────────────────────────────┐
│              API GATEWAY (Port 8080)                             │
│  ├─ /auth/**          → Auth Service (Port 8082)               │
│  ├─ /api/auth/**      → Auth Service (Port 8082) [StripPrefix] │
│  ├─ /users/**         → User Service (Port 8081)               │
│  ├─ /api/users/**     → User Service (Port 8081) [StripPrefix] │
│  ├─ /orders/**        → Order Service (Port 8083)              │
│  └─ /api/orders/**    → Order Service (Port 8083) [StripPrefix]│
└─────────────────────────────────────────────────────────────────┘
         │                │                 │
         ▼                ▼                 ▼
   ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
   │  Auth        │ │  User        │ │  Order       │
   │  Service     │ │  Service     │ │  Service     │
   │  (Port 8082) │ │  (Port 8081) │ │  (Port 8083) │
   └──────────────┘ └──────────────┘ └──────────────┘
```

## 🔐 Quy trình Xác thực (Authentication Flow)

```
1. REGISTER / LOGIN
   Client → Gateway → Auth Service
   └─ Create JWT Token (signed with secret key)
   └─ Return: { token, userId, username }

2. SUBSEQUENT REQUESTS
   Client → Gateway (Headers: Authorization: Bearer <token>)
   └─ Forward to Services
   └─ Services validate token (optional)

3. TOKEN VALIDATION
   Service → Auth Service (/auth/validate)
   └─ Check if token is valid & not expired
   └─ Return: { valid, userId, username }
```

## 📚 Các Service

### 1️⃣ **Auth Service** (NEW - Quản lý xác thực)
- **Port**: 8082
- **Chức năng**: 
  - Đăng ký người dùng
  - Đăng nhập (tạo JWT token)
  - Validate JWT token
- **Endpoints**:
  - `POST /auth/register` - Đăng ký người dùng mới
  - `POST /auth/login` - Đăng nhập, nhận token
  - `POST /auth/validate` - Kiểm tra token
  - `GET /auth/health` - Health check

### 2️⃣ **User Service** (Quản lý thông tin người dùng)
- **Port**: 8081
- **Chức năng**: 
  - Lấy danh sách users
  - Lấy thông tin user
  - Đăng nhập (tạo token - có thể gọi Auth Service)
- **Endpoints**:
  - `GET /users` - Lấy tất cả users
  - `GET /users/{id}` - Lấy user theo ID
  - `POST /users/login` - Đăng nhập

### 3️⃣ **Order Service** (Quản lý đơn hàng)
- **Port**: 8083
- **Chức năng**: 
  - Tạo đơn hàng
  - Lấy thông tin đơn hàng
  - Cần xác thực trước khi truy cập
- **Endpoints**:
  - `GET /orders` - Lấy tất cả orders
  - `GET /orders/{id}` - Lấy order theo ID

### 4️⃣ **API Gateway** (Định tuyến requests)
- **Port**: 8080
- **Chức năng**: 
  - Định tuyến requests đến đúng service
  - Xử lý exception chung
  - Cho phép/chặn requests dựa vào path

---

## 🚀 Hướng dẫn Khởi động

### Bước 1: Build toàn bộ project

```bash
# Từ thư mục gốc (Spring-gatewat-demo)
mvn clean install
```

### Bước 2: Khởi động các service theo thứ tự

**Terminal 1 - Eureka Server** (Service Discovery)
```bash
mvn spring-boot:run -pl eureka-server
# Khởi động trên http://localhost:8761
```

**Terminal 2 - Auth Service**
```bash
mvn spring-boot:run -pl auth-service
# Khởi động trên http://localhost:8082
```

**Terminal 3 - User Service**
```bash
mvn spring-boot:run -pl user-service
# Khởi động trên http://localhost:8081
```

**Terminal 4 - Order Service**
```bash
mvn spring-boot:run -pl order-service
# Khởi động trên http://localhost:8083
```

**Terminal 5 - API Gateway**
```bash
mvn spring-boot:run -pl api-gateway
# Khởi động trên http://localhost:8080
```

---

## 📡 Ví dụ cách sử dụng

### 1. Đăng ký người dùng mới

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "password123",
    "email": "john@example.com"
  }'
```

**Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "username": "john",
  "message": "User registered successfully"
}
```

### 2. Đăng nhập

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "password123"
  }'
```

### 3. Lấy danh sách users (không cần xác thực)

```bash
curl http://localhost:8080/users
# hoặc
curl http://localhost:8080/api/users
```

### 4. Gọi Order Service với token

```bash
TOKEN="<token_nhận_được_từ_login>"

curl http://localhost:8080/orders \
  -H "Authorization: Bearer $TOKEN"
# hoặc
curl http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN"
```

### 5. Validate token

```bash
curl -X POST http://localhost:8080/auth/validate \
  -H "Content-Type: application/json" \
  -d '{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }'
```

---

## 🔑 JWT Token Giải thích

Một JWT token gồm 3 phần được phân cách bằng dấu `.`:

```
Header.Payload.Signature

eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huIiwiZXhwIjoxNzAyODk0MDAwfQ.signature
```

- **Header**: Loại token (JWT) & thuật toán (HS256)
- **Payload**: Dữ liệu (username, user-id, expiration time)
- **Signature**: Chữ ký để xác minh token không bị sửa đổi

---

## ⚙️ Cấu hình JWT

Tất cả service phải dùng **cùng một secret key** để validate token:

```properties
# File: application.properties của từng service
jwt.secret=mySecretKeyForJWTTokenGenerationAndValidation12345678901234567890
jwt.expiration=3600000  # Token hết hạn sau 1 giờ
```

---

## 🛡️ Security Best Practices

1. **Change Secret Key**: Thay đổi `jwt.secret` thành một key mạnh trong production
2. **Use HTTPS**: Luôn sử dụng HTTPS trong production
3. **Store Token Safely**: Client lưu token ở localStorage hoặc sessionStorage
4. **Token Expiration**: Đặt thời gian hết hạn phù hợp (15 phút - 1 giờ là tốt)
5. **Refresh Token**: Cân nhắc implementing refresh token mechanism

---

## 📁 Cấu trúc Project

```
Spring-gatewat-demo/
├── pom.xml                          # Parent POM (defines modules)
├── eureka-server/                   # Service Discovery
├── api-gateway/                     # API Gateway
├── auth-service/                    # 🆕 Auth Service (NEW)
├── user-service/                    # User Service
└── order-service/                   # Order Service
```

---

## 🐛 Troubleshooting

### Port đã được sử dụng
```bash
# Tìm process chiếm port (ví dụ: 8080)
# Windows
netstat -ano | findstr :8080

# Linux/Mac
lsof -i :8080

# Kill process
# Windows
taskkill /PID <PID> /F

# Linux/Mac
kill -9 <PID>
```

### Không thể kết nối tới Eureka
- Kiểm tra Eureka Server đã chạy trên port 8761
- Kiểm tra `eureka.client.serviceUrl.defaultZone` trong application.properties

### Token không valid
- Kiểm tra `jwt.secret` phải giống nhau ở tất cả service
- Kiểm tra token đã hết hạn chưa (xem expiration time)
- Kiểm tra format header: `Authorization: Bearer <token>`

---

## 📖 Tài liệu thêm

- [Auth Service README](./auth-service/README.md)
- [Spring Cloud Gateway Documentation](https://spring.io/projects/spring-cloud-gateway)
- [JWT Introduction](https://jwt.io)

---

## ✅ Checklist Hoàn tất

- [x] Auth Service được tạo
- [x] Auth Service endpoints: register, login, validate
- [x] JWT token generation & validation
- [x] Gateway routes được cập nhật
- [x] Tất cả service được configure với cùng secret key
- [x] Documentation hoàn tất

**Ready to use!** 🎉

