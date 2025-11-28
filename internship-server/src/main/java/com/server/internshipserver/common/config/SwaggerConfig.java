package com.server.internshipserver.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * Swagger API文档配置类
 * 配置Swagger2，用于生成和展示RESTful API文档
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    /**
     * 创建Swagger Docket Bean
     * 配置API文档的基本信息和扫描路径
     * 
     * @return Docket Swagger文档对象
     */
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.server.internshipserver.controller"))
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts());
    }

    /**
     * 配置API文档的基本信息
     * 
     * @return ApiInfo API信息对象
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("高校实习过程跟踪与评价管理系统 API文档")
                .description("高校实习过程跟踪与评价管理系统接口文档")
                .version("1.0.0")
                .contact(new Contact("开发团队", "", ""))
                .build();
    }

    /**
     * 配置安全模式
     * 指定JWT Token通过Authorization请求头传递
     * 
     * @return List<SecurityScheme> 安全模式列表
     */
    private List<SecurityScheme> securitySchemes() {
        List<SecurityScheme> apiKeyList = new ArrayList<>();
        apiKeyList.add(new ApiKey("Authorization", "Authorization", "header"));
        return apiKeyList;
    }

    /**
     * 配置安全上下文
     * 为所有API接口应用JWT认证
     * 
     * @return List<SecurityContext> 安全上下文列表
     */
    private List<SecurityContext> securityContexts() {
        List<SecurityContext> securityContexts = new ArrayList<>();
        securityContexts.add(
                SecurityContext.builder()
                        .securityReferences(defaultAuth())
                        .operationSelector(operationContext -> true)
                        .build());
        return securityContexts;
    }

    /**
     * 配置默认的安全引用
     * 设置全局访问权限范围
     * 
     * @return List<SecurityReference> 安全引用列表
     */
    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        List<SecurityReference> securityReferences = new ArrayList<>();
        securityReferences.add(new SecurityReference("Authorization", authorizationScopes));
        return securityReferences;
    }
}

