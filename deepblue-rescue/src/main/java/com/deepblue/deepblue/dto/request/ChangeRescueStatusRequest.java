package com.deepblue.deepblue.dto.request;

import com.deepblue.deepblue.domain.RescueStatus;

import jakarta.validation.constraints.NotNull;


public record ChangeRescueStatusRequest(

        @NotNull(
                message = "Status is required"
        )
        RescueStatus status

) {
}
