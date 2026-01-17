package com.example.DATN.services;

import cn.ipokerface.snowflake.SnowflakeIdGenerator;
import com.example.DATN.constant.Util.CheckSumUtil;
import com.example.DATN.constant.VoucherClaimStatus;
import com.example.DATN.dtos.request.voucher.CreateVoucherRequest;
import com.example.DATN.dtos.respone.voucher.VoucherResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.GetUserByJwtHelper;
import com.example.DATN.mapper.VoucherMapper;
import com.example.DATN.models.User;
import com.example.DATN.models.Voucher;
import com.example.DATN.models.VoucherClaim;
import com.example.DATN.models.VoucherClaimRepository;
import com.example.DATN.repositories.UserRepository;
import com.example.DATN.repositories.VoucherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class VoucherService {
    private final UserRepository userRepository;
    private final VoucherClaimRepository voucherClaimRepository;
    private final VoucherRepository voucherRepository;
    private final CheckSumUtil checkSumUtil;
    private final VoucherMapper voucherMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final String prefix = "VC";
    private final GetUserByJwtHelper getUserByJwtHelper;

    @Transactional(rollbackFor = Exception.class)
    public VoucherResponse createVoucher(CreateVoucherRequest request) {
        User user = getUserByJwtHelper.getCurrentUser();
        String code = checkSumUtil.generate(prefix);
        if (request.getUsageLimit() == null) {
            request.setUsageLimit(-1);
        }
        String voucherName = request.getType().format(request.getDiscountValue());
        Voucher voucher = Voucher.builder()
                .voucherCode(code)
                .createdBy(user.getUsername())
                .discountValue(request.getDiscountValue())
                .description(voucherName + " cho don " + request.getDiscountValue())
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

    public VoucherResponse findVoucherByCode(String code) {
        Voucher voucher = voucherRepository.findByVoucherCode(code)
                .orElseThrow(() -> new ApplicationException(ErrorCode.VOUCHER_NOT_FOUND));
        return voucherMapper.toResponse(voucher);
    }

    public VoucherClaim AddVoucherForUser(String voucherCode) {
        User user = getUserByJwtHelper.getCurrentUser();
        Voucher voucher = voucherRepository.findByVoucherCode(voucherCode)
                .orElseThrow(() -> new ApplicationException(ErrorCode.VOUCHER_NOT_FOUND));
        boolean existed = voucherClaimRepository
                .existsByUser_IdAndVoucher_Id(user.getId(), voucher.getId());

        if (existed) {
            throw new ApplicationException(ErrorCode.VOUCHER_ALREADY_CLAIMED);
        }
        VoucherClaim claim = VoucherClaim.builder()
                .user(user)
                .voucher(voucher)
                .status(VoucherClaimStatus.CLAIMED)
                .maxUsage(voucher.getUsageLimit())
                .build();
        voucherClaimRepository.save(claim);
        return claim;
    }

    @Transactional
    public boolean claimVoucherForUser(Voucher voucher, User user) {

        int inserted = voucherClaimRepository.insertIgnoreClaim(
                UUID.randomUUID(),
                user.getId(),
                voucher.getId(),
                voucher.getUsageLimit()
        );
        return inserted == 1;
    }

    public List<Voucher> getAllVoucher(){
        return voucherRepository.findAll();
    }
    @Async
    public void voucherClaimForAllUser(String voucherCode) {
        Voucher voucher = voucherRepository.findByVoucherCode(voucherCode)
                .orElseThrow(() -> new ApplicationException(ErrorCode.VOUCHER_NOT_FOUND));

        int page = 0;
        int size = 500;

        Page<User> users;
        do {
            users = userRepository.findAll(PageRequest.of(page, size));
            for (User user : users) {
                claimVoucherForUser(voucher, user);
            }
            page++;
        } while (users.hasNext());
    }

    public List<VoucherClaim> myVoucherClaim() {
        User user = getUserByJwtHelper.getCurrentUser();
        return voucherClaimRepository.findAllByUser_Id(user.getId());
    }

    @Transactional
    @Scheduled(cron = "0 */5 * * * *")
    public void expireVoucherClaims() {
        int affected = voucherClaimRepository.markExpiredClaims();
        if (affected > 0) {
            log.info("[VoucherScheduler] Expired {} voucher claims", affected);
        }
    }


}
