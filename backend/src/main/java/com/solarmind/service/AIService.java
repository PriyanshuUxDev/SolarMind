package com.solarmind.service;

import com.solarmind.config.AiProperties;
import com.solarmind.dto.request.AskRequest;
import com.solarmind.dto.response.AskResponse;
import com.solarmind.exception.AiServiceUnavailableException;
import java.util.*;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;

@Service
public class AIService {
  private static final Logger log = LoggerFactory.getLogger(AIService.class);
  private final RestClient client;
  private final AiProperties properties;
  private final AssessmentService assessments;

  public AIService(RestClient client, AiProperties properties, AssessmentService assessments) {
    this.client = client;
    this.properties = properties;
    this.assessments = assessments;
  }

  public AskResponse ask(AskRequest r) {
    try {
      Object context = r.assessmentId() == null ? Map.of() : assessments.get(r.assessmentId());
      return client
          .post()
          .uri(properties.serviceUrl() + "/api/ai/ask")
          .header("X-Internal-Token", properties.internalToken())
          .body(Map.of("question", r.question(), "context", context))
          .retrieve()
          .body(AskResponse.class);
    } catch (Exception e) {
      log.warn("AI service request failed: {}", e.getClass().getSimpleName(), e);
      throw new AiServiceUnavailableException(
          "AI service is unavailable. Check that the AI service is running and configured.");
    }
  }
}
