package com.deepblue.deepblue.dto.response;

import com.deepblue.deepblue.domain.TreatmentType;

import java.time.LocalDateTime;

public record TreatmentResponse(

        Long id,

        String animalCode,

        String specialistCode,

        LocalDateTime performedAt,

        TreatmentType type,

        String description
) {
}
