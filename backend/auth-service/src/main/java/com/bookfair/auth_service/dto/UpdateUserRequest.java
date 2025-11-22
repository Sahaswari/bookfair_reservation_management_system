package com.bookfair.auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @NotBlank(message = "First name is mandatory")
    @Size(max = 100, message = "First name must be less than 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name must be less than 100 characters")
    private String lastName;

    @Size(max = 255, message = "Company name must be less than 255 characters")
    private String companyName;

    @NotBlank(message = "Mobile number is mandatory")
    @Pattern(regexp = "^[0-9+\\-() ]{7,20}$", message = "Mobile number must be between 7 to 20 digits")
    private String mobileNo;
}
