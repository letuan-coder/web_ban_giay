package com.example.DATN.models;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VoucherClaimRepository extends JpaRepository<VoucherClaim, UUID> {
    Boolean existsByUser_IdAndVoucher_Id(Long userId, UUID voucherCode);

    List<VoucherClaim> findAllByUser_Id(Long userId);

    Optional<VoucherClaim> findByUser_IdAndVoucher_Id(Long userId, UUID voucherCode);

    @Modifying
    @Query("""
                UPDATE VoucherClaim vc
                SET vc.status = 'EXPIRED'
                WHERE vc.status = 'CLAIMED'
                  AND vc.voucher.endAt <= CURRENT_TIMESTAMP
            """)
    int markExpiredClaims();

    @Modifying
    @Query("""
                UPDATE VoucherClaim c
                SET c.usedCount = c.usedCount + 1,
                    c.status ='USED'
                WHERE c.id = :claimId
                  AND c.status = 'CLAIMED'
                  AND c.usedCount < c.maxUsage
            """)
    int lockVoucherClaim(@Param("claimId") UUID claimId);

    @Modifying
    @Query("""
                UPDATE VoucherClaim c
                SET c.usedCount = c.usedCount - 1,
                    c.status = com.example.DATN.constant.VoucherClaimStatus.CLAIMED
                WHERE c.id = :claimId
                  AND c.usedCount > 0
            """)
    int unlockVoucherClaim(@Param("claimId") UUID claimId);
}