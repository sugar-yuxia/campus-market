package com.campus.market.controller;

import com.campus.market.auth.CurrentUser;
import com.campus.market.common.PageResult;
import com.campus.market.common.Result;
import com.campus.market.dto.ProductQuery;
import com.campus.market.dto.ProductRequest;
import com.campus.market.dto.ProductStatusRequest;
import com.campus.market.service.HotProductService;
import com.campus.market.service.ProductService;
import com.campus.market.vo.ProductVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final HotProductService hotProductService;

    public ProductController(ProductService productService, HotProductService hotProductService) {
        this.productService = productService;
        this.hotProductService = hotProductService;
    }

    /**
     * 商品列表：支持关键词搜索、分类筛选、排序与分页。
     */
    @GetMapping
    public Result<PageResult<ProductVO>> list(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) Long categoryId,
                                             @RequestParam(defaultValue = "newest") String sort) {
        ProductQuery query = new ProductQuery();
        query.setPage(page);
        query.setSize(size);
        query.setKeyword(keyword);
        query.setCategoryId(categoryId);
        query.setSort(sort);
        return Result.ok(productService.list(query));
    }

    @GetMapping("/hot")
    public Result<List<ProductVO>> hot() {
        return Result.ok(hotProductService.listHot());
    }

    @GetMapping("/mine")
    public Result<PageResult<ProductVO>> mine(@CurrentUser Long userId,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(required = false) String status) {
        ProductQuery query = new ProductQuery();
        query.setPage(page);
        query.setSize(size);
        query.setStatus(status);
        return Result.ok(productService.listMine(userId, query));
    }

    @GetMapping("/{id}")
    public Result<ProductVO> detail(@PathVariable Long id) {
        return Result.ok(productService.detail(id, true));
    }

    @PostMapping
    public Result<ProductVO> create(@CurrentUser Long userId,
                                    @Valid @RequestBody ProductRequest request) {
        return Result.ok(productService.create(userId, request));
    }

    @PutMapping("/{id}")
    public Result<ProductVO> update(@CurrentUser Long userId,
                                    @PathVariable Long id,
                                    @Valid @RequestBody ProductRequest request) {
        return Result.ok(productService.update(userId, id, request));
    }

    @PutMapping("/{id}/status")
    public Result<ProductVO> updateStatus(@CurrentUser Long userId,
                                          @PathVariable Long id,
                                          @Valid @RequestBody ProductStatusRequest request) {
        return Result.ok(productService.updateStatus(userId, id, request.status()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@CurrentUser Long userId, @PathVariable Long id) {
        productService.delete(userId, id);
        return Result.ok();
    }
}
