package com.migration.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.migration.config.Document360Config;
import com.migration.model.ArticlePayload;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class Document360Service {

    private static final Logger logger = Logger.getLogger(Document360Service.class.getName());

    private final Document360Config config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public Document360Service(Document360Config config) {
        this.config = config;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public String createArticle(String title, String htmlContent) throws Exception {
        String url = config.getBaseUrl() + "/Articles";
        
        ArticlePayload payload = new ArticlePayload();
        payload.setTitle(title);
        payload.setContent(htmlContent);
        payload.setProjectVersionId(config.getProjectVersionId());
        payload.setCategoryId(config.getCategoryId());
        payload.setUserId(config.getUserId());
        payload.setOrder(1);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api_token", config.getApiKey());
        
        try {
            String jsonBody = objectMapper.writeValueAsString(payload);
            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);
            
            logger.info("Sending POST request to Document360: " + url);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            String responseBody = response.getBody();
            
            logger.info("Response Status: " + response.getStatusCode());
            logger.info("Response Body: " + responseBody);
            
            return responseBody;
        } catch (HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            logger.log(Level.SEVERE, "Document360 API error: HTTP " + e.getStatusCode() + " - " + errorBody, e);
            throw new Exception("Document360 API error: " + errorBody, e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error communicating with Document360", e);
            throw new Exception("Unexpected error communicating with Document360: " + e.getMessage(), e);
        }
    }

    public String extractArticleId(String responseJson) {
        if (responseJson == null || responseJson.isEmpty()) {
            return null;
        }
        try {
            JsonNode rootNode = objectMapper.readTree(responseJson);
            
            // Check for top-level id
            if (rootNode.has("id")) {
                return rootNode.get("id").asText();
            }
            
            // Check for data.id
            JsonNode dataNode = rootNode.get("data");
            if (dataNode != null && dataNode.has("id")) {
                return dataNode.get("id").asText();
            }
            
            return null;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to extract article ID from response JSON", e);
            return null;
        }
    }
}
