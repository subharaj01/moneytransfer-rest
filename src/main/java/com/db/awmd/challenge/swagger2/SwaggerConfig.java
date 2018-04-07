package com.db.awmd.challenge.swagger2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * open http://localhost:18080/swagger-ui.html after running the application
 * 
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {// extends WebMvcConfigurerAdapter{
	@Bean
	public Docket productApi() {
		return new Docket(DocumentationType.SWAGGER_2).select()
				.apis(RequestHandlerSelectors.basePackage("com.db.awmd.challenge.web"))
				// .apis(RequestHandlerSelectors.any())
				// .apis(Predicates.not(RequestHandlerSelectors.basePackage("org.springframework.boot")))
				.paths(PathSelectors.any())
				// .apis(RequestHandlerSelectors.basePackage("com.db.awmd.challenge.web.AccountsController"))
				// .paths(PathSelectors.ant("/v1/accounts/transaction"))
				.build().apiInfo(metaData());
	}

	/**
	 * this method is just for extra meatdata info in swagger ui page
	 * 
	 * @return
	 */
	private ApiInfo metaData() {
		return new ApiInfoBuilder().title("Spring Boot REST API for money transfer")
				.description("\"Spring Boot REST API for money transfer\"").version("1.0.0")
				// .license("Apache License Version 2.0")
				// .licenseUrl("https://www.apache.org/licenses/LICENSE-2.0\"")
				.contact(new Contact("Subharaj Saha", "", "")).build();
	}

	// @Override
	// public void addResourceHandlers(ResourceHandlerRegistry registry)
	// {
	// //enabling swagger-ui part for visual documentation
	// registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
	// registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
	// }
}