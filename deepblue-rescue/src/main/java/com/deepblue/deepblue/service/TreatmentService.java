package com.deepblue.deepblue.service;

import com.deepblue.deepblue.dto.request.CreateTreatmentRequest;
import com.deepblue.deepblue.dto.response.TreatmentResponse;

import java.util.List;

public interface TreatmentService {
    TreatmentResponse register(
            CreateTreatmentRequest request
    );

    List<TreatmentResponse> findByAnimalCode(
            String animalCode
    );
}
