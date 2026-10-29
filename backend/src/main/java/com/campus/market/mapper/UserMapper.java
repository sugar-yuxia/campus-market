package com.campus.market.mapper;

import com.campus.market.entity.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {

    User findById(@Param("id") Long id);

    User findByUsername(@Param("username") String username);

    int insert(User user);

    int updateProfile(User user);

    long countByUsername(@Param("username") String username);
}
