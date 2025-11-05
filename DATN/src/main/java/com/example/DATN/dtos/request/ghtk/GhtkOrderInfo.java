package com.example.DATN.dtos.request.ghtk;

import lombok.Data;

import java.util.List;

@Data
public class GhtkOrderInfo {
    // --------------------- Thông tin người gửi ---------------------
    /** Mã đơn hàng của bạn */
    private String id;

    /** Tên người gửi (shop) */
    private String pick_name;

    /** Địa chỉ người gửi */
    private String pick_address;

    /** Tỉnh thành người gửi */
    private String pick_province;

    /** Quận huyện người gửi */
    private String pick_district;

    /** Phường xã người gửi */
    private String pick_ward;

    /** Số điện thoại người gửi */
    private String pick_tel;

    // --------------------- Thông tin người nhận ---------------------
    /** Số điện thoại người nhận */
    private String tel;

    /** Tên người nhận */
    private String name;

    /** Địa chỉ người nhận */
    private String address;

    /** Tỉnh thành người nhận */
    private String province;

    /** Quận huyện người nhận */
    private String district;

    /** Phường xã người nhận */
    private String ward;

    /** Khối lượng đơn hàng (gram) */
    private double total_weight;

    /** Thôn xóm người nhận (nếu có) */
    private String hamlet;

    // --------------------- Thông tin đơn hàng ---------------------
    /** 1: miễn phí ship, 0: không miễn phí */
    private String is_freeship;

    /** Ngày lấy hàng (yyyy-MM-dd) */
    private String pick_date;

    /** Số tiền thu hộ (COD) */
    private double pick_money;

    /** Ghi chú thêm về đơn hàng */
    private String note;

    /** Giá trị đơn hàng (dùng để tính bảo hiểm) */
    private double value;

    /** Phương thức vận chuyển ("fly" = giao nhanh) */
    private String transport;

    /** Tùy chọn lấy hàng ("cod" = thu tiền hộ, bắt buộc cho xfast) */
    private String pick_option;

    /** Danh sách giải pháp giao hàng (GAM) */
    private List<GamSolution> gam_solutions;
}
