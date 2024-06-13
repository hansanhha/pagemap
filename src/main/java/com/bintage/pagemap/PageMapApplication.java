package com.bintage.pagemap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import java.util.List;

@SpringBootApplication
public class PageMapApplication {

    public static void main(String[] args) {
        SpringApplication.run(PageMapApplication.class, args);
    }

}

@Configuration
class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.removeIf(resolver -> resolver instanceof DefaultHandlerExceptionResolver);
    }
}