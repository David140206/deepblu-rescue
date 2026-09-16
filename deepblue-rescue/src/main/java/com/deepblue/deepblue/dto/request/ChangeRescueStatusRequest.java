package com.deepblue.deepblue.dto.request;

import com.deepblue.deepblue.domain.RescueStatus;

public record ChangeRescueStatusRequest(

        RescueStatus status

) {
}
