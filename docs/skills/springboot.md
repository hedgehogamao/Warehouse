# Skill: Spring Boot 后端开发规范

> 适用于后端开发（`backend-dev`）和安全工程师（`security`）

---

## 1 分层架构

```
Controller  →  Service  →  Repository (Mapper)  →  Database
    ↑              ↑
  DTO/VO      Model/Entity
```

| 层 | 包名 | 职责 |
|----|------|------|
| Controller | `com.autoparts.controller` | 接收请求、参数校验、调用 Service、返回 Result |
| Service | `com.autoparts.service` | 业务逻辑、事务管理 |
| Service Impl | `com.autoparts.service.impl` | Service 实现类 |
| Repository | `com.autoparts.repository` | 数据访问（MyBatis-Plus BaseMapper） |
| Model | `com.autoparts.model` | 数据库实体（对应表结构） |
| DTO | `com.autoparts.dto` | 请求参数对象（前端 → 后端） |
| VO | `com.autoparts.vo` | 响应视图对象（后端 → 前端） |
| Common | `com.autoparts.common` | 公共工具类 |
| Config | `com.autoparts.config` | 配置类 |
| AOP | `com.autoparts.aop` | 切面（操作日志等） |
| Annotation | `com.autoparts.annotation` | 自定义注解 |

---

## 2 命名规范

### 2.1 类命名

| 类型 | 规则 | 示例 |
|------|------|------|
| Controller | `{模块}Controller` | `ProductController` |
| Service | `{模块}Service` | `OrderService` |
| Service Impl | `{模块}ServiceImpl` | `OrderServiceImpl` |
| Mapper | `{模块}Mapper` | `ProductMapper` |
| Entity | 单数名词 | `Product`、`Order` |
| DTO | `{动作}{模块}Request` | `OrderCreateRequest`、`ProductSaveRequest` |
| VO | `{模块}VO` / `{模块}DetailVO` | `ProductVO`、`OrderDetailVO` |

### 2.2 方法命名

| 动作 | 命名 | 示例 |
|------|------|------|
| 查询单个 | `get{id}` | `getProduct`、`getById` |
| 查询列表 | `list{模块}` / `page{模块}` | `listProducts`、`pageOrders` |
| 新增 | `create{模块}` / `save{模块}` | `createOrder`、`saveProduct` |
| 修改 | `update{模块}` | `updateProduct` |
| 删除 | `delete{模块}` / `remove{模块}` | `deleteProduct` |

---

## 3 统一响应体

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}
```

**所有 Controller 方法返回 `Result<T>`**

---

## 4 统一异常处理

```java
@Data
@AllArgsConstructor
public class BusinessException extends RuntimeException {
    private int code;
    private String message;
}

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusiness(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        return Result.error(500, "服务器内部错误");
    }
}
```

---

## 5 DTO / VO 设计原则

### DTO（请求对象）

- 仅包含前端提交的字段
- 使用 `@NotBlank`、`@NotNull`、`@Min` 等校验注解
- Controller 中使用 `@Valid` 触发校验

```java
@Data
public class ProductSaveRequest {
    @NotBlank(message = "商品名称不能为空")
    private String name;
    @NotBlank(message = "SKU不能为空")
    private String sku;
    private String brand;
    private String category;
    @NotNull(message = "销售价不能为空")
    @Min(value = 0, message = "价格不能为负")
    private BigDecimal salePrice;
    // ...
}
```

### VO（响应对象）

- 不返回敏感字段（如 password）
- 包含前端需要的展示字段
- 可包含额外计算字段（如 `stockStatus`、`typeName`）

---

## 6 事务管理

- 使用 `@Transactional` 注解
- 写操作（入库、下单、退款、库存调整）必须在 Service 层加事务
- 事务方法必须是 `public` 的
- 异常类型指定：`@Transactional(rollbackFor = Exception.class)`

```java
@Service
public class StockInServiceImpl implements StockInService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StockInVO stockIn(StockInRequest request) {
        // 1. 查询商品
        // 2. 保存入库记录
        // 3. 更新库存
        // 4. 记录库存流水
    }
}
```

---

## 7 分页规范

使用 MyBatis-Plus 分页：

```java
// Controller
@GetMapping("/products")
public Result<IPage<ProductVO>> listProducts(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String category) {
    return Result.success(productService.pageProducts(page, size, keyword, category));
}
```

**分页响应格式**：
```json
{
  "code": 200,
  "data": {
    "total": 128,
    "pages": 7,
    "records": [...]
  }
}
```

---

## 8 认证与鉴权

- JWT Token 存储用户 ID 和角色
- 请求头：`Authorization: Bearer {token}`
- 使用 Spring Security + 自定义 `JwtAuthFilter`
- 白名单接口：登录、健康检查、小程序登录
- 权限控制：`@PreAuthorize("hasRole('ADMIN')")` 或自定义拦截

---

## 9 技术栈依赖

```xml
<!-- pom.xml 核心依赖 -->
spring-boot-starter-web
spring-boot-starter-security
spring-boot-starter-validation
mybatis-plus-spring-boot3          <!-- 3.5.x -->
mysql-connector-j
jjwt-api / jjwt-impl / jjwt-jackson  <!-- 0.12.x -->
lombok
hutool-all                         <!-- 工具库 -->
```

---

## 10 代码产出规则

- 每个 Controller 对应一个业务模块
- 每个 Service 接口 + 实现类成对出现
- 每个 Mapper 继承 `BaseMapper<Entity>`
- 复杂 SQL 使用 XML 映射文件（`resources/mapper/*.xml`）
- 简单查询使用 MyBatis-Plus QueryWrapper
