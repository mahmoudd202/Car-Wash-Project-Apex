package com.apex.carwash.dto;

import com.apex.carwash.entity.WashingService;

import java.math.BigDecimal;

public record ServiceResponse(
        Long id,
        String name,
        int durationMinutes,
        BigDecimal price
) {
    public static ServiceResponse from(WashingService service) {
        return new ServiceResponse(
                service.getId(),
                service.getName(),
                service.getDurationMinutes(),
                service.getPrice()
        );
    }
}
