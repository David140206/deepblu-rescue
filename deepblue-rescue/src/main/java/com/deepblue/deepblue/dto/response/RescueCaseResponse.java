package com.deepblue.deepblue.dto.response;

import com.deepblue.deepblue.domain.RescueStatus;

import java.time.LocalDate;

public record RescueCaseResponse (
        Long id,

        String caseCode,

        LocalDate rescueDate,

        String rescueLocation,

        RescueStatus status,

        String centerCode,

        String animalCode
){
}
