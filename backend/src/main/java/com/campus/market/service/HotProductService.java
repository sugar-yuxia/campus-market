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
import java.util.Map;
import java.util.stream.Collectors;

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
        List<ProductVO> hot = null;
        try {
            String cached = redisTemplate.opsForValue().get(HOT_KEY);
            if (cached != null) {
                hot = objectMapper.readValue(cached, LIST_TYPE);
            }
        } catch (Exception e) {
            log.warn("读取 Redis 热门商品缓存失败，降级查询数据库：{}", e.getMessage());
        }

        if (hot == null) {
            hot = productMapper.selectHot(limit);
            try {
                redisTemplate.opsForValue().set(HOT_KEY, objectMapper.writeValueAsString(hot), ttl);
            } catch (Exception e) {
                log.warn("写入 Redis 热门商品缓存失败：{}", e.getMessage());
            }
        }

        refreshViewCounts(hot);
        return hot;
    }

    /**
     * 浏览数高频变更，不值得为了一次浏览就失效整个缓存；
     * 缓存负责"哪些商品上榜"（联表/排序的重查询），浏览数用点查实时补齐。
     */
    private void refreshViewCounts(List<ProductVO> hot) {
        if (hot.isEmpty()) {
            return;
        }
        Map<Long, Integer> latest = productMapper.selectViewCounts(
                        hot.stream().map(ProductVO::getId).toList())
                .stream()
                .collect(Collectors.toMap(ProductVO::getId, ProductVO::getViewCount));
        hot.forEach(p -> {
            Integer v = latest.get(p.getId());
            if (v != null) {
                p.setViewCount(v);
            }
        });
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
