package com.deepblue.deepblue.service.impl;

import com.deepblue.deepblue.domain.RescueStatus;
import com.deepblue.deepblue.dto.request.ChangeRescueStatusRequest;
import com.deepblue.deepblue.dto.response.RescueCaseResponse;
import com.deepblue.deepblue.exception.ResourceNotFoundException;
import com.deepblue.deepblue.mapper.RescueCaseMapper;
import com.deepblue.deepblue.repository.RescueCaseRepository;
import com.deepblue.deepblue.service.RescueCaseService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RescueCaseServiceImpl
        implements RescueCaseService {

    private final RescueCaseRepository repository;

    private final RescueCaseMapper mapper;

    public RescueCaseServiceImpl(
            RescueCaseRepository repository,
            RescueCaseMapper mapper) {

        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public RescueCaseResponse findByCode(String caseCode) {
        return repository
                .findByCaseCode(caseCode)
                .map(mapper::toResponse)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Rescue case not found: "
                                        + caseCode
                        )
                );
    }

    @Override
    public List<RescueCaseResponse> findByStatus(RescueStatus status) {
        return repository
                .findByStatusOrderByRescueDateAsc(status)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public RescueCaseResponse changeStatus(String caseCode, ChangeRescueStatusRequest request) {
        // TODO 1
        // Buscar el RescueCase.

        // TODO 2
        // Si no existe:
        // ResourceNotFoundException.

        // TODO 3
        // Obtener currentStatus.

        // TODO 4
        // Validar transición.

        // TODO 5
        // Si no es válida:
        // BusinessRuleException.

        // TODO 6
        // Cambiar status.

        // TODO 7
        // Guardar.

        // TODO 8
        // Transformar a Response.
        return null;
    }

    private boolean isValidTransition(
            RescueStatus current,
            RescueStatus next) {

        return switch (current) {

            case ADMITTED ->
                    next == RescueStatus.UNDER_EVALUATION;

            case UNDER_EVALUATION ->
                    next == RescueStatus.IN_REHABILITATION;

            case IN_REHABILITATION ->
                    next == RescueStatus.READY_FOR_RELEASE;

            case READY_FOR_RELEASE ->
                    next == RescueStatus.RELEASED;

            default -> false;
        };
    }

}
