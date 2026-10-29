package com.campus.market.mapper;

import com.campus.market.entity.OrderItem;
import org.apache.ibatis.annotations.Param;

public interface OrderItemMapper {

    int insert(OrderItem item);

    OrderItem findByOrderId(@Param("orderId") Long orderId);
}
