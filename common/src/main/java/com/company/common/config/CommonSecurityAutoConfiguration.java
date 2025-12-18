package com.company.common.config;

import com.company.common.security.JwtTokenExtractionFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto Configuration for Common Security Components
 *
 * This configuration class imports shared security components
 * from the common module into any service that depends on it.
 *
 * Services that import this will automatically get:
 * - JwtTokenExtractionFilter
 *
 * Usage:
 * @Import(CommonSecurityAutoConfiguration.class)
 * or
 * Just add dependency - Spring Boot will auto-register via spring.factories
 */
@Configuration
@Import(JwtTokenExtractionFilter.class)
public class CommonSecurityAutoConfiguration {
    // Auto-configuration for security components
}

