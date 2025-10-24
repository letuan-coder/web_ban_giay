package com.example.DATN.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Auth & User
    ROLE_NOT_FOUND(1036,"ROLE NOT FOUND",HttpStatus.NOT_FOUND),
    EXPIRED_TOKEN(1001, "TOKEN EXPIRED", HttpStatus.UNAUTHORIZED),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1007, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    INVALID_DOB(1008, "Your age must be at least 12  ", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(1009, "Email is not valid", HttpStatus.BAD_REQUEST),
    FIRST_NAME_REQUIRED(1010, "First name is required", HttpStatus.BAD_REQUEST),
    LAST_NAME_REQUIRED(1011, "Last name is required", HttpStatus.BAD_REQUEST),
    PASSWORD_REQUIRED(1012, "Password is required", HttpStatus.BAD_REQUEST),
    USERNAME_ALREADY_EXISTS(1013, "Username is already existed", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXISTS(1026, "Email already exists", HttpStatus.BAD_REQUEST), // Re-numbered
    ACCESS_DENIED(1014, "You do not have permission", HttpStatus.FORBIDDEN),
    ROLE_NOT_EXIST(1015, "Role not exist",HttpStatus.NOT_FOUND),

    // Validation
    INVALID_VALIDATION(1016, "Invalid validation", HttpStatus.BAD_REQUEST),

    // Product
    PRODUCT_NOT_AVAILABLE(1099,"product is not available",HttpStatus.BAD_REQUEST),
    INVALID_HEX_CODE(1038, "Invalid hex color code", HttpStatus.BAD_REQUEST),
    HEXCODE_ALREADY_EXISTS  (1037, "Hex code already exists", HttpStatus.BAD_REQUEST),
    SIZE_CANNOT_BE_EMPTY(1036, "Size cannot be empty", HttpStatus.BAD_REQUEST),
    PRODUCT_COLOR_OR_SIZE_EXISTED(1035, "Product color or size already existed", HttpStatus.BAD_REQUEST),
    PRODUCT_ID_REQUIRED(1028, "Product ID is required", HttpStatus.BAD_REQUEST), // Re-numbered
    CATEGORY_ID_REQUIRED(1029, "Category ID is required", HttpStatus.BAD_REQUEST), // Re-numbered
    BRAND_ID_REQUIRED(1030, "Brand ID is required", HttpStatus.BAD_REQUEST),
    PRICE_REQUIRED(1031, "Price is required", HttpStatus.BAD_REQUEST),
    COLOR_REQUIRED(1033, "Color is required", HttpStatus.BAD_REQUEST),
    SIZE_REQUIRED(1034, "Size is required", HttpStatus.BAD_REQUEST),
    PRODUCT_NAME_REQUIRED(1028, "Product name is required", HttpStatus.BAD_REQUEST), // Re-numbered
    STOCK_REQUIRED(1027, "Stock is required", HttpStatus.BAD_REQUEST), // Re-numbered
    PRODUCT_ALREADY_EXISTED(1002, "Product already existed", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_FOUND(1017, "Product not found", HttpStatus.NOT_FOUND),
    BRAND_NOT_FOUND(1018, "Brand not found", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND(1019, "Category not found", HttpStatus.NOT_FOUND),
    PRODUCT_VARIANT_NOT_FOUND(1021, "Product variant not found", HttpStatus.NOT_FOUND),
    PRODUCT_VARIANT_ALREADY_EXISTED(1022, "Product variant with this color already exists for the product", HttpStatus.BAD_REQUEST),
    PRODUCT_PRICE_REQUIRED(1040, "Product price is required", HttpStatus.BAD_REQUEST),
    COLOR_NOT_FOUND(1041, "Color not found", HttpStatus.NOT_FOUND),
    SIZE_NOT_FOUND(1042, "Size not found", HttpStatus.NOT_FOUND),
    PRODUCT_COLOR_NOT_FOUND(1046, "Product color not found", HttpStatus.NOT_FOUND),
    PRODUCT_COLOR_EXISTED(1047, "Product color already exists", HttpStatus.BAD_REQUEST),

    // File & Image
    FILE_EMPTY(1006, "File is empty", HttpStatus.BAD_REQUEST),
    IMAGE_NOT_FOUND(1020, "Image not found", HttpStatus.NOT_FOUND),
    FILE_SIZE_EXCEEDED(1023, "File size must be 10mb", HttpStatus.BAD_REQUEST),
    FILE_COUNT_EXCEEDED(1024, "Product have 5 image", HttpStatus.BAD_REQUEST),
    INVALID_FILE_TYPE(1032, "Invalid file type", HttpStatus.BAD_REQUEST),

    // Newsletter
    NEWSLETTER_SUBSCRIPTION_NOT_FOUND(1025, "Newsletter subscription not found", HttpStatus.NOT_FOUND), // Re-numbered

    //CART
    OUT_OF_STOCK(1028,"out of stock",HttpStatus.BAD_REQUEST),
    CART_ITEM_NOT_FOUND(1027,"item is empty", HttpStatus.NOT_FOUND),
    CART_NOT_FOUND(1026,"cart is not exsited",HttpStatus.NOT_FOUND),

    // Order
    ORDER_NOT_FOUND(1043, "Order not found", HttpStatus.NOT_FOUND),
    PAYMENT_METHOD_NOT_FOUND(1044, "Payment method not found", HttpStatus.NOT_FOUND),
    INSUFFICIENT_STOCK(1045, "Insufficient stock", HttpStatus.BAD_REQUEST),

    // Generic
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR)
    ;

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
