package com.bintage.pagemap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import java.util.List;

@Configuration(
        proxyBeanMethods = false
)
@Profile("test")
public class MvcTestConfig implements WebMvcConfigurer {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity testSecurity) throws Exception {
        testSecurity
                .httpBasic(HttpBasicConfigurer::disable)
                .csrf(CsrfConfigurer::disable)
                .cors(CorsConfigurer::disable)
                .sessionManagement(SessionManagementConfigurer::disable)
                .formLogin(FormLoginConfigurer::disable)
                .rememberMe(RememberMeConfigurer::disable)
                .authorizeHttpRequests(configurer -> {
                            String user = "USER";
                            configurer
                                    .requestMatchers("/storage/maps").hasRole(user)
                                    .requestMatchers("/storage/maps/**").hasRole(user)
                                    .requestMatchers("/storage/webpages").hasRole(user)
                                    .requestMatchers("/storage/webpages/**").hasRole(user)
                                    .requestMatchers("/storage/trash/maps/*").hasRole(user)
                                    .requestMatchers("/storage/trash/webpages/*").hasRole(user)
                                    .requestMatchers("/storage/categories").hasRole(user)
                                    .requestMatchers("/storage/categories/**").hasRole(user)
                                    .anyRequest().denyAll();
                        }
                );

        return testSecurity.build();
    }

    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.removeIf(resolver -> resolver instanceof DefaultHandlerExceptionResolver);
    }
}
