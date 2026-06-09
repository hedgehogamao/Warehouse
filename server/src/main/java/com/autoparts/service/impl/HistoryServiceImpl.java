package com.autoparts.service.impl;

import com.autoparts.model.InventoryLog;
import com.autoparts.model.OperationLog;
import com.autoparts.model.Product;
import com.autoparts.repository.InventoryLogMapper;
import com.autoparts.repository.OperationLogMapper;
import com.autoparts.repository.ProductMapper;
import com.autoparts.service.HistoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 历史记录业务逻辑实现
 */
@Service
public class HistoryServiceImpl implements HistoryService {

    private final InventoryLogMapper inventoryLogMapper;
    private final OperationLogMapper operationLogMapper;
    private final ProductMapper productMapper;

    public HistoryServiceImpl(InventoryLogMapper inventoryLogMapper,
                               OperationLogMapper operationLogMapper,
                               ProductMapper productMapper) {
        this.inventoryLogMapper = inventoryLogMapper;
        this.operationLogMapper = operationLogMapper;
        this.productMapper = productMapper;
    }

    @Override
    public IPage<InventoryLog> pageInventoryLogs(int page, int size,
                                                   String type, Integer productId,
                                                   String productName,
                                                   String startDate, String endDate) {
        LambdaQueryWrapper<InventoryLog> wrapper = new LambdaQueryWrapper<>();

        if (type != null && !type.isEmpty()) {
            wrapper.eq(InventoryLog::getType, type);
        }
        if (productId != null) {
            wrapper.eq(InventoryLog::getProductId, productId);
        }
        // 按商品名称搜索：先查商品 ID 列表
        if (productName != null && !productName.trim().isEmpty()) {
            List<Product> products = productMapper.selectList(
                    new LambdaQueryWrapper<Product>().like(Product::getName, productName));
            if (products.isEmpty()) {
                // 没有匹配商品，返回空结果
                return new Page<>(page, size, 0);
            }
            List<Integer> productIds = products.stream().map(Product::getId).toList();
            wrapper.in(InventoryLog::getProductId, productIds);
        }
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(InventoryLog::getCreatedAt, LocalDateTime.parse(startDate + "T00:00:00"));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(InventoryLog::getCreatedAt, LocalDateTime.parse(endDate + "T23:59:59"));
        }

        wrapper.orderByDesc(InventoryLog::getCreatedAt);
        return inventoryLogMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public IPage<OperationLog> pageOperationLogs(int page, int size,
                                                   String module, Integer operatorId,
                                                   String startDate, String endDate) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();

        if (module != null && !module.isEmpty()) {
            wrapper.eq(OperationLog::getModule, module);
        }
        if (operatorId != null) {
            wrapper.eq(OperationLog::getOperatorId, operatorId);
        }
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge(OperationLog::getCreatedAt, LocalDateTime.parse(startDate + "T00:00:00"));
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le(OperationLog::getCreatedAt, LocalDateTime.parse(endDate + "T23:59:59"));
        }

        wrapper.orderByDesc(OperationLog::getCreatedAt);
        return operationLogMapper.selectPage(new Page<>(page, size), wrapper);
    }
}
