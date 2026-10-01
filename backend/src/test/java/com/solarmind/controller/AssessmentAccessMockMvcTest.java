package com.solarmind.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.solarmind.exception.GlobalExceptionHandler;
import com.solarmind.exception.ResourceNotFoundException;
import com.solarmind.service.AssessmentService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AssessmentAccessMockMvcTest {
  @Test
  void crossUserAssessmentAccessIsNotFoundOverHttp() throws Exception {
    AssessmentService service = new AssessmentService(null, null, null, null, null) {
      @Override
      public com.solarmind.dto.response.AssessmentResponse get(Long id) {
        throw new ResourceNotFoundException("Assessment not found");
      }
    };
    MockMvc mvc = MockMvcBuilders.standaloneSetup(new AssessmentController(service))
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();

    mvc.perform(get("/api/assessments/12"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Assessment not found"));
  }
}
