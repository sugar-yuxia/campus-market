package com.campus.market.dto;

import com.campus.market.vo.UserVO;

public record LoginResponse(String token, UserVO user) {
}
