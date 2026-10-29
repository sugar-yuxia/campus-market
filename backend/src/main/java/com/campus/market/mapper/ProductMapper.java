package com.campus.market.mapper;

import com.campus.market.dto.ProductQuery;
import com.campus.market.entity.Product;
import com.campus.market.vo.ProductVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductMapper {

    List<ProductVO> selectPage(@Param("q") ProductQuery query);

    long countPage(@Param("q") ProductQuery query);

    List<ProductVO> selectHot(@Param("limit") int limit);

    ProductVO selectDetail(@Param("id") Long id);

    int insert(Product product);

    int update(Product product);

    int updateStatus(@Param("id") Long id, @Param("sellerId") Long sellerId, @Param("status") String status);

    int delete(@Param("id") Long id, @Param("sellerId") Long sellerId);

    int incrementViewCount(@Param("id") Long id);

    int lockForSale(@Param("id") Long id);

    int transferStatus(@Param("id") Long id, @Param("from") String from, @Param("to") String to);
}
