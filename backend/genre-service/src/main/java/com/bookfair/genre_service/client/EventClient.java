package com.bookfair.genre.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "event-service", url = "http://reservation-service:8084")
public interface EventClient {

    @GetMapping("/events/{eventId}/validate")
    boolean validateEvent(@PathVariable Long eventId);

}
