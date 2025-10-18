//package com.example.DATN.services;
//
//import com.example.DATN.models.ShippingOrder;
//import com.example.DATN.repositories.ShippingOrderRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
///**
// * Service nghiệp vụ đơn vận chuyển
// */
//@Service
//public class ShippingOrderService {
//    @Autowired
//    private ShippingOrderRepository shippingOrderRepository;
//
//    public List<ShippingOrder> getAllShippingOrders() {
//        return shippingOrderRepository.findAll();
//    }
//
//    public Optional<ShippingOrder> getShippingOrderById(UUID id) {
//        return shippingOrderRepository.findById(id);
//    }
//
//    public ShippingOrder createShippingOrder(ShippingOrder shippingOrder) {
//        return shippingOrderRepository.save(shippingOrder);
//    }
//
//    public ShippingOrder updateShippingOrder(ShippingOrder shippingOrder) {
//        return shippingOrderRepository.save(shippingOrder);
//    }
//
//    public void deleteShippingOrder(UUID id) {
//        shippingOrderRepository.deleteById(id);
//    }
//}
//
