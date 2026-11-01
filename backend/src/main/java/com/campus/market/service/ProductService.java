package com.campus.market.service;

import com.campus.market.common.BizException;
import com.campus.market.common.PageResult;
import com.campus.market.constant.ProductStatus;
import com.campus.market.dto.ProductQuery;
import com.campus.market.dto.ProductRequest;
import com.campus.market.entity.Category;
import com.campus.market.entity.Product;
import com.campus.market.mapper.CategoryMapper;
import com.campus.market.mapper.ProductImageMapper;
import com.campus.market.mapper.ProductMapper;
import com.campus.market.vo.ProductVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class ProductService {

    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final CategoryMapper categoryMapper;
    private final HotProductService hotProductService;

    public ProductService(ProductMapper productMapper,
                          ProductImageMapper productImageMapper,
                          CategoryMapper categoryMapper,
                          HotProductService hotProductService) {
        this.productMapper = productMapper;
        this.productImageMapper = productImageMapper;
        this.categoryMapper = categoryMapper;
        this.hotProductService = hotProductService;
    }

    public PageResult<ProductVO> list(ProductQuery query) {
        query.normalize();
        long total = productMapper.countPage(query);
        List<ProductVO> records = total == 0 ? List.of() : productMapper.selectPage(query);
        return PageResult.of(records, total, query.getPage(), query.getSize());
    }

    public PageResult<ProductVO> listMine(Long sellerId, ProductQuery query) {
        query.setSellerId(sellerId);
        query.normalize();
        long total = productMapper.countPage(query);
        List<ProductVO> records = total == 0 ? List.of() : productMapper.selectPage(query);
        return PageResult.of(records, total, query.getPage(), query.getSize());
    }

    public ProductVO detail(Long id, boolean increaseView) {
        ProductVO product = productMapper.selectDetail(id);
        if (product == null) {
            throw new BizException(404, "商品不存在或已下架");
        }
        if (increaseView) {
            productMapper.incrementViewCount(id);
            product.setViewCount(product.getViewCount() + 1);
        }
        product.setImages(productImageMapper.selectUrlsByProductId(id));
        return product;
    }

    @Transactional
    public ProductVO create(Long sellerId, ProductRequest request) {
        validateCategory(request.categoryId());

        Product product = new Product();
        product.setTitle(request.title());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setOriginalPrice(request.originalPrice());
        product.setCategoryId(request.categoryId());
        product.setSellerId(sellerId);
        product.setConditionLevel(request.conditionLevel());
        product.setStatus(ProductStatus.ON_SALE);
        List<String> images = request.images() == null ? List.of() : request.images();
        product.setCover(images.isEmpty() ? null : images.get(0));
        productMapper.insert(product);

        if (!images.isEmpty()) {
            productImageMapper.batchInsert(product.getId(), images);
        }
        hotProductService.evict();
        return detail(product.getId(), false);
    }

    @Transactional
    public ProductVO update(Long sellerId, Long id, ProductRequest request) {
        ProductVO existing = productMapper.selectDetail(id);
        if (existing == null) {
            throw new BizException(404, "商品不存在");
        }
        if (!Objects.equals(existing.getSellerId(), sellerId)) {
            throw new BizException(403, "只能编辑自己发布的商品");
        }
        if (!ProductStatus.ON_SALE.equals(existing.getStatus())
                && !ProductStatus.OFF_SHELF.equals(existing.getStatus())) {
            throw new BizException("交易中或已售出的商品不能编辑");
        }
        validateCategory(request.categoryId());

        Product product = new Product();
        product.setId(id);
        product.setSellerId(sellerId);
        product.setTitle(request.title());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setOriginalPrice(request.originalPrice());
        product.setCategoryId(request.categoryId());
        product.setConditionLevel(request.conditionLevel());
        if (request.images() != null) {
            product.setCover(request.images().isEmpty() ? null : request.images().get(0));
        } else {
            product.setCover(existing.getCover());
        }
        productMapper.update(product);

        if (request.images() != null) {
            productImageMapper.deleteByProductId(id);
            if (!request.images().isEmpty()) {
                productImageMapper.batchInsert(id, request.images());
            }
        }
        hotProductService.evict();
        return detail(id, false);
    }

    public ProductVO updateStatus(Long sellerId, Long id, String status) {
        if (!ProductStatus.ON_SALE.equals(status) && !ProductStatus.OFF_SHELF.equals(status)) {
            throw new BizException("只支持上架或下架操作");
        }
        int rows = productMapper.updateStatus(id, sellerId, status);
        if (rows == 0) {
            throw new BizException(403, "无权操作，或商品处于交易中/已售出状态");
        }
        hotProductService.evict();
        return detail(id, false);
    }

    @Transactional
    public void delete(Long sellerId, Long id) {
        int rows = productMapper.delete(id, sellerId);
        if (rows == 0) {
            throw new BizException(403, "无权删除，或商品处于交易中/已售出状态");
        }
        productImageMapper.deleteByProductId(id);
        hotProductService.evict();
    }

    private void validateCategory(Long categoryId) {
        boolean exists = categoryMapper.findAll().stream()
                .map(Category::getId)
                .anyMatch(categoryId::equals);
        if (!exists) {
            throw new BizException("商品分类不存在");
        }
    }
}
