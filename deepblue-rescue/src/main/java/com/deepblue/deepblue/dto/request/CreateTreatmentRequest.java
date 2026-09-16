package com.deepblue.deepblue.dto.request;

import com.deepblue.deepblue.domain.TreatmentType;

import java.time.LocalDateTime;

public record CreateTreatmentRequest(

        String animalCode,

        String specialistCode,

        LocalDateTime performedAt,

        TreatmentType type,

        String description

) {
}
