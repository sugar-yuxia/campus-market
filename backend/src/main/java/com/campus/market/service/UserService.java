package com.campus.market.service;

import com.campus.market.common.BizException;
import com.campus.market.dto.UpdateProfileRequest;
import com.campus.market.entity.User;
import com.campus.market.mapper.UserMapper;
import com.campus.market.vo.UserVO;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public UserVO getById(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new BizException(404, "用户不存在");
        }
        return UserVO.from(user);
    }

    public UserVO updateProfile(Long userId, UpdateProfileRequest request) {
        User user = new User();
        user.setId(userId);
        user.setNickname(request.nickname());
        user.setAvatar(request.avatar());
        user.setPhone(request.phone());
        userMapper.updateProfile(user);
        return getById(userId);
    }
}
