package com.example.DATN.services;

import com.example.DATN.constant.Util.CheckSumUtil;
import com.example.DATN.dtos.request.voucher.CreateVoucherRequest;
import com.example.DATN.dtos.respone.voucher.VoucherResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.GetUserByJwtHelper;
import com.example.DATN.mapper.VoucherMapper;
import com.example.DATN.models.User;
import com.example.DATN.models.Voucher;
import com.example.DATN.repositories.UserRepository;
import com.example.DATN.repositories.VoucherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class VoucherService {
    private final UserRepository userRepository;
    private final VoucherRepository voucherRepository;
    private final CheckSumUtil checkSumUtil;
    private final VoucherMapper voucherMapper;
    private final String prefix = "VC";
    private final GetUserByJwtHelper getUserByJwtHelper;

    @Transactional(rollbackFor = Exception.class)
    public VoucherResponse createVoucher(CreateVoucherRequest request) {
        User user = getUserByJwtHelper.getCurrentUser();
        String code = checkSumUtil.generate(prefix);
        if (request.getUsageLimit() == null) {
            request.setUsageLimit(-1);
        }
        String voucherName = request.getType()
                .format(request.getDiscountValue(), request.getMaxDiscountValue());
        Voucher voucher = Voucher.builder()
                .voucherCode(code)
                .createdBy(user.getUsername())
                .discountValue(request.getDiscountValue())
                .description(voucherName)
                .type(request.getType())
                .apply(request.getApply())
                .target(request.getTarget())
                .maxDiscountValue(request.getMaxDiscountValue())
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .minOrderValue(request.getMinOrderValue())
                .isActive(request.getIsActive())
                .voucherName(voucherName)
                .usageLimit(request.getUsageLimit())
                .build();
        voucherRepository.save(voucher);
        return voucherMapper.toResponse(voucher);
    }


    public VoucherResponse findVoucherByCode(String code) {
        Voucher voucher = voucherRepository.findByVoucherCode(code)
                .orElseThrow(() -> new ApplicationException(ErrorCode.VOUCHER_NOT_FOUND));
        return voucherMapper.toResponse(voucher);
    }


    public List<Voucher> getAllVoucher() {
        return voucherRepository.findAll();
    }


}
