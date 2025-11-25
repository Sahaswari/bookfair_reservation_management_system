package com.bookfair.reservation_service.integration;

import com.bookfair.reservation_service.exception.InvalidOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/**
 * Thin HTTP client around Stall Service endpoints that mutate reservation state
 * (reserve/unreserve).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StallServiceClient {

    private final RestTemplate restTemplate;

    @Value("${app.services.stall-service.base-url}")
    private String stallServiceBaseUrl;

    /**
     * Calls Stall Service to mark a stall as reserved by a vendor.
     */
    public void reserveStall(UUID stallId, UUID vendorId) {
        URI uri = baseUriBuilder()
                .path("/api/stalls/{id}/reserve")
                .queryParam("vendorId", vendorId)
                .buildAndExpand(stallId)
                .toUri();
        executePost(uri, "reserve");
    }

    /**
     * Calls Stall Service to mark a stall as unreserved (best-effort rollback).
     */
    public void unreserveStall(UUID stallId) {
        URI uri = baseUriBuilder()
                .path("/api/stalls/{id}/unreserve")
                .buildAndExpand(stallId)
                .toUri();
        executePost(uri, "unreserve");
    }

    private UriComponentsBuilder baseUriBuilder() {
        String baseUrl = stallServiceBaseUrl;
        if (!(baseUrl.startsWith("http://") || baseUrl.startsWith("https://"))) {
            baseUrl = "http://" + baseUrl;
        }
        return UriComponentsBuilder.fromHttpUrl(baseUrl);
    }

    private void executePost(URI uri, String action) {
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(uri, null, Void.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new InvalidOperationException("Stall Service returned status %s for %s request"
                        .formatted(response.getStatusCode(), action));
            }
        } catch (RestClientResponseException ex) {
            log.error("Stall Service responded with an error while attempting to {} stall: {}", action, ex.getResponseBodyAsString());
            throw new InvalidOperationException("Failed to %s stall in Stall Service".formatted(action), ex);
        } catch (ResourceAccessException ex) {
            log.error("Could not reach Stall Service to {} stall", action, ex);
            throw new InvalidOperationException("Stall Service is unreachable; please try again later", ex);
        }
    }
}
