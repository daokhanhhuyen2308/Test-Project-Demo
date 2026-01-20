package com.example.authenticate.exception;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;


@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException {
    ApiExceptionResponse apiHandleResponse = new ApiExceptionResponse();

    CustomError customError =
        CustomError.builder()
            .code(403)
            .path(request.getRequestURI())
            .timestamp(Instant.now())
            .message("You do not have permission to access this resource")
            .build();
    apiHandleResponse.setError(customError);

    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().write(objectMapper.writeValueAsString(apiHandleResponse));
    response.flushBuffer();
  }
}
