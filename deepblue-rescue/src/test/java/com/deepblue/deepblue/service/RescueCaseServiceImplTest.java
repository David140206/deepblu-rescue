package com.deepblue.deepblue.service;

import com.deepblue.deepblue.domain.RescueCase;
import com.deepblue.deepblue.domain.RescueStatus;
import com.deepblue.deepblue.dto.request.ChangeRescueStatusRequest;
import com.deepblue.deepblue.dto.response.RescueCaseResponse;
import com.deepblue.deepblue.exception.BusinessRuleException;
import com.deepblue.deepblue.exception.ResourceNotFoundException;
import com.deepblue.deepblue.mapper.RescueCaseMapper;
import com.deepblue.deepblue.repository.RescueCaseRepository;
import com.deepblue.deepblue.service.impl.RescueCaseServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RescueCaseServiceImplTest {

    @Mock
    private RescueCaseRepository repository;

    @Mock
    private RescueCaseMapper mapper;

    @InjectMocks
    private RescueCaseServiceImpl service;

    @Test
    void shouldFindRescueCaseByCode() {

        RescueCase rescueCase = mock(RescueCase.class);

        RescueCaseResponse response =
                new RescueCaseResponse(
                    1L,
                    "RES-001",
                    LocalDate.of(2026, 9, 23),
                    "Santa Marta",
                    RescueStatus.ADMITTED,
                    "RC-001",
                    "AN-001"
                );

        when(repository.findByCaseCode("RES-001")).thenReturn(Optional.of(rescueCase));
        when(mapper.toResponse(rescueCase)).thenReturn(response);
        RescueCaseResponse result = service.findByCode("RES-001");
        assertThat(result).isEqualTo(response);
        verify(repository).findByCaseCode("RES-001");
        verify(mapper).toResponse(rescueCase);
    }

    @Test
    void shouldThrowWhenRescueCaseNotFoundByCode(){

        when(repository.findByCaseCode("RES-999")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findByCode("RES-999"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(repository).findByCaseCode("RES-999");
        verify(mapper, never()).toResponse(any());
    }

    @Test
    void shouldChangeStatusWhenTransitionIsValid(){

        RescueCase rescueCase = mock(RescueCase.class);

        RescueCaseResponse response =
                new RescueCaseResponse(
                        1L,
                        "RES-001",
                        LocalDate.of(2026, 9, 23),
                        "Santa Marta",
                        RescueStatus.UNDER_EVALUATION,
                        "RC-001",
                        "AN-001"
                );

        ChangeRescueStatusRequest request =
                new ChangeRescueStatusRequest(RescueStatus.UNDER_EVALUATION);

        when(repository.findByCaseCode("RES-001")).thenReturn(Optional.of(rescueCase));
        when(rescueCase.getStatus()).thenReturn(RescueStatus.ADMITTED);
        when(repository.saveAndFlush(rescueCase)).thenReturn(rescueCase);
        when(mapper.toResponse(rescueCase)).thenReturn(response);

        RescueCaseResponse result = service.changeStatus("RES-001", request);
        assertThat(result).isEqualTo(response);
        verify(repository).findByCaseCode("RES-001");
        verify(rescueCase).setStatus(RescueStatus.UNDER_EVALUATION);
        verify(repository).saveAndFlush(rescueCase);
        verify(mapper).toResponse(rescueCase);
    }

    @Test
    void shouldThrowExceptionWhenTransitionIsInvalid() {

        RescueCase rescueCase = mock(RescueCase.class);

        ChangeRescueStatusRequest request =
                new ChangeRescueStatusRequest(RescueStatus.READY_FOR_RELEASE);

        when(repository.findByCaseCode("RES-001")).thenReturn(Optional.of(rescueCase));
        when(rescueCase.getStatus()).thenReturn(RescueStatus.ADMITTED);
        assertThatThrownBy(() -> service.changeStatus("RES-001", request)).
                isInstanceOf(BusinessRuleException.class);

        verify(repository).findByCaseCode("RES-001");
        verify(rescueCase, never()).setStatus(RescueStatus.READY_FOR_RELEASE);
        verify(repository, never()).save(any());
    }
}
