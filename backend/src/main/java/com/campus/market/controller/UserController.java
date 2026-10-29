package com.campus.market.controller;

import com.campus.market.auth.CurrentUser;
import com.campus.market.common.Result;
import com.campus.market.dto.UpdateProfileRequest;
import com.campus.market.service.UserService;
import com.campus.market.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public Result<UserVO> me(@CurrentUser Long userId) {
        return Result.ok(userService.getById(userId));
    }

    @PutMapping("/me")
    public Result<UserVO> update(@CurrentUser Long userId,
                                 @Valid @RequestBody UpdateProfileRequest request) {
        return Result.ok(userService.updateProfile(userId, request));
    }

    @GetMapping("/{id}")
    public Result<UserVO> detail(@PathVariable Long id) {
        return Result.ok(userService.getById(id));
    }
}
