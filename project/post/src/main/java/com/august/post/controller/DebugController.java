package com.august.post.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Enumeration;

@RestController
public class DebugController {

    private static final Logger log = LoggerFactory.getLogger(DebugController.class);
    @GetMapping("/api/debug/headers")
    public String debugHeaders(HttpServletRequest request) {
            log.info("DEBUG");
            StringBuilder sb = new StringBuilder();
            sb.append("=== DỮ LIỆU TỪ KONG GỬI SANG ===\n");

            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                sb.append(headerName).append(": ").append(headerValue).append("\n");
                log.info("Header: {} = {}", headerName, headerValue);
            }

            return sb.toString();
    }
}