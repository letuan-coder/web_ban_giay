---
title: Quy Trình Đăng Nhập Người Dùng
---
graph TD
    A[Bắt đầu] --> B{Người dùng truy cập trang Đăng nhập};

    B --> C[Hệ thống hiển thị biểu mẫu Đăng nhập];
    C --> D[Người dùng nhập Email/Tên đăng nhập và Mật khẩu];
    D --> E{Người dùng nhấn nút Đăng nhập};

    E --> F[Hệ thống nhận yêu cầu Đăng nhập];
    F --> G{Xác thực thông tin đăng nhập?};

    G -- Có --> H[Đăng nhập thành công];
    H --> I[Chuyển hướng đến Trang chủ / Dashboard];
    I --> Z[Kết thúc];

    G -- Không --> J[Hiển thị thông báo lỗi (VD: Sai tài khoản hoặc mật khẩu)];
    J --> K{Người dùng muốn thử lại?};
    K -- Có --> D;
    K -- Không --> Z;

    D -- Quên mật khẩu? --> L[Truy cập chức năng Quên mật khẩu];
    L --> M[Kết thúc quy trình Quên mật khẩu];
    M --> Z;
