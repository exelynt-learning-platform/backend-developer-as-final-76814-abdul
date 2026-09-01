package com.exelynt.booking.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Note: there is intentionally NO userId field here.
 * The reserving user's identity always comes from the authenticated
 * JWT principal (see ReservationService), never from client input,
 * so a user cannot create a reservation on someone else's behalf.
 */
@Data
public class ReservationRequest {

    @NotNull
    private Long resourceId;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    @Future(message = "endTime must be in the future")
    private LocalDateTime endTime;
}
