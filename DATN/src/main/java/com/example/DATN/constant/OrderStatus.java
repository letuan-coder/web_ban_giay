package com.example.DATN.constant;

public enum OrderStatus {
    PENDING, // Đang chờ xác nhận
    PROCESSCING, // Đang xử lý
    DELEVERING, // Đang giao hàng
    DELEVERED, // Đã giao hàng
    CONFIRMED, // Đã xác nhận
    CANCELLED,// Đã hủy
    COMPLETED // Hoàn thành
}
