//package com.example.DATN.repositories.redis;
//
//import com.example.DATN.models.ProductRedis;
//import com.redis.om.spring.annotations.Query;
//import com.redis.om.spring.repository.RedisDocumentRepository;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.UUID;
//
//@Repository
//public interface ProductRedisRepository extends RedisDocumentRepository<ProductRedis, UUID> {
//    @Query("(@name:*${keyword}*) | (@productCode:*${keyword}*)")
//    List<ProductRedis> searchByName(@Param("keyword") String keyword);
//}
