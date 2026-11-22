package com.example.apigateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.Enumeration;
import java.util.Map;

@RestController
@Slf4j
public class GatewayProxyController {

    @Value("${services.auth:http://localhost:8081}")
    private String authServiceUrl;

    @Value("${services.catalog:http://localhost:8082}")
    private String catalogServiceUrl;

    @Value("${services.inventory:http://localhost:8083}")
    private String inventoryServiceUrl;

    @Value("${services.order:http://localhost:8084}")
    private String orderServiceUrl;

    @Value("${services.payment:http://localhost:8085}")
    private String paymentServiceUrl;

    @Value("${services.notification:http://localhost:8086}")
    private String notificationServiceUrl;

    private final RestClient restClient = RestClient.builder()
            .defaultStatusHandler(new DefaultResponseErrorHandler() {
                @Override
                public boolean hasError(ClientHttpResponse response) {
                    return false;
                }
            })
            .build();

    @GetMapping("/api/v1/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "api-gateway",
                "timestamp", System.currentTimeMillis()
        ));
    }

    @RequestMapping(value = {
            "/api/v1/auth/**",
            "/api/v1/users/**",
            "/api/v1/catalog/**",
            "/api/v1/inventory/**",
            "/api/v1/orders/**",
            "/api/v1/payments/**",
            "/api/v1/notifications/**"
    }, method = {
            RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
            RequestMethod.DELETE, RequestMethod.PATCH, RequestMethod.HEAD, RequestMethod.OPTIONS
    })
    public ResponseEntity<byte[]> proxyRequest(
            @RequestBody(required = false) byte[] body,
            HttpMethod method,
            HttpServletRequest request) {

        String path = request.getRequestURI();
        String queryString = request.getQueryString();
        String targetBaseUrl = resolveTargetService(path);

        String fullTargetUrl = targetBaseUrl + path + (queryString != null ? "?" + queryString : "");

        HttpHeaders forwardHeaders = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (!headerName.equalsIgnoreCase(HttpHeaders.HOST) &&
                    !headerName.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)) {
                Enumeration<String> headerValues = request.getHeaders(headerName);
                while (headerValues.hasMoreElements()) {
                    forwardHeaders.add(headerName, headerValues.nextElement());
                }
            }
        }

        try {
            RestClient.RequestBodySpec requestSpec = restClient.method(method)
                    .uri(URI.create(fullTargetUrl))
                    .headers(httpHeaders -> httpHeaders.addAll(forwardHeaders));

            if (body != null && body.length > 0) {
                requestSpec.body(body);
            }

            return requestSpec.exchange((clientRequest, clientResponse) -> {
                byte[] responseBody = StreamUtils.copyToByteArray(clientResponse.getBody());
                HttpHeaders responseHeaders = new HttpHeaders();
                clientResponse.getHeaders().forEach((name, values) -> {
                    if (!name.equalsIgnoreCase(HttpHeaders.TRANSFER_ENCODING)) {
                        responseHeaders.addAll(name, values);
                    }
                });

                return ResponseEntity.status(clientResponse.getStatusCode())
                        .headers(responseHeaders)
                        .body(responseBody);
            });
        } catch (Exception e) {
            log.error("Lỗi khi chuyển tiếp request tới {}: {}", fullTargetUrl, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(("{\"code\":502,\"message\":\"Dịch vụ đích không phản hồi: " + e.getMessage() + "\"}").getBytes());
        }
    }

    private String resolveTargetService(String path) {
        if (path.startsWith("/api/v1/auth") || path.startsWith("/api/v1/users")) {
            return authServiceUrl;
        } else if (path.startsWith("/api/v1/catalog")) {
            return catalogServiceUrl;
        } else if (path.startsWith("/api/v1/inventory")) {
            return inventoryServiceUrl;
        } else if (path.startsWith("/api/v1/orders")) {
            return orderServiceUrl;
        } else if (path.startsWith("/api/v1/payments")) {
            return paymentServiceUrl;
        } else if (path.startsWith("/api/v1/notifications")) {
            return notificationServiceUrl;
        }
        return "http://localhost:8080";
    }
}
