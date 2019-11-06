package io.hpb.contract.configure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration //Must quote
@EnableSwagger2 //Must quote
@EnableWebMvc //Must quote
@ComponentScan(basePackages = {"io.hpb.contract.controller"})
public class SwaggerConfig{
    @Bean
    public Docket customDocket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        Contact contact = new Contact("gene", "http://www.hpb.io/", "gene@hpb.io");
        return new ApiInfoBuilder()
                .title("HPB Block Chain Middleware API Interface")
                .description("Unified Interface Service of HPB Block Chain Middleware API")
                .contact(contact)
                .version("1.1.0")
                .build();
    }
}
