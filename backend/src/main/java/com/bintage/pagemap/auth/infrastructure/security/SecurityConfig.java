package com.bintage.pagemap.auth.infrastructure.security;

import com.bintage.pagemap.auth.infrastructure.external.oauth2.client.OAuth2UserQueryService;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    @Value("${application.security.auth.sign-in.redirect-url}")
    private String oauth2AuthorizationRedirectUrl;

    private final List<RequestMatcher> permitApis = new ArrayList<>();
    private final List<RequestMatcher> privateApis = new ArrayList<>();
    private final List<IpAddressMatcher> permittedAddresses = new ArrayList<>();

    private final OAuth2UserQueryService oauth2UserService;
    private final OAuth2AuthenticationSuccessHandler authenticationSuccessHandler;
    private final OAuth2AuthorizationRequestService userAgentAuthorizationRepository;
    private final OAuth2LogoutHandler logoutHandler;
    private final JwtBearerAuthenticationFilter jwtBearerAuthenticationFilter;

    private void requestInspectorSetup() {
        permitApis.add(new AntPathRequestMatcher( "/oauth2/authorization/kakao"));
        permitApis.add(new AntPathRequestMatcher( "/login/oauth2/code/kakao"));
        permitApis.add(new AntPathRequestMatcher("/oauth/authorize"));
        permitApis.add(new AntPathRequestMatcher("/pagemap/global/status"));
        permitApis.add(new AntPathRequestMatcher(oauth2AuthorizationRedirectUrl));
        privateApis.add(new AntPathRequestMatcher("/api/token/refresh"));
        permittedAddresses.add(new IpAddressMatcher("127.0.0.1"));
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
        jwtBearerAuthenticationFilter.setRequestInspector(requestInspector());

        http
                .httpBasic(HttpBasicConfigurer::disable)
                .rememberMe(RememberMeConfigurer::disable)
                .sessionManagement(SessionManagementConfigurer::disable)
                .requestCache(RequestCacheConfigurer::disable)
                .rememberMe(RememberMeConfigurer::disable)
                .anonymous(AnonymousConfigurer::disable)
                .formLogin(FormLoginConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(request -> request
                        .requestMatchers(permitApis.toArray(new RequestMatcher[0])).permitAll()
                        .dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()
                        .anyRequest().authenticated()
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
