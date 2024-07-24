package com.bintage.pagemap.auth.infrastructure.security;

import com.bintage.pagemap.auth.infrastructure.external.oauth2.client.SimpleOAuth2UserService;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final List<RequestMatcher> permitApis = new ArrayList<>();
    private final List<RequestMatcher> privateApis = new ArrayList<>();
    private final List<IpAddressMatcher> permittedAddresses = new ArrayList<>();

    private final SimpleOAuth2UserService oauth2UserServiceSimple;
    private final OAuth2AuthenticationSuccessHandler authenticationSuccessHandler;
    private final SimpleOAuth2AuthorizationRequestRepository simpleOAuth2AuthorizationRequestRepository;
    private final OAuth2LogoutHandler logoutHandler;
    private final SimpleJwtBearerAuthenticationFilter simpleJwtBearerAuthenticationFilter;

    private void requestInspectorSetup() {
        permitApis.add(new AntPathRequestMatcher( "/api/oauth2/authorization/kakao"));
        permitApis.add(new AntPathRequestMatcher( "/api/login/oauth2/code/kakao"));
        permitApis.add(new AntPathRequestMatcher("/api/pagemap/global/status"));
        privateApis.add(new AntPathRequestMatcher("/api/token/refresh"));
    }

    @Bean
    public RequestInspector requestInspector() {
        requestInspectorSetup();
        return RequestInspector.builder()
                .permitApis(permitApis)
                .privateApiMatchers(privateApis)
                .permittedAddresses(permittedAddresses)
                .build();
    }

    @Profile("prod")
    @Bean
    public CorsConfigurationSource productionCorsConfigurationSource() {
        return request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedHeaders(Collections.singletonList("*"));
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
            config.setAllowedOriginPatterns(List.of("https://pagemap.net"));
            config.setAllowCredentials(true);
            return config;
        };
    }

    @Profile("!prod")
    @Primary
    @Bean
    public CorsConfigurationSource localCorsConfigurationSource() {
        return request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedHeaders(Collections.singletonList("*"));
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
            config.setAllowedOriginPatterns(List.of("http://localhost:3000"));
            config.setAllowCredentials(true);
            return config;
        };
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        simpleJwtBearerAuthenticationFilter.setRequestInspector(requestInspector());

        http
                .httpBasic(HttpBasicConfigurer::disable)
                .rememberMe(RememberMeConfigurer::disable)
                .sessionManagement(SessionManagementConfigurer::disable)
                .requestCache(RequestCacheConfigurer::disable)
                .rememberMe(RememberMeConfigurer::disable)
                .anonymous(AnonymousConfigurer::disable)
                .formLogin(FormLoginConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource))
                .authorizeHttpRequests(request -> request
                        .requestMatchers(permitApis.toArray(new RequestMatcher[0])).permitAll()
                        .dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(simpleJwtBearerAuthenticationFilter, OAuth2AuthorizationRequestRedirectFilter.class)
                .oauth2Login(loginConfigurer -> loginConfigurer
                        .loginProcessingUrl("/api/login/oauth2/code/*")
                        .userInfoEndpoint(config -> config.userService(oauth2UserServiceSimple))
                        .successHandler(authenticationSuccessHandler)
                        .authorizationEndpoint(config -> {
                            config.baseUri("/api/oauth2/authorization");
                            config.authorizationRequestRepository(simpleOAuth2AuthorizationRequestRepository);
                        })
                )
                .logout(logoutconfigurer -> logoutconfigurer
                        .logoutUrl("/api/account/sign-out")
                        .addLogoutHandler(logoutHandler)
                        .clearAuthentication(true)
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );
        return http.build();
    }

}
