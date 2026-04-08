package com.zky.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 图片存储目录（外部目录，运行时新增文件无需重启即可访问）
     * 使用项目根目录下的 upload-images 文件夹
     */
    public static final String IMAGE_STORE_PATH =
            "/Users/iniesta/study/idea_project/geraduation_design/market-mall/upload-images/";

    /** 图片访问前缀 */
    public static final String IMAGE_URL_PREFIX = "http://localhost:8099/images/";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射外部文件系统目录，运行时新增图片无需重启立即可访问
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + IMAGE_STORE_PATH);
    }
}
