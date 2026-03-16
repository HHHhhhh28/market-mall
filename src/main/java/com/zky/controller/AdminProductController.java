package com.zky.controller;

import com.github.pagehelper.PageInfo;
import com.zky.common.response.Response;
import com.zky.domain.dto.AdminProductRequestDTO;
import com.zky.domain.dto.ProductInfoRequestDTO;
import com.zky.domain.po.ProductInfo;
import com.zky.domain.vo.ProductVO;
import com.zky.service.IProductService;
import com.zky.dao.ProductDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/mall/admin/product")
public class AdminProductController {

    @Resource
    private IProductService productService;

    @Resource
    private ProductDao productDao;

    @PostMapping("/list")
    public Response<PageInfo<ProductVO>> list(@RequestBody ProductInfoRequestDTO request) {
        log.info("接口 {} 被调用了", "/api/v1/mall/admin/product/list");
        PageInfo<ProductVO> pageInfo = productService.getProductList(request);
        return Response.<PageInfo<ProductVO>>builder().code("0000").info("Success").data(pageInfo).build();
    }

    @PostMapping("/create")
    public Response<Void> create(@RequestBody AdminProductRequestDTO request) {
        log.info("接口 {} 被调用了", "/api/v1/mall/admin/product/create");
        
        // 校验必填字段
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return Response.<Void>builder().code("0001").info("商品名称不能为空").build();
        }
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return Response.<Void>builder().code("0001").info("商品价格不能为空且必须大于0").build();
        }
        if (request.getStock() == null || request.getStock() < 0) {
            return Response.<Void>builder().code("0001").info("商品库存不能为空且不能为负数").build();
        }
        if (request.getCategory() == null || request.getCategory().trim().isEmpty()) {
            return Response.<Void>builder().code("0001").info("商品品类不能为空").build();
        }
        if (request.getBrand() == null || request.getBrand().trim().isEmpty()) {
            return Response.<Void>builder().code("0001").info("商品品牌不能为空").build();
        }
        if (request.getImageUrl() == null || request.getImageUrl().trim().isEmpty()) {
            return Response.<Void>builder().code("0001").info("商品图片不能为空").build();
        }
        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            return Response.<Void>builder().code("0001").info("商品描述不能为空").build();
        }
        if (request.getKeywords() == null || request.getKeywords().trim().isEmpty()) {
            return Response.<Void>builder().code("0001").info("商品关键词不能为空").build();
        }
        if (request.getUserTags() == null || request.getUserTags().trim().isEmpty()) {
            return Response.<Void>builder().code("0001").info("用户标签不能为空").build();
        }
        
        ProductInfo entity = new ProductInfo();
        BeanUtils.copyProperties(request, entity);
        entity.setProductId(UUID.randomUUID().toString());
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        productDao.insert(entity);
        return Response.<Void>builder().code("0000").info("Success").build();
    }

    @PostMapping("/update")
    public Response<Void> update(@RequestBody AdminProductRequestDTO request) {
        log.info("接口 {} 被调用了", "/api/v1/mall/admin/product/update");
        ProductInfo existing = productDao.selectByProductId(request.getProductId());
        if (existing == null) {
            throw new RuntimeException("商品不存在");
        }
        ProductInfo entity = new ProductInfo();
        BeanUtils.copyProperties(existing, entity);
        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            entity.setPrice(request.getPrice());
        }
        if (request.getStock() != null) {
            entity.setStock(request.getStock());
        }
        if (request.getImageUrl() != null) {
            entity.setImageUrl(request.getImageUrl());
        }
        if (request.getCategory() != null) {
            entity.setCategory(request.getCategory());
        }
        if (request.getBrand() != null) {
            entity.setBrand(request.getBrand());
        }
        if (request.getKeywords() != null) {
            entity.setKeywords(request.getKeywords());
        }
        if (request.getUserTags() != null) {
            entity.setUserTags(request.getUserTags());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        productDao.update(entity);
        return Response.<Void>builder().code("0000").info("Success").build();
    }

    @DeleteMapping("/delete")
    public Response<Void> delete(@RequestParam String productId) {
        log.info("接口 {} 被调用了", "/api/v1/mall/admin/product/delete");
        productDao.deleteByProductId(productId);
        return Response.<Void>builder().code("0000").info("Success").build();
    }

    @PostMapping("/upload")
    public Response<String> upload(@RequestParam("file") MultipartFile file) {
        log.info("接口 {} 被调用了", "/api/v1/mall/admin/product/upload");
        if (file == null || file.isEmpty()) {
            return Response.<String>builder().code("0001").info("文件不能为空").build();
        }
        
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = FilenameUtils.getExtension(originalFilename);
            String newFileName = UUID.randomUUID().toString() + "." + extension;
            
            // 获取文件字节数组
            byte[] fileBytes = file.getBytes();
            
            // 上传到后台项目的public/images
            String backgroundUploadPath = "/Users/iniesta/study/idea_project/geraduation_design/market-mall-front-background/public/images";
            File backgroundUploadDir = new File(backgroundUploadPath);
            if (!backgroundUploadDir.exists()) {
                backgroundUploadDir.mkdirs();
            }
            Path backgroundFilePath = Paths.get(backgroundUploadPath, newFileName);
            Files.write(backgroundFilePath, fileBytes);
            
            // 上传到前台项目的public/images
            String frontUploadPath = "/Users/iniesta/study/idea_project/geraduation_design/market-mall-front/public/images";
            File frontUploadDir = new File(frontUploadPath);
            if (!frontUploadDir.exists()) {
                frontUploadDir.mkdirs();
            }
            Path frontFilePath = Paths.get(frontUploadPath, newFileName);
            Files.write(frontFilePath, fileBytes);
            
            log.info("文件上传成功: {}, {}", backgroundFilePath.toString(), frontFilePath.toString());
            
            String imageUrl = "/images/" + newFileName;
            return Response.<String>builder().code("0000").info("Success").data(imageUrl).build();
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Response.<String>builder().code("0001").info("文件上传失败").build();
        }
    }
}

