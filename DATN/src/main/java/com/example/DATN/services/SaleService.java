package com.example.DATN.services;

import com.example.DATN.dtos.respone.sale.SaleResponse;
import com.example.DATN.models.Vnpay;
import com.example.DATN.repositories.OrderRepository;
import com.example.DATN.repositories.VnpayRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SaleService {
    private final VnpayRepository vnpayRepository;
    private final OrderRepository orderRepository;

    public SaleResponse getDataChart() {


        Map<String, BigDecimal> dailyTemp = new HashMap<>();
        Map<String, BigDecimal> monthlyTemp = new HashMap<>();
        Map<String, BigDecimal> yearlyTemp = new HashMap<>();
        SaleResponse saleResponse = new SaleResponse();

        List<Vnpay> vnpayTransactions = vnpayRepository.findAll();

        BigDecimal totalVnpayAmount = BigDecimal.ZERO;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        for (Vnpay vnpay : vnpayTransactions) {
            try {
                BigDecimal amount = new BigDecimal(vnpay.getVnp_Amount());
//                        .divide(new BigDecimal(100), 0, RoundingMode.DOWN);
                totalVnpayAmount = totalVnpayAmount.add(amount);

                LocalDateTime payDate = LocalDateTime.parse(vnpay.getVnp_PayDate(), formatter);

                String dayKey = payDate.toLocalDate().toString(); // YYYY-MM-DD
                String monthKey = payDate.getYear() + "-" + String.format("%02d", payDate.getMonthValue()); // YYYY-MM
                String yearKey = String.valueOf(payDate.getYear()); // YYYY

                dailyTemp.put(dayKey, dailyTemp.getOrDefault(dayKey, BigDecimal.ZERO).add(amount));
                monthlyTemp.put(monthKey, monthlyTemp.getOrDefault(monthKey, BigDecimal.ZERO).add(amount));
                yearlyTemp.put(yearKey, yearlyTemp.getOrDefault(yearKey, BigDecimal.ZERO).add(amount));

            } catch (Exception e) {
                log.error("Error parsing Vnpay transaction ID {}: {}", vnpay.getId(), e.getMessage());
            }
        }

        saleResponse.setDaily(new TreeMap<>(dailyTemp));
        saleResponse.setMonthly(new TreeMap<>(monthlyTemp));
        saleResponse.setYearly(new TreeMap<>(yearlyTemp));
        saleResponse.setTotal_Amount(totalVnpayAmount);
        return saleResponse;
    }
}
