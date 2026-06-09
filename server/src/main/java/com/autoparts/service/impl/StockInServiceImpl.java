package com.autoparts.service.impl;

import com.autoparts.common.BusinessException;
import com.autoparts.model.InventoryLog;
import com.autoparts.model.Product;
import com.autoparts.model.StockInRecord;
import com.autoparts.model.User;
import com.autoparts.repository.InventoryLogMapper;
import com.autoparts.repository.ProductMapper;
import com.autoparts.repository.StockInRecordMapper;
import com.autoparts.repository.UserMapper;
import com.autoparts.service.StockInService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 入库业务逻辑实现
 */
@Service
public class StockInServiceImpl implements StockInService {

    private final StockInRecordMapper stockInRecordMapper;
    private final ProductMapper productMapper;
    private final InventoryLogMapper inventoryLogMapper;
    private final UserMapper userMapper;

    public StockInServiceImpl(StockInRecordMapper stockInRecordMapper,
                               ProductMapper productMapper,
                               InventoryLogMapper inventoryLogMapper,
                               UserMapper userMapper) {
        this.stockInRecordMapper = stockInRecordMapper;
        this.productMapper = productMapper;
        this.inventoryLogMapper = inventoryLogMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createStockIn(Integer productId, Integer quantity,
                                              BigDecimal costPrice, String remark,
                                              Integer operatorId) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessException(400, "入库数量必须大于0");
        }

        // 1. 查询商品
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        int beforeStock = product.getStock();

        // 2. 保存入库记录
        StockInRecord record = new StockInRecord();
        record.setProductId(productId);
        record.setQuantity(quantity);
        record.setCostPrice(costPrice);
        record.setRemark(remark);
        record.setOperatorId(operatorId);
        record.setCreatedAt(LocalDateTime.now());
        stockInRecordMapper.insert(record);

        // 3. 更新商品库存
        product.setStock(beforeStock + quantity);
        if (costPrice != null) {
            product.setCostPrice(costPrice);
        }
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);

        // 4. 记录库存流水
        InventoryLog log = new InventoryLog();
        log.setProductId(productId);
        log.setType("STOCK_IN");
        log.setQuantity(quantity);
        log.setBeforeStock(beforeStock);
        log.setAfterStock(beforeStock + quantity);
        log.setRefId(record.getId());
        log.setRemark("入库" + (remark != null && !remark.isEmpty() ? ": " + remark : ""));
        log.setOperatorId(operatorId);
        log.setCreatedAt(LocalDateTime.now());
        inventoryLogMapper.insert(log);

        // 5. 返回结果
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", record.getId());
        result.put("productId", productId);
        result.put("productName", product.getName());
        result.put("quantity", quantity);
        result.put("beforeStock", beforeStock);
        result.put("afterStock", beforeStock + quantity);
        result.put("createdAt", record.getCreatedAt());
        return result;
    }

    @Override
    public IPage<Map<String, Object>> pageStockInRecords(int page, int size,
                                                           Integer productId,
                                                           String startDate, String endDate) {
        LambdaQueryWrapper<StockInRecord> wrapper = new LambdaQueryWrapper<>();

        if (productId != null) {
            wrapper.eq(StockInRecord::getProductId, productId);
        }
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(StockInRecord::getCreatedAt,
                    LocalDateTime.parse(startDate + "T00:00:00"));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(StockInRecord::getCreatedAt,
                    LocalDateTime.parse(endDate + "T23:59:59"));
        }

        wrapper.orderByDesc(StockInRecord::getCreatedAt);

        IPage<StockInRecord> recordPage = stockInRecordMapper.selectPage(
                new Page<>(page, size), wrapper);

        // 转换为包含商品名称的 Map
        IPage<Map<String, Object>> resultPage = new Page<>(page, size, recordPage.getTotal());
        resultPage.setRecords(recordPage.getRecords().stream().map(record -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", record.getId());
            map.put("productId", record.getProductId());
            // 查询商品名称和 SKU
            Product product = productMapper.selectById(record.getProductId());
            map.put("productName", product != null ? product.getName() : "未知商品");
            map.put("productSku", product != null ? product.getSku() : "");
            map.put("imageUrl", product != null ? product.getImageUrl() : "");
            map.put("quantity", record.getQuantity());
            map.put("costPrice", record.getCostPrice());
            map.put("remark", record.getRemark());
            // 查询操作人姓名
            User operator = userMapper.selectById(record.getOperatorId());
            map.put("operatorName", operator != null ? operator.getRealName() : "未知");
            map.put("createdAt", record.getCreatedAt());
            return map;
        }).toList());

        return resultPage;
    }

    @Override
    public Map<String, Object> getStockInDetail(Integer id) {
        StockInRecord record = stockInRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException(404, "入库记录不存在");
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", record.getId());
        map.put("productId", record.getProductId());
        Product product = productMapper.selectById(record.getProductId());
        map.put("productName", product != null ? product.getName() : "未知商品");
        map.put("productSku", product != null ? product.getSku() : "");
        map.put("quantity", record.getQuantity());
        map.put("costPrice", record.getCostPrice());
        map.put("remark", record.getRemark());
        User operator = userMapper.selectById(record.getOperatorId());
        map.put("operatorName", operator != null ? operator.getRealName() : "未知");
        map.put("createdAt", record.getCreatedAt());
        return map;
    }
}
