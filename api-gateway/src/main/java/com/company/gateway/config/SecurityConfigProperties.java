package com.company.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Security Configuration Properties
 * Binds security configuration from application.properties
 *
 * Usage in code:
 * @Autowired
 * private SecurityConfigProperties securityConfig;
 *
 * String[] publicPaths = securityConfig.getPublicPaths();
 */
@Component
@ConfigurationProperties(prefix = "security")
public class SecurityConfigProperties {

    private String[] publicPaths = {};
    private CsrfConfig csrf = new CsrfConfig();
    private HttpBasicConfig httpBasic = new HttpBasicConfig();

    public String[] getPublicPaths() {
        return publicPaths;
    }

    public void setPublicPaths(String[] publicPaths) {
        this.publicPaths = publicPaths;
    }

    public CsrfConfig getCsrf() {
        return csrf;
    }

    public void setCsrf(CsrfConfig csrf) {
        this.csrf = csrf;
    }

    public HttpBasicConfig getHttpBasic() {
        return httpBasic;
    }

    public void setHttpBasic(HttpBasicConfig httpBasic) {
        this.httpBasic = httpBasic;
    }

    public static class CsrfConfig {
        private boolean enabled = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class HttpBasicConfig {
        private boolean enabled = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}

