package com.bookfair.auth_service.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class VendorSummaryResponse {
    UUID id;
    String companyName;
    String firstName;
    String lastName;
    String email;
}
