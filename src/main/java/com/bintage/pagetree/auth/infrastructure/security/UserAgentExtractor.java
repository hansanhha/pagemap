package com.bintage.pagetree.auth.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import ua_parser.Client;
import ua_parser.Parser;

@Component
public class UserAgentExtractor {

    private final Parser userAgentParser = new Parser();

    public UserAgentInfo extract(HttpServletRequest request) {
        var userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        Client parsed = userAgentParser.parse(userAgent);

        if (isMobile(parsed.device.family)) {
            return new UserAgentInfo("MOBILE", parsed.os.family, parsed.device.family, parsed.userAgent.family);
        }

        if (parsed.os.family.equalsIgnoreCase("MAC OS X")) {
            return new UserAgentInfo("DESKTOP", "MAC_OS_X", parsed.device.family, parsed.userAgent.family);
        }

        return new UserAgentInfo("DESKTOP", parsed.os.family, parsed.device.family, parsed.userAgent.family);
    }

    private boolean isMobile(String device) {
        return switch (device) {
            case "iPhone", "iPad", "iPod", "Android" -> true;
            default -> false;
        };
    }

    public record UserAgentInfo(String type, String os, String device, String application) {}
}
