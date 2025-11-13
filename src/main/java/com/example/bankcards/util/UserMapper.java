package com.example.bankcards.util;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.User;

public class UserMapper {
    public static UserDto toUserDto(User user) {
        return new UserDto(user.getUsername(),
                           user.getRole());
    }
}
