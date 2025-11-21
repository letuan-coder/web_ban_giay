package com.example.DATN.constant;

public enum OrderStatus {
    UNPAID, // Chưa thanh toán
    PAID, // Đã thanh toán
    PENDING, // Đang chờ xác nhận
    CONFIRMED, // Đã xác nhận
    DELIVERING, // Chuẩn bị hàng -> Shipper nhận hàng
    DELIVERED, // Đã giao hàng
    CANCELLED,// Đã hủy
    COMPLETED // Hoàn thành
}
