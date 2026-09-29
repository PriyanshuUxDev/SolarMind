package com.solarmind.exception;
import jakarta.servlet.http.HttpServletRequest; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.time.Instant;
@RestControllerAdvice public class GlobalExceptionHandler {
  @ExceptionHandler(ResourceNotFoundException.class) ResponseEntity<ApiError> missing(ResourceNotFoundException e,HttpServletRequest r){return body(404,"Not Found",e.getMessage(),r);}
  @ExceptionHandler(NoSuitablePanelException.class) ResponseEntity<ApiError> noPanel(NoSuitablePanelException e,HttpServletRequest r){return body(422,"Unprocessable Entity",e.getMessage(),r);}
  @ExceptionHandler(AiServiceUnavailableException.class) ResponseEntity<ApiError> ai(AiServiceUnavailableException e,HttpServletRequest r){return body(503,"Service Unavailable",e.getMessage(),r);}
  @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class) ResponseEntity<ApiError> validation(org.springframework.web.bind.MethodArgumentNotValidException e,HttpServletRequest r){return body(400,"Bad Request","Request validation failed",r);}
  @ExceptionHandler(NotImplementedException.class) ResponseEntity<ApiError> notImpl(NotImplementedException e,HttpServletRequest r){return body(501,"Not Implemented",e.getMessage(),r);}
  @ExceptionHandler({InvalidInputException.class,IllegalArgumentException.class}) ResponseEntity<ApiError> bad(RuntimeException e,HttpServletRequest r){return body(400,"Bad Request",e.getMessage(),r);}
  @ExceptionHandler(Exception.class) ResponseEntity<ApiError> generic(Exception e,HttpServletRequest r){return body(500,"Internal Server Error","Unexpected server failure",r);}
  private ResponseEntity<ApiError> body(int s,String e,String m,HttpServletRequest r){return ResponseEntity.status(s).body(new ApiError(Instant.now(),s,e,m,r.getRequestURI()));}
  public record ApiError(Instant timestamp,int status,String error,String message,String path){}
}
