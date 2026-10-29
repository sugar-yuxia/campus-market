package com.campus.market.controller;

import com.campus.market.auth.CurrentUser;
import com.campus.market.common.PageResult;
import com.campus.market.common.Result;
import com.campus.market.dto.CreateOrderRequest;
import com.campus.market.service.OrderService;
import com.campus.market.vo.OrderVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Result<OrderVO> create(@CurrentUser Long userId,
                                  @Valid @RequestBody CreateOrderRequest request) {
        return Result.ok(orderService.create(userId, request));
    }

    @GetMapping("/bought")
    public Result<PageResult<OrderVO>> bought(@CurrentUser Long userId,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             @RequestParam(required = false) String status) {
        return Result.ok(orderService.bought(userId, page, size, status));
    }

    @GetMapping("/sold")
    public Result<PageResult<OrderVO>> sold(@CurrentUser Long userId,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(required = false) String status) {
        return Result.ok(orderService.sold(userId, page, size, status));
    }

    @GetMapping("/{id}")
    public Result<OrderVO> detail(@CurrentUser Long userId, @PathVariable Long id) {
        return Result.ok(orderService.detail(userId, id));
    }

    @PutMapping("/{id}/pay")
    public Result<OrderVO> pay(@CurrentUser Long userId, @PathVariable Long id) {
        return Result.ok(orderService.pay(userId, id));
    }

    @PutMapping("/{id}/confirm")
    public Result<OrderVO> confirm(@CurrentUser Long userId, @PathVariable Long id) {
        return Result.ok(orderService.confirm(userId, id));
    }

    @PutMapping("/{id}/cancel")
    public Result<OrderVO> cancel(@CurrentUser Long userId, @PathVariable Long id) {
        return Result.ok(orderService.cancel(userId, id));
    }
}
