package com.company.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "service")
public class ServiceUrlProperties {

    private ServiceConfig user = new ServiceConfig();
    private ServiceConfig order = new ServiceConfig();
    private ServiceConfig auth = new ServiceConfig();

    public ServiceConfig getUser() {
        return user;
    }

    public void setUser(ServiceConfig user) {
        this.user = user;
    }

    public ServiceConfig getOrder() {
        return order;
    }

    public void setOrder(ServiceConfig order) {
        this.order = order;
    }

    public ServiceConfig getAuth() {
        return auth;
    }

    public void setAuth(ServiceConfig auth) {
        this.auth = auth;
    }

    public static class ServiceConfig {
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}

