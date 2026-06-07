package com.autoparts.common;

/**
 * 系统常量
 */
public class Constants {

    /** 默认分页大小 */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /** 最大分页大小 */
    public static final int MAX_PAGE_SIZE = 100;

    /** 用户角色：管理员 */
    public static final String ROLE_ADMIN = "ADMIN";

    /** 用户角色：普通员工 */
    public static final String ROLE_STAFF = "STAFF";

    /** 请求头中的 Token 键名 */
    public static final String TOKEN_HEADER = "Authorization";

    /** Token 前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";

    /** 状态：启用 */
    public static final int STATUS_ENABLED = 1;

    /** 状态：禁用 */
    public static final int STATUS_DISABLED = 0;

    private Constants() {
        // 防止实例化
    }
}
