package com.bookfair.genre.client;

import com.bookfair.genre.dto.UserSnapshot;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth-service", url = "http://auth-service:8081")
public interface AuthClient {

    @GetMapping("/auth/me")
    UserSnapshot getUserInfo(@RequestHeader("Authorization") String token);

}
