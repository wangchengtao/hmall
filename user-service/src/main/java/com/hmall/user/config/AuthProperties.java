package com.hmall.user.config;

import lombok.Data;

import java.util.List;

@Data
// @ConfigurationProperties(prefix = "hm.auth")
public class AuthProperties {
    private List<String> includePaths;
    private List<String> excludePaths;
}
