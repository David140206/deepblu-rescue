package com.deepblue.deepblue.mapper;

import com.deepblue.deepblue.domain.Treatment;
import com.deepblue.deepblue.dto.response.TreatmentResponse;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TreatmentMapper {

    @Mapping(
            target = "animalCode",
            source = "animal.animalCode"
    )

    @Mapping(
            target = "specialistCode",
            source = "specialist.professionalCode"
    )
    TreatmentResponse toResponse(
            Treatment treatment
    )
    ;
}
