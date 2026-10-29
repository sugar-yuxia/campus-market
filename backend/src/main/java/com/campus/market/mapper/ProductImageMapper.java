package com.campus.market.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductImageMapper {

    List<String> selectUrlsByProductId(@Param("productId") Long productId);

    int batchInsert(@Param("productId") Long productId, @Param("urls") List<String> urls);

    int deleteByProductId(@Param("productId") Long productId);
}
