# Nghiệp vụ: Website Bán Quần Áo và Giày Dép

## Mục tiêu
- Quản lý tài khoản người dùng và phân quyền ADMIN/STAFF/USER
- Quản lý sản phẩm: quần áo và giày dép, màu sắc, size, tồn kho theo chi nhánh
- Quản lý đơn hàng với chính sách cọc cho đơn hàng số lượng lớn
- Tích hợp API thanh toán (VNPAY) và giao hàng (Giao Hàng Tiết Kiệm)
- Gửi thông báo cập nhật sản phẩm qua email (Gmail)
- Tích hợp webhook để tư vấn sản phẩm qua messenger và zalo 
- 
## Chi tiết nghiệp vụ

### 1. Quản lý tài khoản người dùng
- Người dùng **truy cập trang chủ** mà không cần đăng nhập.
- Khi **thêm sản phẩm vào giỏ hàng, đặt hàng, hoặc yêu thích sản phẩm**, người dùng bắt buộc đăng nhập.
- Người dùng có thể:
  - Đăng ký / Đăng nhập (JWT authentication)
  - Cập nhật thông tin cá nhân
  - Xem lịch sử đơn hàng
- Phân quyền:
  - **ADMIN**: toàn quyền quản lý
  - **STAFF**: quản lý tồn kho, xử lý đơn hàng
  - **USER**: mua hàng, quản lý giỏ hàng, yêu thích sản phẩm

### 2. Quản lý sản phẩm
- Nhập sản phẩm có thể nhập từ 
- Mỗi sản phẩm có:
  - Tên sản phẩm, loại, thương hiệu
  - Màu sắc, size, giá, hình ảnh
- Tồn kho chi tiết:
  - Mỗi **màu – size** phải có số lượng riêng
  - Theo dõi **tồn kho theo chi nhánh**:
    - Ví dụ: Chi nhánh A có size 38 màu trắng 10 sản phẩm, chi nhánh B có size 38 màu trắng 5 sản phẩm
- Khi STAFF cập nhật số lượng, chỉ cập nhật số lượng của chi nhánh đó

### 3. Chính sách đặt cọc
- Nếu **người dùng mua từ 10 sản phẩm trở lên để đặt ship**, bắt buộc **cọc 50% giá trị đơn hàng**.
- Hệ thống tự tính số tiền cần cọc và hiển thị cho người dùng.

### 4. Tích hợp giao hàng
- Sử dụng **API Giao Hàng Tiết Kiệm**:
  - Tạo đơn hàng vận chuyển tự động khi người dùng đặt hàng
  - Lấy phí ship, mã vận đơn, trạng thái đơn hàng

### 5. Thông báo sản phẩm mới
- Khi ADMIN đăng sản phẩm mới lên bảng tin:
  - Gửi email đến người dùng **dùng Gmail** với thông tin sản phẩm
  - Nội dung email bao gồm: tên sản phẩm, hình ảnh, giá, link sản phẩm

### 6. Thanh toán
- Tích hợp **VNPAY**:
  - Thanh toán toàn bộ hoặc thanh toán cọc
  - Xác thực trạng thái thanh toán từ VNPAY
  - Cập nhật trạng thái đơn hàng sau khi thanh toán thành công

### 7. Cập nhật tồn kho chi nhánh
- Khi STAFF cập nhật số lượng hàng:
  - Chỉ ảnh hưởng số lượng tại chi nhánh đó
  - Hệ thống tổng hợp tồn kho tất cả chi nhánh cho báo cáo

### 8. (Tạm để trống hoặc mở rộng)
- Có thể thêm tính năng: voucher, khuyến mãi, lọc sản phẩm, wishlist, đánh giá sản phẩm, báo cáo doanh thu theo chi nhánh.

### 9 Webhook n8n
  - n8n sẽ quét database
  - Khi khách hàng sử dụng messenger hoặc zalo hỏi tư vấn các sản phẩm thì 
  - Khi thêm khuyến mãi mới thì n8n sẽ tự động gửi mail tới các khách hàng đã đăng ký
  - 
    