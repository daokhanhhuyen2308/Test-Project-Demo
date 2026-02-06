package com.august.shared.exception;

import com.august.shared.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;


@Component
public class CustomAccessDenied implements AccessDeniedHandler {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @NullMarked
  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException {

    ErrorCode errorCode = ErrorCode.ACCESS_DENIED;

    CustomAuthEntryPoint.writeErrorResponse(response, errorCode, objectMapper);
  }
}
