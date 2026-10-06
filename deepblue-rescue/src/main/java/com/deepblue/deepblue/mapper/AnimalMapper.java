package com.deepblue.deepblue.mapper;

import com.deepblue.deepblue.domain.Animal;
import com.deepblue.deepblue.dto.response.AnimalResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnimalMapper {

    @Mapping(
            target = "caseCode",
            source = "rescueCase.caseCode"
    )

    @Mapping(
            target = "rescueStatus",
            source = "rescueCase.status"
    )
    AnimalResponse toResponse(
            Animal animal
    );
}
