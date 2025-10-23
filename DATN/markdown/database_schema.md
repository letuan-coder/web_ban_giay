# Tài liệu Schema Database - Luận văn tốt nghiệp

Đây là tài liệu mô tả cấu trúc database dựa trên file `final_db_luanvan.txt`.
taileduc0404@gmail.com Le Duc Tai

© 2025 Lê Đức Tài.
Tác phẩm/ cơ sở dữ liệu đã đăng ký quyền tác giả. 
Mọi quyền được bảo lưu. Nghiêm cấm sao chép, 
trích dẫn,
phân phối hoặc khai thác dưới mọi hình thức khi chưa có phép bằng văn bản.
---

## Phần 1: Bảng Địa lý (Geography)

### Bảng: `Province`
Lưu trữ thông tin các tỉnh/thành phố.

| Tên cột | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| `Id` | `uuid` | Khóa chính |
| `Name` | `varchar` | |
| `NameEn` | `varchar` | |
| `FullName` | `varchar` | |
| `FullNameEn` | `varchar` | |
| `AdministrativeUnitId` | `varchar` | |
| `CreatedAt` | `timestamp` | |
| `ModifiedAt` | `timestamp` | |
| `Code` | `varchar` | |

### Bảng: `District`
Lưu trữ thông tin các quận/huyện.

| Tên cột | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| `Id` | `uuid` | Khóa chính |
| `Name` | `varchar` | |
| `NameEn` | `varchar` | |
| `FullName` | `varchar` | |
| `FullNameEn` | `varchar` | |
| `AdministrativeUnitId` | `varchar` | |
| `CreatedAt` | `timestamp` | |
| `ModifiedAt` | `timestamp` | |
| `Code` | `varchar` | |
| `ProvinceId` | `uuid` | Khóa ngoại -> `Province.Id` |

### Bảng: `Commune`
Lưu trữ thông tin các xã/phường.

| Tên cột | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| `Id` | `uuid` | Khóa chính |
| `Name` | `varchar` | |
| `NameEn` | `varchar` | |
| `FullName` | `varchar` | |
| `FullNameEn` | `varchar` | |
| `AdministrativeUnitId` | `varchar` | |
| `CreatedAt` | `timestamp` | |
| `ModifiedAt` | `timestamp` | |
| `Code` | `varchar` | |
| `DistrictId` | `uuid` | Khóa ngoại -> `District.Id` |

---

## Phần 2: Xác thực & Phân quyền (Auth & Access Control)

### Bảng: `User`
Lưu trữ thông tin người dùng.

| Tên cột | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| `Id` | `uuid` | Khóa chính |
| `Username` | `varchar` | `unique` |
| `Email` | `varchar` | `unique` |
| `Password` | `varchar` | |
| `Name` | `varchar` | |
| `PhoneNumber` | `varchar` | |
| `DayOfBirth` | `timestamp` | |
| `Avatar` | `varchar` | |
| `Status` | `enum('active', 'inactive', 'banned')` | |
| `UserType` | `int` | |
| `RoleId` | `uuid` | Khóa ngoại -> `Role.Id` |
| `CreatedAt` | `timestamp` | |
| `ModifiedAt` | `timestamp` | |

### Bảng: `Role`
Lưu trữ các vai trò trong hệ thống.

| Tên cột | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| `Id` | `uuid` | Khóa chính |
| `Code` | `varchar` | |
| `Name` | `varchar` | |
| `Description` | `varchar` | |
| `ModelType` | `varchar` | |
| `Guards` | `varchar` | |
| `CreatedAt` | `timestamp` | |

### Bảng: `Permission`
Lưu trữ các quyền hạn chi tiết.

| Tên cột | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| `Code` | `varchar` | Khóa chính |
| `Name` | `varchar` | |
| `ModelType` | `varchar` | |
| `Resource` | `varchar` | |
| `Guards` | `varchar` | |
| `CreatedAt` | `timestamp` | |

### Bảng Quan hệ (Junction Tables)
- **`RolePermission`**: Nối `Role` và `Permission`.
- **`ModelRole`**: Gán vai trò cho một model cụ thể.
- **`ModelPermission`**: Gán quyền cho một model cụ thể.

### Bảng: `UserAddress`
Lưu trữ địa chỉ của người dùng.

| Tên cột | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| `Id` | `uuid` | Khóa chính |
| `UserId` | `uuid` | Khóa ngoại -> `User.Id` |
| `ReceiverName` | `varchar` | |
| `PhoneNumber` | `varchar` | |
| `ProvinceId` | `uuid` | Khóa ngoại -> `Province.Id` |
| `DistrictId` | `uuid` | Khóa ngoại -> `District.Id` |
| `CommuneId` | `uuid` | Khóa ngoại -> `Commune.Id` |
| `StreetDetail` | `varchar` | |
| `IsDefault` | `boolean` | |
| `CreatedAt` | `timestamp` | |
| `ModifiedAt` | `timestamp` | |

### Bảng: `InvalidatedRefeshToken`
Lưu trữ refresh token của hết hạn khi logout

| Tên cột | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| `Id` | `uuid` | Khóa chính |
| `Token` | `varchar` | `unique` |

### Bảng: `UserRefreshToken`
Lưu trữ refresh token của người dùng.

| Tên cột | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| `Id` | `uuid` | Khóa chính |
| `Token` | `varchar` | `unique` |
| `ExpireTime` | `timestamp` | |
| `SessionId` | `uuid` | |
| `UserId` | `uuid` | Khóa ngoại -> `User.Id` |
| `CreatedAt` | `timestamp` | |

---

## Phần 3: Lõi Thương mại điện tử (E-Commerce Core)

### Bảng: `Brand`
Lưu trữ thông tin thương hiệu.

| Tên cột | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| `Id` | `uuid` | Khóa chính |
| `Name` | `varchar` | |
| `Description` | `text` | |
| `CreatedAt` | `timestamp` | |
| `ModifiedAt` | `timestamp` | |

### Bảng: `Category`
Lưu trữ danh mục sản phẩm (hỗ trợ phân cấp).

| Tên cột | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| `Id` | `uuid` | Khóa chính |
| `Name` | `varchar` | |
| `Description` | `text` | |
| `ParentCategoryId` | `uuid` | Khóa ngoại -> `Category.Id` |
| `CreatedAt` | `timestamp` | |
| `ModifiedAt` | `timestamp` | |

### Bảng: `Product`
Lưu trữ thông tin sản phẩm.

| Tên cột | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| `Id` | `uuid` | Khóa chính |
| `Name` | `varchar` | |
| `Slug` | `varchar` | `unique` |
| `Description` | `text` | |
| `BrandId` | `uuid` | Khóa ngoại -> `Brand.Id` |
| `CategoryId` | `uuid` | Khóa ngoại -> `Category.Id` |
| `CreatedAt` | `timestamp` | |
| `ModifiedAt` | `timestamp` | |

### Bảng: `ImageProduct`
Lưu trữ các hình ảnh của sản phẩm.

| Tên cột | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| `Id` | `int` | Khóa chính, Tự tăng |
| `ProductId` | `uuid` | Khóa ngoại -> `Product.Id` |
| `ImageUrl` | `varchar` | |

### Bảng: `ProductVariant`
Lưu trữ các biến thể của sản phẩm (theo size, màu sắc...).

| Tên cột      | Kiểu dữ liệu | Ràng buộc |
|--------------|--------------|---|
| `Id`         | `uuid`       | Khóa chính |
| `ProductId`  | `uuid`       | Khóa ngoại -> `Product.Id` |
| `Size`       | `varchar`    | |
| `Color`      | `varchar`    | |
| `Price`      | `decimal`    | |
| `Stock`      | `int`        | |
| `Branch_id`  | `uuid`       | |
| `Warehouse`  | `uuid`       | |
| `CreatedAt`  | `timestamp`  | |
| `ModifiedAt` | `timestamp`  | |



## Phần 4: Giỏ hàng, Đơn hàng & Thanh toán (Cart, Orders & Payments)

### Bảng: `Cart`
Lưu trữ giỏ hàng của người dùng.

| Tên cột | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| `Id` | `uuid` | Khóa chính |
| `UserId` | `uuid` | Khóa ngoại -> `User.Id` |
| `CreatedAt` | `timestamp` | |
| `ModifiedAt` | `timestamp` | |

### Bảng: `CartItem`
Lưu trữ các sản phẩm trong giỏ hàng.

| Tên cột | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| `CartId` | `uuid` | Khóa ngoại -> `Cart.Id` |
| `ProductId` | `uuid` | Khóa ngoại -> `Product.Id` |
| `Quantity` | `int` | |
| `AddedAt` | `timestamp` | |

### Bảng: `Order`
Lưu trữ thông tin đơn hàng.

| Tên cột | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| `Id` | `uuid` | Khóa chính |
| `UserId` | `uuid` | Khóa ngoại -> `User.Id` |
| `TotalAmount` | `decimal` | |
| `Status` | `enum('pending', 'processing', 'shipped', 'delivered', 'cancelled')` | |
| `PaymentMethodId` | `uuid` | Khóa ngoại -> `PaymentMethod.Id` |
| `CreatedAt` | `timestamp` | |
| `ModifiedAt` | `timestamp` | |

### Bảng: `OrderItem`
Lưu trữ các sản phẩm trong đơn hàng.

| Tên cột | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| `OrderId` | `uuid` | Khóa ngoại -> `Order.Id` |
| `ProductId` | `uuid` | Khóa ngoại -> `Product.Id` |
| `Quantity` | `int` | |
| `UnitPrice` | `decimal` | |
| `CreatedAt` | `timestamp` | |

### Bảng: `OrderShippingAddress`
Lưu trữ địa chỉ giao hàng của đơn hàng tại thời điểm đặt.

| Tên cột | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| `OrderId` | `uuid` | Khóa chính, Khóa ngoại -> `Order.Id` |
| `ReceiverName` | `varchar` | |
| `PhoneNumber` | `varchar` | |
| `ProvinceName` | `varchar` | |
| `DistrictName` | `varchar` | |
| `CommuneName` | `varchar` | |
| `StreetDetail` | `varchar` | |

### Bảng: `PaymentMethod`
Lưu trữ các phương thức thanh toán.

| Tên cột | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| `Id` | `uuid` | Khóa chính |
| `Code` | `varchar` | |
| `Name` | `varchar` | |
| `Description` | `text` | |
| `CreatedAt` | `timestamp` | |

---

## Phần 5: Khuyến mãi & Tin tức (Promotions & Newsletter)

### Bảng: `Promotion`
Lưu trữ thông tin các chương trình khuyến mãi.

| Tên cột | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| `Id` | `uuid` | Khóa chính |
| `Code` | `varchar` | `unique` |
| `Description` | `text` | |
| `DiscountType` | `enum('PERCENT', 'FIXED_AMOUNT')` | |
| `DiscountValue` | `decimal` | |
| `StartAt` | `timestamp` | |
| `EndAt` | `timestamp` | |
| `CreatedAt` | `timestamp` | |

### Bảng: `OrderPromotion`
Nối đơn hàng và khuyến mãi được áp dụng.

| Tên cột | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| `OrderId` | `uuid` | Khóa ngoại -> `Order.Id` |
| `PromotionId` | `uuid` | Khóa ngoại -> `Promotion.Id` |

### Bảng: `NewsletterSubscription`
Lưu trữ email đăng ký nhận tin.

| Tên cột | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| `Email` | `varchar` | Khóa chính |
| `SubscribedAt` | `timestamp` | |
| `IsActive` | `bool` | |

---

## Phần 6: Vận chuyển & Theo dõi (Shipping & Tracking)

### Bảng: `ShippingOrder`
Lưu trữ thông tin vận chuyển của đơn hàng.

| Tên cột | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| `Id` | `uuid` | Khóa chính |
| `OrderId` | `uuid` | Khóa ngoại -> `Order.Id` |
| `ShippingCode` | `varchar` | |
| `ShippingPartner` | `varchar` | |
| `CurrentStatus` | `enum('pending', 'in_transit', 'delivered', 'failed')` | |
| `LastUpdated` | `timestamp` | |
| `RawResponse` | `text` | |

### Bảng: `ShippingStatusLog`
Ghi lại lịch sử các trạng thái vận chuyển.

| Tên cột | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| `Id` | `uuid` | Khóa chính |
| `ShippingOrderId` | `uuid` | Khóa ngoại -> `ShippingOrder.Id` |
| `Status` | `enum('pending', 'in_transit', 'delivered', 'failed')` | |
| `Time` | `timestamp` | |
| `Note` | `text` | |

---

## Phần 7: Đánh giá & Bình luận (Reviews & Ratings)

### Bảng: `TopProduct`
lưu trữ các sản phẩm được yêu thích 
| Tên cột      | Kiểu dữ liệu | Mô tả                                |
| ------------ | ------------ | ------------------------------------ |
| `id`         | UUID / INT   | Khóa chính                           |
| `product_id` | UUID         | Liên kết đến `Product`               |
| `score`      | FLOAT        | Điểm yêu thích (tính theo công thức) |
| `rank`       | INT          | Thứ hạng                             |
| `updated_at` | TIMESTAMP    | Lần cập nhật cuối                    |

### Bảng: `ProductReview`
Lưu trữ đánh giá của người dùng cho sản phẩm.

| Tên cột | Kiểu dữ liệu | Ràng buộc |
|---|---|---|
| `Id` | `uuid` | Khóa chính |
| `ProductId` | `uuid` | Khóa ngoại -> `Product.Id` |
| `UserId` | `uuid` | Khóa ngoại -> `User.Id` |
| `Rating` | `int` | 1-5 sao |
| `Comment` | `text` | |
| `CreatedAt` | `timestamp` | |
| `ModifiedAt` | `timestamp` | |
