package com.campus.market.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record ProductRequest(
        @NotBlank(message = "标题不能为空")
        @Size(max = 60, message = "标题最多 60 个字符")
        String title,

        @Size(max = 2000, message = "描述最多 2000 个字符")
        String description,

        @NotNull(message = "价格不能为空")
        @DecimalMin(value = "0.01", message = "价格必须大于 0")
        BigDecimal price,

        @DecimalMin(value = "0.01", message = "原价必须大于 0")
        BigDecimal originalPrice,

        @NotNull(message = "请选择分类")
        Long categoryId,

        @NotNull(message = "请选择成色")
        @Min(value = 1, message = "成色取值为 1-10")
        @Max(value = 10, message = "成色取值为 1-10")
        Integer conditionLevel,

        @Size(max = 9, message = "最多上传 9 张图片")
        List<String> images) {
}
