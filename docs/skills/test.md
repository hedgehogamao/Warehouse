# Skill: 测试规范

> 适用于测试工程师（`qa-engineer`）和后端开发（`backend-dev`）

---

## 1 测试策略

| 层级 | 工具 | 覆盖范围 |
|------|------|----------|
| 单元测试 | JUnit 5 + Mockito | Service 层业务逻辑 |
| 集成测试 | MockMvc / SpringBootTest | Controller 层 API |
| 接口测试 | Apifox / Postman | 全部 API 端到端 |
| 功能测试 | 手动 + 验收清单 | 前端页面交互 |

---

## 2 单元测试规范

### 2.1 测试类命名

- 测试类：`{类名}Test`，如 `ProductServiceTest`
- 测试方法：`test{方法名}_{场景}`，如 `testCreateProduct_DuplicateSku`
- 文件位置：`src/test/java/com/autoparts/service/`

### 2.2 测试结构（AAA 模式）

```java
@Test
void testCreateOrder_Success() {
    // Arrange - 准备数据
    OrderCreateRequest request = new OrderCreateRequest();
    request.setItems(List.of(new ItemDTO(1, 2, new BigDecimal("298.00"))));
    
    Product product = new Product();
    product.setId(1);
    product.setStock(100);
    product.setStatus(1);
    
    when(productMapper.selectById(1)).thenReturn(product);
    
    // Act - 执行操作
    OrderVO result = orderService.createOrder(request);
    
    // Assert - 验证结果
    assertNotNull(result);
    assertEquals("PAID", result.getStatus());
    verify(productMapper).updateById(argThat(p -> p.getStock() == 98));
}
```

### 2.3 Mock 策略

- 使用 `@MockBean` 或 `@Mock` 模拟 Mapper 层
- Service 层之间的依赖使用 `@Mock` + `@InjectMocks`
- 不依赖真实数据库（使用 H2 内存库或纯 Mock）

```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderMapper orderMapper;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private InventoryLogMapper inventoryLogMapper;
    
    @InjectMocks
    private OrderServiceImpl orderService;

    // 测试方法...
}
```

---

## 3 集成测试规范

### 3.1 测试配置

```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=MySQL
    driver-class-name: org.h2.Driver
  sql:
    init:
      schema-locations: classpath:schema-h2.sql
      data-locations: classpath:test-data.sql
```

### 3.2 Controller 测试

```java
@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testListProducts() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                .param("page", "1")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    void testCreateProduct_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"test\"}"))
                .andExpect(status().isUnauthorized());
    }
}
```

---

## 4 关键测试用例清单

### Phase 1: 用户认证

| # | 用例 | 预期 |
|---|------|------|
| 1 | 正确用户名密码登录 | 返回 Token |
| 2 | 错误密码登录 | 返回 401 |
| 3 | 无 Token 访问接口 | 返回 401 |
| 4 | STAFF 访问用户管理 | 返回 403 |
| 5 | 修改密码（正确旧密码） | 成功 |
| 6 | 修改密码（错误旧密码） | 失败 |
| 7 | 禁用用户登录 | 返回 401 |

### Phase 2: 商品管理

| # | 用例 | 预期 |
|---|------|------|
| 1 | 新增商品 | 保存成功 |
| 2 | 重复 SKU 新增 | 返回错误 |
| 3 | 关键字搜索 | 匹配名称/SKU/车型 |
| 4 | 编辑商品 | 数据更新 |
| 5 | 上下架 | 状态变更 |
| 6 | 删除商品 | 记录删除 |

### Phase 3: 入库与库存

| # | 用例 | 预期 |
|---|------|------|
| 1 | 入库 | 库存增加，流水生成 |
| 2 | 库存预警 | low-stock 返回正确 |
| 3 | 库存调整（盘盈） | 库存增加 |
| 4 | 库存调整（盘亏） | 库存减少 |
| 5 | 入库异常回滚 | 库存不变 |

### Phase 4: 销售收银

| # | 用例 | 预期 |
|---|------|------|
| 1 | 正常下单 | 订单生成，库存扣减 |
| 2 | 库存不足下单 | 返回错误 |
| 3 | 订单退款 | 库存恢复 |
| 4 | 订单编号唯一性 | 不重复 |
| 5 | 事务异常回滚 | 所有操作撤销 |

### Phase 6: 小程序

| # | 用例 | 预期 |
|---|------|------|
| 1 | 微信登录 | Token 返回 |
| 2 | 商品浏览 | 列表正常 |
| 3 | 商品搜索 | 结果正确 |
| 4 | 购买记录 | 仅显示自己的订单 |
| 5 | Token 过期 | 401 处理 |

---

## 5 接口测试集合

- 使用 Apifox 或 Postman 维护
- 包含所有 API 的请求示例和预期响应
- 按模块分组：Auth、Product、StockIn、Inventory、Order、History、MiniApp
- 导出为 JSON：`server/postman/collection.json`

---

## 6 性能测试

| 场景 | 工具 | 目标 |
|------|------|------|
| 商品列表查询 | JMeter | 50 并发，平均 < 1s |
| 下单操作 | JMeter | 50 并发，平均 < 2s |
| 统计查询 | JMeter | 10 并发，平均 < 3s |

---

## 7 测试覆盖率

- 目标：Service 层 ≥ 80%
- 工具：JaCoCo（Maven 插件）
- CI 中集成覆盖率报告
