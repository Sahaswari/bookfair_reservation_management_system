package com.bookfair.stall_service.dto;

import com.bookfair.stall_service.entity.StallSize;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for creating a new Stall
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateStallRequest {
    
    @NotNull(message = "Event ID is required")
    private UUID eventId;
    
    @NotBlank(message = "Stall code is required")
    @Size(max = 10, message = "Stall code must not exceed 10 characters")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Stall code must contain only uppercase letters and numbers")
    private String stallCode;
    
    @NotNull(message = "Size category is required")
    private StallSize sizeCategory;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;
    
    private Float locationX;
    
    private Float locationY;
}
