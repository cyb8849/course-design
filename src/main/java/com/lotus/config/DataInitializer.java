package com.lotus.config;

import com.lotus.entity.LotusRootProduct;
import com.lotus.entity.ProductCategory;
import com.lotus.service.LotusRootProductService;
import com.lotus.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 数据初始化配置
 * 应用启动时自动添加一些基础数据
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final ProductCategoryService categoryService;
    private final LotusRootProductService productService;

    @Bean(name = "lotusDataInitializer")
    public ApplicationRunner dataInitializer() {
        return args -> {
            log.info("开始初始化基础数据...");
            
            // 初始化商品分类
            initCategories();
            
            // 初始化商品数据
            initProducts();
            
            log.info("基础数据初始化完成");
        };
    }

    /**
     * 初始化商品分类
     */
    private void initCategories() {
        List<ProductCategory> categories = categoryService.list();
        if (categories.isEmpty()) {
            log.info("初始化商品分类...");
            
            // 新鲜莲藕分类
            ProductCategory freshCategory = new ProductCategory();
            freshCategory.setCategoryName("新鲜莲藕");
            freshCategory.setSortOrder(1);
            categoryService.save(freshCategory);

            // 莲藕制品分类
            ProductCategory processedCategory = new ProductCategory();
            processedCategory.setCategoryName("莲藕制品");
            processedCategory.setSortOrder(2);
            categoryService.save(processedCategory);

            // 特色农产品分类
            ProductCategory specialCategory = new ProductCategory();
            specialCategory.setCategoryName("特色农产品");
            specialCategory.setSortOrder(3);
            categoryService.save(specialCategory);

            log.info("商品分类初始化完成");
        }
    }

    /**
     * 初始化商品数据
     */
    private void initProducts() {
        try {
            // 检查是否已有商品数据
            List<LotusRootProduct> existingProducts = productService.list();
            if (!existingProducts.isEmpty()) {
                // 已有商品数据，跳过初始化
                log.info("商品数据已存在，跳过初始化");
                return;
            }
            
            log.info("初始化商品数据...");

            // 获取分类
            List<ProductCategory> categories = categoryService.list();
            if (categories.isEmpty()) {
                log.warn("商品分类为空，无法初始化商品数据");
                return;
            }

            // 查找或创建新鲜莲藕分类
            ProductCategory freshCategory = categories.stream()
                    .filter(c -> "新鲜莲藕".equals(c.getCategoryName()))
                    .findFirst()
                    .orElseGet(() -> {
                        ProductCategory category = new ProductCategory();
                        category.setCategoryName("新鲜莲藕");
                        category.setSortOrder(1);
                        categoryService.save(category);
                        return category;
                    });

            // 查找或创建莲藕制品分类
            ProductCategory processedCategory = categories.stream()
                    .filter(c -> "莲藕制品".equals(c.getCategoryName()))
                    .findFirst()
                    .orElseGet(() -> {
                        ProductCategory category = new ProductCategory();
                        category.setCategoryName("莲藕制品");
                        category.setSortOrder(2);
                        categoryService.save(category);
                        return category;
                    });

            // 查找或创建特色农产品分类
            ProductCategory specialCategory = categories.stream()
                    .filter(c -> "特色农产品".equals(c.getCategoryName()))
                    .findFirst()
                    .orElseGet(() -> {
                        ProductCategory category = new ProductCategory();
                        category.setCategoryName("特色农产品");
                        category.setSortOrder(3);
                        categoryService.save(category);
                        return category;
                    });

            // 添加新鲜莲藕商品
                if (freshCategory != null) {
                    // 商品1：西胪特优粉藕
                    LotusRootProduct product1 = new LotusRootProduct();
                    product1.setProductName("西胪特优粉藕");
                    product1.setDescription("西胪镇特产，粉糯香甜，适合煲汤");
                    product1.setPrice(15.80);
                    product1.setStock(200);
                    product1.setStatus("ON_SALE");
                    product1.setCategoryId(freshCategory.getId());
                    product1.setFarmerId(1002L);
                    product1.setImageUrl("https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=fresh%20lotus%20root%20vegetable%20organic%20farm&image_size=square");
                    product1.setOrigin("汕头市潮阳区西胪镇");
                    product1.setHarvestTime(java.time.LocalDate.now());
                    productService.save(product1);

                    // 商品2：西胪脆嫩藕尖
                    LotusRootProduct product2 = new LotusRootProduct();
                    product2.setProductName("西胪脆嫩藕尖");
                    product2.setDescription("新鲜采摘的藕尖，清脆爽口，适合清炒");
                    product2.setPrice(22.80);
                    product2.setStock(150);
                    product2.setStatus("ON_SALE");
                    product2.setCategoryId(freshCategory.getId());
                    product2.setFarmerId(1003L);
                    product2.setImageUrl("https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=lotus%20root%20tips%20fresh%20vegetable&image_size=square");
                    product2.setOrigin("汕头市潮阳区西胪镇");
                    product2.setHarvestTime(java.time.LocalDate.now());
                    productService.save(product2);

                    // 商品3：西胪生态莲藕
                    LotusRootProduct product3 = new LotusRootProduct();
                    product3.setProductName("西胪生态莲藕");
                    product3.setDescription("生态种植，无农药，绿色健康");
                    product3.setPrice(12.80);
                    product3.setStock(180);
                    product3.setStatus("ON_SALE");
                    product3.setCategoryId(freshCategory.getId());
                    product3.setFarmerId(1002L);
                    product3.setImageUrl("https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=ecological%20lotus%20root%20vegetable&image_size=square");
                    product3.setOrigin("汕头市潮阳区西胪镇");
                    product3.setHarvestTime(java.time.LocalDate.now());
                    productService.save(product3);
                }

                // 添加莲藕制品
                if (processedCategory != null) {
                    // 商品4：西胪纯藕粉
                    LotusRootProduct product4 = new LotusRootProduct();
                    product4.setProductName("西胪纯藕粉");
                    product4.setDescription("100%纯藕粉，无添加，营养丰富");
                    product4.setPrice(39.90);
                    product4.setStock(100);
                    product4.setStatus("ON_SALE");
                    product4.setCategoryId(processedCategory.getId());
                    product4.setFarmerId(1004L);
                    product4.setImageUrl("https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=lotus%20root%20starch%20powder%20pure%20packaging&image_size=square");
                    product4.setOrigin("汕头市潮阳区西胪镇");
                    product4.setHarvestTime(java.time.LocalDate.now());
                    productService.save(product4);

                    // 商品5：西胪莲藕干
                    LotusRootProduct product5 = new LotusRootProduct();
                    product5.setProductName("西胪莲藕干");
                    product5.setDescription("自然晾晒，保留营养，方便存储");
                    product5.setPrice(49.90);
                    product5.setStock(80);
                    product5.setStatus("ON_SALE");
                    product5.setCategoryId(processedCategory.getId());
                    product5.setFarmerId(1004L);
                    product5.setImageUrl("https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=dried%20lotus%20root%20slices%20traditional&image_size=square");
                    product5.setOrigin("汕头市潮阳区西胪镇");
                    product5.setHarvestTime(java.time.LocalDate.now());
                    productService.save(product5);

                    // 商品6：西胪藕片零食
                    LotusRootProduct product6 = new LotusRootProduct();
                    product6.setProductName("西胪藕片零食");
                    product6.setDescription("脱水藕片，香脆可口，健康零食");
                    product6.setPrice(29.90);
                    product6.setStock(120);
                    product6.setStatus("ON_SALE");
                    product6.setCategoryId(processedCategory.getId());
                    product6.setFarmerId(1004L);
                    product6.setImageUrl("https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=lotus%20root%20chips%20snack%20packaging&image_size=square");
                    product6.setOrigin("汕头市潮阳区西胪镇");
                    product6.setHarvestTime(java.time.LocalDate.now());
                    productService.save(product6);
                }

                // 添加特色农产品
                if (specialCategory != null) {
                    // 商品7：西胪农家土鸡蛋
                    LotusRootProduct product7 = new LotusRootProduct();
                    product7.setProductName("西胪农家土鸡蛋");
                    product7.setDescription("农家散养土鸡下的蛋，营养丰富");
                    product7.setPrice(2.50);
                    product7.setStock(500);
                    product7.setStatus("ON_SALE");
                    product7.setCategoryId(specialCategory.getId());
                    product7.setFarmerId(1005L);
                    product7.setImageUrl("https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=fresh%20farm%20eggs%20organic&image_size=square");
                    product7.setOrigin("汕头市潮阳区西胪镇");
                    product7.setHarvestTime(java.time.LocalDate.now());
                    productService.save(product7);

                    // 商品8：西胪新鲜蔬菜礼盒
                    LotusRootProduct product8 = new LotusRootProduct();
                    product8.setProductName("西胪新鲜蔬菜礼盒");
                    product8.setDescription("精选新鲜蔬菜组合，绿色健康");
                    product8.setPrice(88.00);
                    product8.setStock(50);
                    product8.setStatus("ON_SALE");
                    product8.setCategoryId(specialCategory.getId());
                    product8.setFarmerId(1005L);
                    product8.setImageUrl("https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=fresh%20vegetable%20gift%20box&image_size=square");
                    product8.setOrigin("汕头市潮阳区西胪镇");
                    product8.setHarvestTime(java.time.LocalDate.now());
                    productService.save(product8);

                    // 商品9：西胪农家蜂蜜
                    LotusRootProduct product9 = new LotusRootProduct();
                    product9.setProductName("西胪农家蜂蜜");
                    product9.setDescription("纯天然蜂蜜，香甜可口");
                    product9.setPrice(58.00);
                    product9.setStock(80);
                    product9.setStatus("ON_SALE");
                    product9.setCategoryId(specialCategory.getId());
                    product9.setFarmerId(1005L);
                    product9.setImageUrl("https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=natural%20honey%20jar%20farm&image_size=square");
                    product9.setOrigin("汕头市潮阳区西胪镇");
                    product9.setHarvestTime(java.time.LocalDate.now());
                    productService.save(product9);
                }

            log.info("商品数据初始化完成");
        } catch (Exception e) {
            log.warn("初始化商品数据失败，可能是数据库表结构问题: {}", e.getMessage());
        }
    }
}
