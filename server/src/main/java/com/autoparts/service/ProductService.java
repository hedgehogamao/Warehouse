package com.autoparts.service;

import com.autoparts.dto.ProductSaveRequest;
import com.autoparts.model.Product;
import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 商品业务逻辑接口
 */
public interface ProductService {

    /**
     * 分页查询商品（支持关键词、品牌、分类、状态筛选）
     */
    IPage<Product> pageProducts(int page, int size, String keyword,
                                 String brand, String category, Integer status);

    /**
     * 根据 ID 获取商品详情
     */
    Product getProductById(Integer id);

    /**
     * 新增商品
     */
    void createProduct(ProductSaveRequest request);

    /**
     * 修改商品
     */
    void updateProduct(Integer id, ProductSaveRequest request);

    /**
     * 删除商品
     */
    void deleteProduct(Integer id);

    /**
     * 上架/下架商品
     */
    void updateProductStatus(Integer id, Integer status);

    /**
     * 获取所有已存在的分类列表
     */
    List<String> getCategories();

    /**
     * POS 快速搜索商品（仅返回 id, name, sku, salePrice, stock）
     */
    List<Map<String, Object>> posProductSearch(String keyword);

    /**
     * Excel 批量导入商品
     */
    Map<String, Object> importProducts(MultipartFile file);

    /**
     * 生成导入模板 Excel
     */
    void generateImportTemplate(HttpServletResponse response) throws IOException;
}
