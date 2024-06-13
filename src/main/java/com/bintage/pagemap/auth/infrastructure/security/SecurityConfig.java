package com.bintage.pagemap.auth.infrastructure.security;

import com.bintage.pagemap.auth.infrastructure.external.oauth2.client.OAuth2UserQueryService;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
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

    private final List<RequestMatcher> authRequireRequests = new ArrayList<>();
    private final List<RequestMatcher> privateApis = new ArrayList<>();
    private final List<IpAddressMatcher> permittedAddresses = new ArrayList<>();
    private final OAuth2UserQueryService oauth2UserService;
    private final OAuth2AuthenticationSuccessHandler authenticationSuccessHandler;
    private final OAuth2AuthorizationRequestService userAgentAuthorizationRepository;
    private final OAuth2LogoutHandler logoutHandler;
    private final JwtBearerAuthenticationFilter jwtBearerAuthenticationFilter;

    {
        authRequireRequests.add(new AntPathRequestMatcher( "/account/me"));
        authRequireRequests.add(new AntPathRequestMatcher("/account/sign-out"));
        authRequireRequests.add(new AntPathRequestMatcher("/account/me/devices"));
        authRequireRequests.add(new AntPathRequestMatcher("/account/me/devices/*"));
        privateApis.add(new AntPathRequestMatcher("/api/token/refresh"));
        permittedAddresses.add(new IpAddressMatcher("127.0.0.1"));
    }

    @Bean
    public RequestInspector requestInspector() {
        return new RequestInspector(authRequireRequests, privateApis, permittedAddresses);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedHeaders(Collections.singletonList("*"));
            config.setAllowedMethods(Collections.singletonList("*"));
            config.setAllowedOriginPatterns(List.of("*"));
            config.setAllowCredentials(true);
            return config;
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        jwtBearerAuthenticationFilter.setAuthRequireRequests(requestInspector());

        http
                .httpBasic(HttpBasicConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(request -> request
                        .requestMatchers(authRequireRequests.toArray(new RequestMatcher[0])).authenticated()
                        .dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtBearerAuthenticationFilter, OAuth2AuthorizationRequestRedirectFilter.class)
                .oauth2Login(loginConfigurer -> loginConfigurer
                        .userInfoEndpoint(config -> config.userService(oauth2UserService))
                        .successHandler(authenticationSuccessHandler)
                        .authorizationEndpoint(config -> config.authorizationRequestRepository(userAgentAuthorizationRepository))
                )
                .logout(logoutconfigurer -> logoutconfigurer
                        .logoutUrl("/account/sign-out")
                        .addLogoutHandler(logoutHandler)
                        .clearAuthentication(true)
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );
        return http.build();
    }

}
