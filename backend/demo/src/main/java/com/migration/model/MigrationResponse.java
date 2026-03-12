package com.migration.model;

/**
 * Response model for the Migration API.
 */
public class MigrationResponse {
    private boolean success;
    private String message;
    private String htmlContent;
    private String document360Response;
    private String articleId;

    public MigrationResponse() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }

    public String getDocument360Response() {
        return document360Response;
    }

    public void setDocument360Response(String document360Response) {
        this.document360Response = document360Response;
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public static MigrationResponse ok(String htmlContent) {
        MigrationResponse response = new MigrationResponse();
        response.setSuccess(true);
        response.setMessage("Success");
        response.setHtmlContent(htmlContent);
        return response;
    }

    public static MigrationResponse error(String message) {
        MigrationResponse response = new MigrationResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}
