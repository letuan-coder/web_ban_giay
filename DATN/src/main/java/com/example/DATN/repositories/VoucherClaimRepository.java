package com.example.DATN.repositories;

import com.example.DATN.models.VoucherClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    @Query(value = """
    INSERT IGNORE INTO voucher_claims
    (id, user_id, voucher_id, status, max_usage, used_count)
    VALUES (:id, :userId, :voucherId, 'CLAIMED', :maxUsage, 0)
    """, nativeQuery = true)
    int insertIgnoreClaim(
            @Param("id") UUID id,
            @Param("userId") Long userId,
            @Param("voucherId") UUID voucherId,
            @Param("maxUsage") Integer maxUsage
    );
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


//    @Modifying
//    @Transactional
//    @Query(value = """
//        INSERT IGNORE INTO voucher_claims
//        (id, user_id, voucher_id, status, max_usage, used_count)
//        VALUES (UUID_TO_BIN(UUID()), :userId, :voucherId, 'CLAIMED', :maxUsage, 0)
//        """, nativeQuery = true)
//    int insertIgnoreClaim(
//            @Param("userId") Long userId,
//            @Param("voucherId") UUID voucherId,
//            @Param("maxUsage") Integer maxUsage
//    );

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