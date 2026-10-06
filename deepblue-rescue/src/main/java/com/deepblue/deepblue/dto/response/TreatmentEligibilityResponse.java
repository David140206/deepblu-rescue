package com.deepblue.deepblue.dto.response;

public record TreatmentEligibilityResponse(
        String animalCode,
        boolean eligible
) {
}

