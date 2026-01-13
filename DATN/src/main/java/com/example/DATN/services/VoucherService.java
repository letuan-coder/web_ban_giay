package com.example.DATN.services;

import cn.ipokerface.snowflake.SnowflakeIdGenerator;
import com.example.DATN.constant.Util.CheckSumUtil;
import com.example.DATN.dtos.request.voucher.CreateVoucherRequest;
import com.example.DATN.dtos.respone.voucher.VoucherResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.VoucherMapper;
import com.example.DATN.models.Voucher;
import com.example.DATN.models.VoucherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class VoucherService {
    private final VoucherRepository voucherRepository;
    private final CheckSumUtil checkSumUtil;
    private final VoucherMapper voucherMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final String prefix = "VC";
    @Transactional(rollbackFor = Exception.class)
    public VoucherResponse createVoucher (CreateVoucherRequest  request){
        String code =  checkSumUtil.generate(prefix);
        if(request.getUsageLimit()==null) {
            request.setUsageLimit(-1);
        }
        String voucherName = request.getType().format(request.getDiscountValue());
        Voucher voucher = Voucher.builder()
                .voucherCode(code)
                .discountValue(request.getDiscountValue())
                .type(request.getType())
                .apply(request.getApply())
                .target(request.getTarget())
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

    public VoucherResponse findVoucherByCode(String code){
        Voucher voucher = voucherRepository.findByVoucherCode(code)
                .orElseThrow(()->new ApplicationException(ErrorCode.VOUCHER_NOT_FOUND));
        return voucherMapper.toResponse(voucher);
    }

}
