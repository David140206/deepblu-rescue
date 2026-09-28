package com.deepblue.deepblue.dto.response;

import com.deepblue.deepblue.domain.AnimalSex;
import com.deepblue.deepblue.domain.RescueStatus;

public record AnimalResponse(
        Long id,

        String animalCode,

        String commonName,

        String scientificName,

        AnimalSex sex,

        String caseCode,

        RescueStatus rescueStatus
) {
}
