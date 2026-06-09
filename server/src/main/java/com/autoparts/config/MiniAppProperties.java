package com.autoparts.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信小程序配置属性
 * 从 application.yml 中读取 wechat.miniapp.* 配置
 */
@Component
@ConfigurationProperties(prefix = "wechat.miniapp")
public class MiniAppProperties {

    /** 小程序 AppID */
    private String appId;

    /** 小程序 AppSecret */
    private String appSecret;

    /** 微信登录接口 URL */
    private String loginUrl = "https://api.weixin.qq.com/sns/jscode2session";

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }

    public String getAppSecret() { return appSecret; }
    public void setAppSecret(String appSecret) { this.appSecret = appSecret; }

    public String getLoginUrl() { return loginUrl; }
    public void setLoginUrl(String loginUrl) { this.loginUrl = loginUrl; }
}
