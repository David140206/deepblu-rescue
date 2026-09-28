package com.deepblue.deepblue.service;

import com.deepblue.deepblue.domain.Animal;
import com.deepblue.deepblue.domain.AnimalSex;
import com.deepblue.deepblue.domain.RescueCase;
import com.deepblue.deepblue.domain.RescueStatus;
import com.deepblue.deepblue.dto.response.AnimalResponse;
import com.deepblue.deepblue.exception.ResourceNotFoundException;
import com.deepblue.deepblue.mapper.AnimalMapper;
import com.deepblue.deepblue.repository.AnimalRepository;
import com.deepblue.deepblue.service.impl.AnimalServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnimalServiceImplTest {

    @Mock
    private AnimalRepository repository;
    @Mock
    private AnimalMapper mapper;

    @InjectMocks
    AnimalServiceImpl service;

    @Test
    void shouldFindAnimalByCode (){

        Animal animal = mock(Animal.class);

        AnimalResponse response =
                new AnimalResponse(
                        1L,
                        "AN-001",
                        "Green Sea Turtle",
                        "Chelonia mydas",
                        AnimalSex.FEMALE,
                        "RES-001",
                        RescueStatus.IN_REHABILITATION );

        when(repository.findByAnimalCode("AN-001")).thenReturn(Optional.of(animal));
        when(mapper.toResponse(animal)) .thenReturn(response);

        AnimalResponse result = service.findByCode("AN-001");
        assertThat(result).isEqualTo(response);

        verify(repository).findByAnimalCode("AN-001");
        verify(mapper).toResponse(animal);
    }

    @Test
    void shouldThrowWhenAnimalNotFoundByCode(){

        when(repository.findByAnimalCode("AN-999")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findByCode("AN-999") )
                .isInstanceOf(ResourceNotFoundException.class);

        verify(repository).findByAnimalCode("AN-999");
        verify(mapper, never()).toResponse(any());
    }

    @Test
    void shouldReturnTrueWhenAnimalCanReceiveTreatment(){
        // Arrange
        Animal animal = mock(Animal.class);
        RescueCase rescueCase = mock(RescueCase.class);

        when(repository.findByAnimalCode("AN-001")).thenReturn(Optional.of(animal));
        when(animal.getRescueCase()).thenReturn(rescueCase);
        when(rescueCase.getStatus()).thenReturn(RescueStatus.IN_REHABILITATION);

        boolean result = service.canReceiveTreatment("AN-001");
        assertThat(result).isTrue();

        verify(repository).findByAnimalCode("AN-001");
        verify(animal).getRescueCase();
        verify(rescueCase).getStatus();
    }

    @Test
    void shouldReturnFalseWhenAnimalCannotReceiveTreatment(){

        Animal animal = mock(Animal.class);
        RescueCase rescueCase = mock(RescueCase.class);

        when(repository.findByAnimalCode("AN-001")) .thenReturn(Optional.of(animal));
        when(animal.getRescueCase()) .thenReturn(rescueCase);
        when(rescueCase.getStatus()) .thenReturn(RescueStatus.RELEASED);

        boolean result = service.canReceiveTreatment("AN-001");
        assertThat(result).isFalse();

        verify(repository).findByAnimalCode("AN-001");
        verify(animal).getRescueCase();
        verify(rescueCase).getStatus();
    }

}