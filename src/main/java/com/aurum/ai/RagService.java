package com.aurum.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Service
public class RagService {

    @Value("${rag.service.url}")
    private String ragServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();


    public Map<String, Object> generateInsight(
            String insightType,
            int topK,
            Map<String, Object> portfolioMetrics
    ) {

        String url = ragServiceUrl + "/generate-insight";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = Map.of(
                    "insight_type", insightType,
                    "top_k", topK,
                    "portfolio_metrics", portfolioMetrics
            );

            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(requestBody, headers);

            Map<String, Object> response = restTemplate.postForObject(
                    url,
                    request,
                    Map.class
            );

            return response != null
                    ? response
                    : Collections.emptyMap();

        } catch (RestClientException exception) {

            System.err.println(
                    "RAG/AI service unavailable: " + exception.getMessage()
            );

            return Collections.emptyMap();
        }
    }
}