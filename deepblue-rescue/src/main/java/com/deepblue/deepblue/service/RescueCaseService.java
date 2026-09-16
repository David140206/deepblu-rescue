package com.deepblue.deepblue.service;

import com.deepblue.deepblue.domain.RescueStatus;
import com.deepblue.deepblue.dto.request.ChangeRescueStatusRequest;
import com.deepblue.deepblue.dto.response.RescueCaseResponse;

import java.util.List;

public interface RescueCaseService {

    RescueCaseResponse findByCode(
            String caseCode
    );

    List<RescueCaseResponse> findByStatus(
            RescueStatus status
    );

    RescueCaseResponse changeStatus(
            String caseCode,
            ChangeRescueStatusRequest request
    );
}