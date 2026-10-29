package com.campus.market.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 20, message = "昵称最多 20 个字符") String nickname,
        @Size(max = 255, message = "头像地址过长") String avatar,
        @Pattern(regexp = "^[0-9]{0,20}$", message = "手机号格式不正确") String phone) {
}
