package com.campus.market.service;

import com.campus.market.auth.JwtUtil;
import com.campus.market.common.BizException;
import com.campus.market.dto.LoginRequest;
import com.campus.market.dto.LoginResponse;
import com.campus.market.dto.RegisterRequest;
import com.campus.market.entity.User;
import com.campus.market.mapper.UserMapper;
import com.campus.market.vo.UserVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userMapper.countByUsername(request.username()) > 0) {
            throw new BizException("该用户名已被注册");
        }
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setNickname(StringUtils.hasText(request.nickname()) ? request.nickname() : request.username());
        userMapper.insert(user);

        User saved = userMapper.findByUsername(request.username());
        return buildLoginResponse(saved);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userMapper.findByUsername(request.username());
        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BizException(401, "用户名或密码错误");
        }
        return buildLoginResponse(user);
    }

    private LoginResponse buildLoginResponse(User user) {
        String token = jwtUtil.generate(user.getId(), user.getUsername());
        return new LoginResponse(token, UserVO.from(user));
    }
}
