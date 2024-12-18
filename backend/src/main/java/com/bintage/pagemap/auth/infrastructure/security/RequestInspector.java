package com.bintage.pagemap.auth.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;
import java.util.Optional;

@Builder
public class RequestInspector {

    private final List<RequestMatcher> permitApis;
    private final List<RequestMatcher> privateApiMatchers;
    private final List<IpAddressMatcher> permittedAddresses;

    public boolean isRefreshTokenApi(HttpServletRequest request) {
        return privateApiMatchers.stream().anyMatch(privateApiMatcher -> privateApiMatcher.matches(request));
    }

    public boolean isPrivateApi(HttpServletRequest request) {
        return privateApiMatchers.stream().anyMatch(privateApiMatcher -> privateApiMatcher.matches(request));
    }

    public boolean isPermittedAddress(HttpServletRequest request) {
        return privateApiMatchers.stream().anyMatch(privateApiMatcher -> privateApiMatcher.matches(request))
                && permittedAddresses.stream().anyMatch(ipAddressMatcher -> ipAddressMatcher.matches(request));
    }

    public boolean isPermitApi(HttpServletRequest request) {
        return permitApis.stream()
                .anyMatch(requestMatcher -> requestMatcher.matches(request));
    }

    public static Optional<String> findTokenByHttpHeader(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return Optional.of(authorization.substring(7));
        }
        return Optional.empty();
    }

}
