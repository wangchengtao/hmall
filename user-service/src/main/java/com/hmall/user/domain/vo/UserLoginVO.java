package com.hmall.user.domain.vo;

import com.hmall.user.enums.UserStatus;
import lombok.Data;

@Data
public class UserLoginVO {
    private String token;
    private Long userId;
    private String username;
    private Integer balance;
    private UserStatus status;
}
