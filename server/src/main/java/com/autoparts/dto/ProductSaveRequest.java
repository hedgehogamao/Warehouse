package com.autoparts.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 商品新增/修改请求 DTO
 * 新增和修改共用（SKU 新增时必填，编辑时不可修改）
 */
public class ProductSaveRequest {

    /** 商品名称 */
    @NotBlank(message = "商品名称不能为空")
    private String name;

    /** SKU 编码（新增时必填） */
    private String sku;

    /** 品牌 */
    private String brand;

    /** 适配车型 */
    private String carModel;

    /** 分类 */
    private String category;

    /** 单位 */
    @NotBlank(message = "单位不能为空")
    private String unit;

    /** 成本价 */
    private BigDecimal costPrice;

    /** 销售价 */
    @NotNull(message = "销售价不能为空")
    @Min(value = 0, message = "价格不能为负")
    private BigDecimal salePrice;

    /** 最低库存预警值 */
    private Integer minStock;

    /** 商品图片 URL */
    private String imageUrl;

    /** 商品描述 */
    private String description;

    public ProductSaveRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getCarModel() { return carModel; }
    public void setCarModel(String carModel) { this.carModel = carModel; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }

    public BigDecimal getSalePrice() { return salePrice; }
    public void setSalePrice(BigDecimal salePrice) { this.salePrice = salePrice; }

    public Integer getMinStock() { return minStock; }
    public void setMinStock(Integer minStock) { this.minStock = minStock; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
