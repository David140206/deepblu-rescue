package com.deepblue.deepblue.service;

import com.deepblue.deepblue.dto.response.AnimalResponse;

import java.util.List;

public interface AnimalService {

    AnimalResponse findByCode(String animalCode);

    List<AnimalResponse> findAnimalsInRehabilitation();

    boolean canReceiveTreatment(String animalCode);
}
