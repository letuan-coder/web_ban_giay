package com.example.DATN.exception;

import com.example.DATN.dtos.respone.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions
            (MethodArgumentNotValidException ex) {
        String enumKey = ex.getFieldError().getDefaultMessage();
        ErrorCode errorCode = ErrorCode.INVALID_VALIDATION;
        errorCode = ErrorCode.valueOf(enumKey);

        return ResponseEntity.badRequest().body(
                ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiResponse> handleMultipartException(org.springframework.web.multipart.MultipartException ex) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(ErrorCode.FILE_SIZE_EXCEEDED.getCode());
        apiResponse.setMessage(ErrorCode.FILE_SIZE_EXCEEDED.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponse> handleApplicationException(ApplicationException ex) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(ex.getErrorCode().getCode());
        apiResponse.setMessage(ex.getErrorCode().getMessage());
        return ResponseEntity
                .status(ex.getErrorCode().getStatusCode())
                .body(apiResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorCode errorCode = ErrorCode.ACCESS_DENIED;
        return ResponseEntity.status(errorCode.getStatusCode()).body(
                ApiResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleUncaughtException(Exception ex) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());
        return ResponseEntity.internalServerError().body(apiResponse);
    }

}


