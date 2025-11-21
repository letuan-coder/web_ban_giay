package com.example.DATN.services;

import com.example.DATN.constant.RefundStatus;
import com.example.DATN.dtos.request.RefundRequest;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.GetUserByJwtHelper;
import com.example.DATN.models.Refund;
import com.example.DATN.models.User;
import com.example.DATN.repositories.RefundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RefundService {
    private final RefundRepository refundRepository;
     private  final GetUserByJwtHelper getUserByJwtHelper;
    @Transactional
    public void createRefundRequest(RefundRequest request) {
        User user = getUserByJwtHelper.getCurrentUser();
        if(request.getOrder().getTotal_price().compareTo(request.getAmount())<0){
            throw new ApplicationException(ErrorCode.REFUND_NOT_ALLOWDED);

        }
        Refund refund = Refund.builder()
                .expectedRefundDate(request.getExpectedRefundDate())
                .amount(request.getAmount())
                .reason(request.getReason())
                .status(RefundStatus.PENDING)
                .orderReturn(request.getOrderReturn())
                .order(request.getOrder())
                .build();
        log.info("Creating refund request: {}", refund );
        log.info("Refund creating by : {}", user.getId() );
        refundRepository.save(refund);
    }
}
