package com.deepblue.deepblue.service.impl;

import com.deepblue.deepblue.domain.*;
import com.deepblue.deepblue.dto.request.CreateTreatmentRequest;
import com.deepblue.deepblue.dto.response.TreatmentResponse;
import com.deepblue.deepblue.exception.BusinessRuleException;
import com.deepblue.deepblue.exception.ResourceNotFoundException;
import com.deepblue.deepblue.mapper.TreatmentMapper;
import com.deepblue.deepblue.repository.AnimalRepository;
import com.deepblue.deepblue.repository.SpecialistRepository;
import com.deepblue.deepblue.repository.TreatmentRepository;
import com.deepblue.deepblue.service.TreatmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TreatmentServiceImpl
        implements TreatmentService {
    private final AnimalRepository animalRepository;

    private final SpecialistRepository specialistRepository;

    private final TreatmentRepository treatmentRepository;

    private final TreatmentMapper mapper;

    public TreatmentServiceImpl(
            AnimalRepository animalRepository,
            SpecialistRepository specialistRepository,
            TreatmentRepository treatmentRepository,
            TreatmentMapper mapper) {

        this.animalRepository = animalRepository;
        this.specialistRepository = specialistRepository;
        this.treatmentRepository = treatmentRepository;
        this.mapper = mapper;
    }

    @Override
    public TreatmentResponse register(CreateTreatmentRequest request) {

        Animal animal = animalRepository
                .findByAnimalCode(request.animalCode())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Animal not found: "
                                        + request.animalCode())
                );

        Specialist specialist = specialistRepository
                .findByProfessionalCode(request.specialistCode())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Specialist not found: "
                                        + request.specialistCode())
                );

        if(!specialist.isActive()){
            throw new BusinessRuleException(
                    "Cannot register treatment because "
                            + "the specialist is inactive."
            );
        }

        RescueCase rescueCase = animal.getRescueCase();

        if (rescueCase.getStatus() == RescueStatus.RELEASED
                || rescueCase.getStatus() == RescueStatus.CLOSED) {

            throw new BusinessRuleException(
                    "Cannot register treatment because "
                            + "the rescue case is already "
                            + rescueCase.getStatus()
            );
        }

        if (request.performedAt().toLocalDate()
                .isBefore(rescueCase.getRescueDate())) {

            throw new BusinessRuleException(
                    "Treatment date cannot be before "
                            + "the rescue date.");
        }

        Treatment treatment = new Treatment(
                animal,
                specialist,
                request.performedAt(),
                request.type(),
                request.description()
        );

        Treatment savedTreatment = treatmentRepository.saveAndFlush(treatment);
        return mapper.toResponse(savedTreatment);
    }

    @Override
    public List<TreatmentResponse> findByAnimalCode(String animalCode) {
        return treatmentRepository
                .findByAnimalAnimalCodeOrderByPerformedAtAsc(animalCode)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
