package com.bookfair.api_gateway.config;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.AntPathMatcher;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security")
public class GatewaySecurityProperties {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /**
     * Paths that should skip JWT validation, e.g. auth endpoints used to obtain tokens.
     */
    private List<String> publicPaths = new ArrayList<>();

    public boolean isPublic(String path) {
        final String candidate = path == null ? "" : path;
        for (String pattern : publicPaths) {
            if (pattern != null && PATH_MATCHER.match(pattern, candidate)) {
                return true;
            }
        }
        return false;
    }
}
