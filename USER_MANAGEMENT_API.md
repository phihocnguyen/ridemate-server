# User Management & Driver Approval APIs

## Tổng quan

Hệ thống API quản lý người dùng và duyệt tài xế, cho phép:

- Admin quản lý tất cả người dùng (xem, tìm kiếm, lọc, thống kê)
- Admin duyệt/từ chối đơn đăng ký tài xế
- Passenger nộp hồ sơ đăng ký trở thành tài xế
- Admin kích hoạt/vô hiệu hóa tài khoản người dùng

## Các API Endpoints

### 1. Admin - Quản lý người dùng

#### 1.1 Lấy danh sách người dùng (có phân trang & filter)

```http
GET /api/admin/users
Authorization: Bearer {admin_token}
```

**Query Parameters:**

- `userType` (optional): DRIVER | PASSENGER | ADMIN
- `isActive` (optional): true | false
- `driverApprovalStatus` (optional): NONE | PENDING | APPROVED | REJECTED
- `searchTerm` (optional): Tìm theo tên, số điện thoại, email
- `page` (default: 0): Số trang (bắt đầu từ 0)
- `size` (default: 10): Số lượng items mỗi trang
- `sortBy` (default: "createdAt"): Trường để sắp xếp
- `sortDirection` (default: "DESC"): ASC | DESC

**Response Example:**

```json
{
  "code": 200,
  "message": "Users retrieved successfully",
  "data": {
    "users": [
      {
        "id": 1,
        "fullName": "Lê Văn C",
        "phoneNumber": "0912345678",
        "email": "lvc@example.com",
        "profilePictureUrl": "https://...",
        "rating": 3.4,
        "userType": "PASSENGER",
        "driverApprovalStatus": "PENDING",
        "licenseNumber": "C456789012",
        "vehicleInfo": "Vios, Taxi",
        "isActive": true,
        "coins": 0,
        "totalRidesCompleted": 5,
        "acceptanceRate": 0.0,
        "completionRate": 0.0,
        "createdAt": "2024-01-01T10:00:00",
        "updatedAt": "2024-01-01T10:00:00"
      }
    ],
    "currentPage": 0,
    "totalPages": 5,
    "totalElements": 50,
    "pageSize": 10
  }
}
```

#### 1.2 Lấy thống kê người dùng

```http
GET /api/admin/users/statistics
Authorization: Bearer {admin_token}
```

**Response Example:**

```json
{
  "code": 200,
  "message": "Statistics retrieved successfully",
  "data": {
    "totalUsers": 100,
    "totalDrivers": 30,
    "totalPassengers": 68,
    "totalAdmins": 2,
    "activeUsers": 95,
    "inactiveUsers": 5,
    "pendingDriverApprovals": 8,
    "approvedDrivers": 25,
    "rejectedDrivers": 3
  }
}
```

#### 1.3 Lấy thông tin chi tiết người dùng

```http
GET /api/admin/users/{id}
Authorization: Bearer {admin_token}
```

#### 1.4 Lấy danh sách đơn đăng ký tài xế đang chờ duyệt

```http
GET /api/admin/users/pending-drivers
Authorization: Bearer {admin_token}
```

**Response Example:**

```json
{
  "code": 200,
  "message": "Pending drivers retrieved successfully",
  "data": [
    {
      "id": 7,
      "fullName": "Lê Thị G",
      "phoneNumber": "0933444355",
      "email": "ltg@example.com",
      "driverApprovalStatus": "PENDING",
      "licenseNumber": "C456789012",
      "vehicleInfo": "Vios, Taxi",
      "licenseImageUrl": "https://...",
      "vehicleImageUrl": "https://..."
    }
  ]
}
```

#### 1.5 Duyệt đơn đăng ký tài xế

```http
POST /api/admin/users/{id}/approve-driver
Authorization: Bearer {admin_token}
```

**Response:** Thông tin user sau khi được duyệt (userType chuyển thành DRIVER)

#### 1.6 Từ chối đơn đăng ký tài xế

```http
POST /api/admin/users/{id}/reject-driver
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "rejectionReason": "Giấy phép lái xe không hợp lệ"
}
```

**Response:** Thông tin user sau khi bị từ chối

#### 1.7 Cập nhật trạng thái hoạt động của user

```http
PATCH /api/admin/users/{id}/status
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "isActive": false
}
```

**Response:** Thông tin user sau khi cập nhật

---

### 2. User - Đăng ký trở thành tài xế

#### 2.1 Nộp đơn đăng ký tài xế

```http
POST /api/users/driver-application/apply
Authorization: Bearer {user_token}
Content-Type: application/json

{
  "licenseNumber": "C456789012",
  "vehicleInfo": "Toyota Vios 2020, Màu trắng, Biển số: 29A-12345",
  "licenseImageUrl": "https://cloudinary.com/...",
  "vehicleImageUrl": "https://cloudinary.com/..."
}
```

**Validation:**

- Chỉ PASSENGER mới có thể đăng ký
- Không thể đăng ký nếu đã có đơn PENDING
- Không thể đăng ký nếu đã là DRIVER (APPROVED)

**Response Example:**

```json
{
  "code": 200,
  "message": "Driver application submitted successfully",
  "data": {
    "id": 7,
    "fullName": "Lê Thị G",
    "userType": "PASSENGER",
    "driverApprovalStatus": "PENDING",
    "licenseNumber": "C456789012",
    "vehicleInfo": "Toyota Vios 2020, Màu trắng, Biển số: 29A-12345"
  }
}
```

#### 2.2 Kiểm tra trạng thái đơn đăng ký

```http
GET /api/users/driver-application/status
Authorization: Bearer {user_token}
```

**Response:** Thông tin trạng thái đơn đăng ký hiện tại (PENDING/APPROVED/REJECTED)

---

## Database Schema Changes

Các trường mới được thêm vào bảng `users`:

```sql
driver_approval_status VARCHAR(20) DEFAULT 'NONE'
  -- NONE: Chưa đăng ký hoặc là PASSENGER
  -- PENDING: Đang chờ admin duyệt
  -- APPROVED: Đã được duyệt thành DRIVER
  -- REJECTED: Bị từ chối

license_number VARCHAR(50)          -- Số giấy phép lái xe
vehicle_info VARCHAR(500)           -- Thông tin xe
rejection_reason VARCHAR(1000)     -- Lý do từ chối (nếu có)
license_image_url VARCHAR(255)     -- Link ảnh giấy phép
vehicle_image_url VARCHAR(255)     -- Link ảnh xe
```

JPA Hibernate sẽ tự động cập nhật schema khi chạy ứng dụng (đã cấu hình `spring.jpa.hibernate.ddl-auto=update`).

---

## Luồng hoạt động

### Luồng đăng ký tài xế:

1. **Passenger nộp đơn:**

   - POST `/api/users/driver-application/apply`
   - Upload ảnh giấy phép & xe lên Cloudinary trước
   - Gửi licenseNumber, vehicleInfo, và URLs
   - Trạng thái chuyển sang `PENDING`

2. **Admin xem danh sách chờ duyệt:**

   - GET `/api/admin/users/pending-drivers`
   - Xem chi tiết: GET `/api/admin/users/{id}`

3. **Admin quyết định:**

   - **Duyệt:** POST `/api/admin/users/{id}/approve-driver`
     - userType: PASSENGER → DRIVER
     - driverApprovalStatus: PENDING → APPROVED
   - **Từ chối:** POST `/api/admin/users/{id}/reject-driver`
     - driverApprovalStatus: PENDING → REJECTED
     - Lưu rejectionReason

4. **User kiểm tra kết quả:**
   - GET `/api/users/driver-application/status`

---

## Security & Authorization

- **Admin endpoints** (`/api/admin/**`): Yêu cầu role `ADMIN`
- **User endpoints** (`/api/users/**`): Yêu cầu authenticated user
- Sử dụng JWT Bearer token trong header `Authorization`
- Spring Security với `@PreAuthorize` để kiểm soát quyền truy cập

---

## Testing với Swagger

Truy cập: `http://localhost:8080/api/swagger-ui.html`

Tất cả các endpoint đã được document với Swagger annotations để dễ dàng test.

---

## Error Handling

Các lỗi phổ biến:

- **400 Bad Request**: Dữ liệu không hợp lệ
- **401 Unauthorized**: Chưa đăng nhập hoặc token không hợp lệ
- **403 Forbidden**: Không có quyền truy cập
- **404 Not Found**: Không tìm thấy user
- **500 Internal Server Error**: Lỗi server

Response format:

```json
{
  "code": 400,
  "message": "Error message here",
  "data": null
}
```
