package com.deepblue.deepblue.service;

import com.deepblue.deepblue.domain.*;
import com.deepblue.deepblue.dto.request.CreateTreatmentRequest;
import com.deepblue.deepblue.dto.response.TreatmentResponse;
import com.deepblue.deepblue.exception.BusinessRuleException;
import com.deepblue.deepblue.mapper.TreatmentMapper;
import com.deepblue.deepblue.repository.AnimalRepository;
import com.deepblue.deepblue.repository.SpecialistRepository;
import com.deepblue.deepblue.repository.TreatmentRepository;
import com.deepblue.deepblue.service.impl.TreatmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TreatmentServiceImplTest {

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private SpecialistRepository specialistRepository;

    @Mock
    private TreatmentRepository treatmentRepository;

    @Mock
    private TreatmentMapper mapper;

    @InjectMocks
    private TreatmentServiceImpl service;

    @Test
    void shouldRegisterTreatmentCorrectly(){

        Animal animal = mock(Animal.class);
        Specialist specialist = mock(Specialist.class);
        RescueCase rescueCase = mock(RescueCase.class);
        Treatment treatment = mock(Treatment.class);

        CreateTreatmentRequest request =
                new CreateTreatmentRequest(
                        "AN-001",
                        "SPEC-001",
                        LocalDateTime.of(2026, 8, 21, 10, 0),
                        TreatmentType.WOUND_CARE,
                        "Cleaning of wound"
                );

        TreatmentResponse response =
                new TreatmentResponse(
                        1L,
                        "AN-001",
                        "SPEC-001",
                        request.performedAt(),
                        TreatmentType.WOUND_CARE,
                        "Cleaning of wound"
                );

        when(animalRepository.findByAnimalCode("AN-001")).thenReturn(Optional.of(animal));
        when(specialistRepository.findByProfessionalCode("SPEC-001"))
                .thenReturn(Optional.of(specialist));
        when(specialist.isActive()).thenReturn(true);
        when(animal.getRescueCase()).thenReturn(rescueCase);
        when(rescueCase.getStatus()).thenReturn(RescueStatus.IN_REHABILITATION);
        when(rescueCase.getRescueDate()).
                thenReturn(LocalDate.of(2026, 8, 20));

        when(treatmentRepository.saveAndFlush(any(Treatment.class))).thenReturn(treatment);
        when(mapper.toResponse(treatment)).thenReturn(response);

        TreatmentResponse result = service.register(request);
        assertThat(result).isEqualTo(response);
        verify(animalRepository).findByAnimalCode("AN-001");
        verify(specialistRepository).findByProfessionalCode("SPEC-001");
        verify(treatmentRepository).saveAndFlush(any(Treatment.class));
        verify(mapper).toResponse(treatment);
    }

    @Test
    void shouldThrowExceptionWhenSpecialistIsInactive() {

        Animal animal = mock(Animal.class);
        Specialist specialist = mock(Specialist.class);

        CreateTreatmentRequest request =
                new CreateTreatmentRequest(
                        "AN-001",
                        "SPEC-001",
                        LocalDateTime.of(2026, 8, 21, 10, 0),
                        TreatmentType.WOUND_CARE,
                        "Cleaning of wound"
                );

        when(animalRepository.findByAnimalCode("AN-001")).thenReturn(Optional.of(animal));
        when(specialistRepository.findByProfessionalCode("SPEC-001"))
                .thenReturn(Optional.of(specialist));
        when(specialist.isActive()).thenReturn(false);

        assertThatThrownBy(() ->service.register(request))
                .isInstanceOf(BusinessRuleException.class);

        verify(animalRepository).findByAnimalCode("AN-001");
        verify(specialistRepository).findByProfessionalCode("SPEC-001");
        verify(treatmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenAnimalIsReleased() {

        Animal animal = mock(Animal.class);
        Specialist specialist = mock(Specialist.class);
        RescueCase rescueCase = mock(RescueCase.class);

        CreateTreatmentRequest request =
                new CreateTreatmentRequest(
                        "AN-001",
                        "SPEC-001",
                        LocalDateTime.of(2026, 8, 21, 10, 0),
                        TreatmentType.OBSERVATION,
                        "Observation after rehabilitation"
                );

        when(animalRepository.findByAnimalCode("AN-001")).thenReturn(Optional.of(animal));
        when(specialistRepository.findByProfessionalCode("SPEC-001"))
                .thenReturn(Optional.of(specialist));
        when(specialist.isActive()).thenReturn(true);
        when(animal.getRescueCase()).thenReturn(rescueCase);
        when(rescueCase.getStatus()).thenReturn(RescueStatus.RELEASED);

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(BusinessRuleException.class);

        verify(animalRepository).findByAnimalCode("AN-001");
        verify(specialistRepository).findByProfessionalCode("SPEC-001");

        verify(treatmentRepository, never()).save(any());
    }
}
