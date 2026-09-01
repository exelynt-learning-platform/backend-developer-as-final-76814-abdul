package com.exelynt.booking.dto.request;

import com.exelynt.booking.model.ReservationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReservationStatusUpdateRequest {

    @NotNull
    private ReservationStatus status;
}
