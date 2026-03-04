package com.august.sharesecurity.exception;

import com.august.sharecore.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;


@RequiredArgsConstructor
public class CustomAccessDenied implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException {

    ErrorCode errorCode = ErrorCode.ACCESS_DENIED;

    CustomAuthEntryPoint.writeErrorResponse(response, errorCode, objectMapper, accessDeniedException);
  }
}
