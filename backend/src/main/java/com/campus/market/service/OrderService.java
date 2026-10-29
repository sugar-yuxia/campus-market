package com.campus.market.service;

import com.campus.market.common.BizException;
import com.campus.market.common.PageResult;
import com.campus.market.constant.OrderStatus;
import com.campus.market.constant.ProductStatus;
import com.campus.market.dto.CreateOrderRequest;
import com.campus.market.entity.Order;
import com.campus.market.entity.OrderItem;
import com.campus.market.mapper.OrderItemMapper;
import com.campus.market.mapper.OrderMapper;
import com.campus.market.mapper.ProductMapper;
import com.campus.market.vo.OrderVO;
import com.campus.market.vo.ProductVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderService {

    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final HotProductService hotProductService;

    public OrderService(OrderMapper orderMapper,
                        OrderItemMapper orderItemMapper,
                        ProductMapper productMapper,
                        HotProductService hotProductService) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.productMapper = productMapper;
        this.hotProductService = hotProductService;
    }

    /**
     * 下单时通过带状态条件的 UPDATE 抢占商品，避免同一商品被重复购买。
     */
    @Transactional
    public OrderVO create(Long buyerId, CreateOrderRequest request) {
        ProductVO product = productMapper.selectDetail(request.productId());
        if (product == null) {
            throw new BizException(404, "商品不存在");
        }
        if (Objects.equals(product.getSellerId(), buyerId)) {
            throw new BizException("不能购买自己发布的商品");
        }
        if (!ProductStatus.ON_SALE.equals(product.getStatus())) {
            throw new BizException("商品已下架或已被购买");
        }
        if (productMapper.lockForSale(product.getId()) == 0) {
            throw new BizException(409, "手慢了，商品已被其他同学下单");
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setProductId(product.getId());
        order.setBuyerId(buyerId);
        order.setSellerId(product.getSellerId());
        order.setAmount(product.getPrice());
        order.setStatus(OrderStatus.PENDING_PAY);
        order.setRemark(request.remark());
        orderMapper.insert(order);

        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setProductId(product.getId());
        item.setProductTitle(product.getTitle());
        item.setProductCover(product.getCover());
        item.setPrice(product.getPrice());
        item.setQuantity(1);
        orderItemMapper.insert(item);

        hotProductService.evict();
        return orderMapper.findDetailById(order.getId());
    }

    public PageResult<OrderVO> bought(Long buyerId, int page, int size, String status) {
        page = Math.max(page, 1);
        size = Math.min(Math.max(size, 1), 50);
        long total = orderMapper.countBought(buyerId, status);
        List<OrderVO> records = total == 0
                ? List.of()
                : orderMapper.selectBoughtPage(buyerId, status, (page - 1) * size, size);
        return PageResult.of(records, total, page, size);
    }

    public PageResult<OrderVO> sold(Long sellerId, int page, int size, String status) {
        page = Math.max(page, 1);
        size = Math.min(Math.max(size, 1), 50);
        long total = orderMapper.countSold(sellerId, status);
        List<OrderVO> records = total == 0
                ? List.of()
                : orderMapper.selectSoldPage(sellerId, status, (page - 1) * size, size);
        return PageResult.of(records, total, page, size);
    }

    public OrderVO detail(Long userId, Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new BizException(404, "订单不存在");
        }
        if (!Objects.equals(order.getBuyerId(), userId) && !Objects.equals(order.getSellerId(), userId)) {
            throw new BizException(403, "无权查看该订单");
        }
        return orderMapper.findDetailById(orderId);
    }

    public OrderVO pay(Long buyerId, Long orderId) {
        Order order = getOwnedOrder(buyerId, orderId);
        if (orderMapper.pay(orderId, buyerId) == 0) {
            throw new BizException("订单状态已变更，无法支付");
        }
        return orderMapper.findDetailById(orderId);
    }

    @Transactional
    public OrderVO confirm(Long buyerId, Long orderId) {
        Order order = getOwnedOrder(buyerId, orderId);
        if (orderMapper.confirm(orderId, buyerId) == 0) {
            throw new BizException("订单状态已变更，无法确认收货");
        }
        productMapper.transferStatus(order.getProductId(), ProductStatus.LOCKED, ProductStatus.SOLD);
        hotProductService.evict();
        return orderMapper.findDetailById(orderId);
    }

    @Transactional
    public OrderVO cancel(Long userId, Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new BizException(404, "订单不存在");
        }
        if (!Objects.equals(order.getBuyerId(), userId) && !Objects.equals(order.getSellerId(), userId)) {
            throw new BizException(403, "无权取消该订单");
        }
        if (orderMapper.cancel(orderId, userId) == 0) {
            throw new BizException("只有待付款订单可以取消");
        }
        productMapper.transferStatus(order.getProductId(), ProductStatus.LOCKED, ProductStatus.ON_SALE);
        hotProductService.evict();
        return orderMapper.findDetailById(orderId);
    }

    private Order getOwnedOrder(Long buyerId, Long orderId) {
        Order order = orderMapper.findById(orderId);
        if (order == null) {
            throw new BizException(404, "订单不存在");
        }
        if (!Objects.equals(order.getBuyerId(), buyerId)) {
            throw new BizException(403, "无权操作该订单");
        }
        return order;
    }

    private String generateOrderNo() {
        return "CM" + LocalDateTime.now().format(ORDER_NO_FORMATTER)
                + ThreadLocalRandom.current().nextInt(1000, 10000);
    }
}
