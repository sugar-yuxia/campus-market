package com.campus.market.dto;

/**
 * 商品列表查询条件，供 MyBatis 分页查询使用。
 */
public class ProductQuery {

    private int page = 1;
    private int size = 10;
    private String keyword;
    private Long categoryId;
    private Long sellerId;
    private String status;
    private String sort = "newest";

    public int getOffset() {
        return (page - 1) * size;
    }

    /**
     * 统一收敛分页参数，避免出现非法值。
     */
    public void normalize() {
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
        if (size > 50) {
            size = 50;
        }
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }
}
