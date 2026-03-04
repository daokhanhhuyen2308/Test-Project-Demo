package com.august.sharesecurity.exception;

import com.august.sharecore.dto.ApiResponse;
import com.august.sharecore.enums.ErrorCode;
import com.august.sharecore.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;


@RequiredArgsConstructor
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  @Override
  public void commence(
          HttpServletRequest request,
          HttpServletResponse response,
          AuthenticationException authException)
          throws IOException {

    ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;

    writeErrorResponse(response, errorCode, objectMapper, authException);
  }

  static void writeErrorResponse(HttpServletResponse response, ErrorCode errorCode,
                                 ObjectMapper objectMapper, Exception e) throws IOException {

    ResponseEntity<ApiResponse<?>> res = GlobalExceptionHandler.buildResponse(errorCode, e);

    response.setStatus(errorCode.getStatusCode());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    response.getWriter().write(objectMapper.writeValueAsString(res));
    response.flushBuffer();
  }

}
