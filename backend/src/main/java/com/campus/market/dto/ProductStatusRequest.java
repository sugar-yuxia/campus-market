package com.campus.market.dto;

import jakarta.validation.constraints.NotBlank;

public record ProductStatusRequest(@NotBlank(message = "状态不能为空") String status) {
}
