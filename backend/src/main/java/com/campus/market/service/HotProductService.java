package com.campus.market.service;

import com.campus.market.common.BizException;
import com.campus.market.dto.ProductQuery;
import com.campus.market.mapper.ProductMapper;
import com.campus.market.vo.ProductVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * 热门商品缓存：优先读 Redis，Redis 不可用时自动降级为直接查库，保证首页可用。
 */
@Service
public class HotProductService {

    private static final Logger log = LoggerFactory.getLogger(HotProductService.class);
    private static final String HOT_KEY = "campus:market:hot:products";
    private static final TypeReference<List<ProductVO>> LIST_TYPE = new TypeReference<>() {
    };

    private final ProductMapper productMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final int limit;
    private final Duration ttl;

    public HotProductService(ProductMapper productMapper,
                             StringRedisTemplate redisTemplate,
                             ObjectMapper objectMapper,
                             @Value("${app.hot-products.limit:8}") int limit,
                             @Value("${app.hot-products.ttl-minutes:5}") long ttlMinutes) {
        this.productMapper = productMapper;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.limit = limit;
        this.ttl = Duration.ofMinutes(ttlMinutes);
    }

    public List<ProductVO> listHot() {
        try {
            String cached = redisTemplate.opsForValue().get(HOT_KEY);
            if (cached != null) {
                return objectMapper.readValue(cached, LIST_TYPE);
            }
        } catch (Exception e) {
            log.warn("读取 Redis 热门商品缓存失败，降级查询数据库：{}", e.getMessage());
        }

        List<ProductVO> hot = productMapper.selectHot(limit);
        try {
            redisTemplate.opsForValue().set(HOT_KEY, objectMapper.writeValueAsString(hot), ttl);
        } catch (Exception e) {
            log.warn("写入 Redis 热门商品缓存失败：{}", e.getMessage());
        }
        return hot;
    }

    /**
     * 商品或订单状态变化后清除缓存，避免首页展示过期数据。
     */
    public void evict() {
        try {
            redisTemplate.delete(HOT_KEY);
        } catch (Exception e) {
            log.warn("清除 Redis 热门商品缓存失败：{}", e.getMessage());
        }
    }
}
