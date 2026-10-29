package com.campus.market.mapper;

import com.campus.market.entity.Order;
import com.campus.market.vo.OrderVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderMapper {

    int insert(Order order);

    Order findById(@Param("id") Long id);

    OrderVO findDetailById(@Param("id") Long id);

    List<OrderVO> selectBoughtPage(@Param("buyerId") Long buyerId,
                                  @Param("status") String status,
                                  @Param("offset") int offset,
                                  @Param("size") int size);

    long countBought(@Param("buyerId") Long buyerId, @Param("status") String status);

    List<OrderVO> selectSoldPage(@Param("sellerId") Long sellerId,
                                 @Param("status") String status,
                                 @Param("offset") int offset,
                                 @Param("size") int size);

    long countSold(@Param("sellerId") Long sellerId, @Param("status") String status);

    int pay(@Param("id") Long id, @Param("buyerId") Long buyerId);

    int confirm(@Param("id") Long id, @Param("buyerId") Long buyerId);

    int cancel(@Param("id") Long id, @Param("userId") Long userId);
}
