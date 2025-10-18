//package com.example.DATN.services;
//
//import com.example.DATN.models.Promotion;
//import com.example.DATN.repositories.PromotionRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import java.util.List;
//import java.util.Optional;
//
///**
// * Service nghiệp vụ khuyến mãi
// */
//@Service
//public class PromotionService {
//    @Autowired
//    private PromotionRepository promotionRepository;
//
//    public List<Promotion> getAllPromotions() {
//        return promotionRepository.findAll();
//    }
//
//    public Optional<Promotion> getPromotionById(Long id) {
//        return promotionRepository.findById(id);
//    }
//
//    public Promotion createPromotion(Promotion promotion) {
//        return promotionRepository.save(promotion);
//    }
//
//    public Promotion updatePromotion(Promotion promotion) {
//        return promotionRepository.save(promotion);
//    }
//
//    public void deletePromotion(Long id) {
//        promotionRepository.deleteById(id);
//    }
//}
//
