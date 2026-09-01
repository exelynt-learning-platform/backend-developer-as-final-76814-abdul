package com.exelynt.booking.dto.response;

import com.exelynt.booking.model.Reservation;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ReservationResponse {
    private Long id;
    private Long resourceId;
    private String resourceName;
    private Long userId;
    private String username;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    public static ReservationResponse from(Reservation r) {
        return new ReservationResponse(
                r.getId(),
                r.getResource().getId(),
                r.getResource().getName(),
                r.getUser().getId(),
                r.getUser().getUsername(),
                r.getStartTime(),
                r.getEndTime(),
                r.getStatus().name()
        );
    }
}
