package com.migration.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Payload model for Document360 Article Creation API.
 */
public class ArticlePayload {

    private String title;
    private String content;

    @JsonProperty("project_version_id")
    private String projectVersionId;

    @JsonProperty("category_id")
    private String categoryId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("order")
    private int order = 1;

    @JsonProperty("content_type")
    private String contentType = null;

    @JsonProperty("slug")
    private String slug = null;

    public ArticlePayload() {
    }

    public ArticlePayload(String title, String content, String projectVersionId, String categoryId, String userId) {
        this.title = title;
        this.content = content;
        this.projectVersionId = projectVersionId;
        this.categoryId = categoryId;
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getProjectVersionId() {
        return projectVersionId;
    }

    public void setProjectVersionId(String projectVersionId) {
        this.projectVersionId = projectVersionId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
}
