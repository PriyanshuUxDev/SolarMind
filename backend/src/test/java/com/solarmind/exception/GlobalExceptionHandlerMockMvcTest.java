package com.solarmind.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class GlobalExceptionHandlerMockMvcTest {
  private MockMvc mvc;

  @BeforeEach
  void setUp() {
    mvc = MockMvcBuilders.standaloneSetup(new FailingController())
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
  }

  @Test
  void mapsRequestedErrorStatusesToApiError() throws Exception {
    mvc.perform(get("/fail/missing-parameter")).andExpect(status().isBadRequest());
    mvc.perform(get("/fail/unreadable")).andExpect(status().isBadRequest());
    mvc.perform(get("/fail/type-mismatch")).andExpect(status().isBadRequest());
    mvc.perform(get("/fail/no-resource")).andExpect(status().isNotFound());
    mvc.perform(get("/fail/method")).andExpect(status().isMethodNotAllowed());
    mvc.perform(get("/fail/integrity")).andExpect(status().isConflict());
    mvc.perform(get("/fail/credentials"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Invalid email or password"));
  }

  @Test
  void includesFieldNameInValidationMessage() throws Exception {
    mvc.perform(post("/fail/validate").contentType("application/json").content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("monthlyConsumption: must not be null"));
  }

  @RestController
  static class FailingController {
    @GetMapping("/fail/missing-parameter")
    void missingParameter() throws MissingServletRequestParameterException {
      throw new MissingServletRequestParameterException("query", "String");
    }

    @GetMapping("/fail/unreadable")
    void unreadable() {
      throw new HttpMessageNotReadableException("bad body");
    }

    @GetMapping("/fail/type-mismatch")
    void typeMismatch() {
      throw new MethodArgumentTypeMismatchException("bad", String.class, "id", null, null);
    }

    @GetMapping("/fail/no-resource")
    void noResource() throws NoResourceFoundException {
      throw new NoResourceFoundException(HttpMethod.GET, "/missing");
    }

    @GetMapping("/fail/method")
    void method() throws HttpRequestMethodNotSupportedException {
      throw new HttpRequestMethodNotSupportedException("PATCH");
    }

    @GetMapping("/fail/integrity")
    void integrity() {
      throw new DataIntegrityViolationException("duplicate");
    }

    @GetMapping("/fail/credentials")
    void credentials() {
      throw new InvalidCredentialsException();
    }

    @PostMapping("/fail/validate")
    void validate(@Valid @RequestBody Payload payload) {}

    record Payload(@NotNull Integer monthlyConsumption) {}
  }
}
