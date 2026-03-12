package com.migration.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration mapping for Document360 application properties.
 */
@Configuration
public class Document360Config {

    @Value("${document360.api.key}")
    private String apiKey;

    @Value("${document360.api.base-url}")
    private String baseUrl;

    @Value("${document360.project.version-id}")
    private String projectVersionId;

    @Value("${document360.user-id}")
    private String userId;

    @Value("${document360.category-id}")
    private String categoryId;

    public String getApiKey() {
        return apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getProjectVersionId() {
        return projectVersionId;
    }

    public String getUserId() {
        return userId;
    }

    public String getCategoryId() {
        return categoryId;
    }
}
