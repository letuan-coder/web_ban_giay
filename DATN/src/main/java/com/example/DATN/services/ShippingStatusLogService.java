//package com.example.DATN.services;
//
//import com.example.DATN.models.ShippingStatusLog;
//import com.example.DATN.repositories.ShippingStatusLogRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import java.util.List;
//import java.util.Optional;
//
///**
// * Service nghiệp vụ lịch sử trạng thái vận chuyển
// */
//@Service
//public class ShippingStatusLogService {
//    @Autowired
//    private ShippingStatusLogRepository shippingStatusLogRepository;
//
//    public List<ShippingStatusLog> getAllShippingStatusLogs() {
//        return shippingStatusLogRepository.findAll();
//    }
//
//    public Optional<ShippingStatusLog> getShippingStatusLogById(Long id) {
//        return shippingStatusLogRepository.findById(id);
//    }
//
//    public ShippingStatusLog createShippingStatusLog(ShippingStatusLog log) {
//        return shippingStatusLogRepository.save(log);
//    }
//
//    public ShippingStatusLog updateShippingStatusLog(ShippingStatusLog log) {
//        return shippingStatusLogRepository.save(log);
//    }
//
//    public void deleteShippingStatusLog(Long id) {
//        shippingStatusLogRepository.deleteById(id);
//    }
//}
//
