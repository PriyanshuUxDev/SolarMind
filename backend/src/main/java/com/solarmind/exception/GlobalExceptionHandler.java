package com.solarmind.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  @ExceptionHandler(ResourceNotFoundException.class)
  ResponseEntity<ApiError> missing(ResourceNotFoundException e, HttpServletRequest r) {
    return body(404, "Not Found", e.getMessage(), r);
  }

  @ExceptionHandler(NoSuitablePanelException.class)
  ResponseEntity<ApiError> noPanel(NoSuitablePanelException e, HttpServletRequest r) {
    return body(422, "Unprocessable Entity", e.getMessage(), r);
  }

  @ExceptionHandler(AiServiceUnavailableException.class)
  ResponseEntity<ApiError> ai(AiServiceUnavailableException e, HttpServletRequest r) {
    return body(503, "Service Unavailable", e.getMessage(), r);
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  ResponseEntity<ApiError> credentials(InvalidCredentialsException e, HttpServletRequest r) {
    return body(401, "Unauthorized", e.getMessage(), r);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  ResponseEntity<ApiError> missingParameter(MissingServletRequestParameterException e, HttpServletRequest r) {
    return body(400, "Bad Request", "Missing request parameter: " + e.getParameterName(), r);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<ApiError> unreadable(HttpMessageNotReadableException e, HttpServletRequest r) {
    return body(400, "Bad Request", "Malformed request body", r);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  ResponseEntity<ApiError> typeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest r) {
    return body(400, "Bad Request", "Invalid value for " + e.getName(), r);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  ResponseEntity<ApiError> noResource(NoResourceFoundException e, HttpServletRequest r) {
    return body(404, "Not Found", "Resource not found", r);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  ResponseEntity<ApiError> methodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest r) {
    return body(405, "Method Not Allowed", "Request method not supported", r);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<ApiError> dataIntegrity(DataIntegrityViolationException e, HttpServletRequest r) {
    return body(409, "Conflict", "Request conflicts with existing data", r);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiError> validation(
      MethodArgumentNotValidException e, HttpServletRequest r) {
    String message = e.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .findFirst()
        .orElse("Request validation failed");
    return body(400, "Bad Request", message, r);
  }

  @ExceptionHandler(NotImplementedException.class)
  ResponseEntity<ApiError> notImpl(NotImplementedException e, HttpServletRequest r) {
    return body(501, "Not Implemented", e.getMessage(), r);
  }

  @ExceptionHandler({InvalidInputException.class, IllegalArgumentException.class})
  ResponseEntity<ApiError> bad(RuntimeException e, HttpServletRequest r) {
    return body(400, "Bad Request", e.getMessage(), r);
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiError> generic(Exception e, HttpServletRequest r) {
    log.error("Unhandled request failure at {}", r.getRequestURI(), e);
    return body(500, "Internal Server Error", "Unexpected server failure", r);
  }

  private ResponseEntity<ApiError> body(int s, String e, String m, HttpServletRequest r) {
    return ResponseEntity.status(s).body(new ApiError(Instant.now(), s, e, m, r.getRequestURI()));
  }

  public record ApiError(
      Instant timestamp, int status, String error, String message, String path) {}
}
