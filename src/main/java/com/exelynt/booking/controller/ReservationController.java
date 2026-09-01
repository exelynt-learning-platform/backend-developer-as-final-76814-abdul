package com.exelynt.booking.controller;

import com.exelynt.booking.dto.request.ReservationRequest;
import com.exelynt.booking.dto.request.ReservationStatusUpdateRequest;
import com.exelynt.booking.dto.response.ReservationResponse;
import com.exelynt.booking.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /** USER creates a reservation for themself; ADMIN can also create one (still tied to their own account). */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody ReservationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.create(request));
    }

    /** ADMIN gets every reservation; USER gets only their own (filtered in the service layer). */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<List<ReservationResponse>> getAll() {
        return ResponseEntity.ok(reservationService.findAllForCurrentUser());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ReservationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.findById(id));
    }

    /** ADMIN can set any status; USER may only cancel their own (enforced in service layer). */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ReservationResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ReservationStatusUpdateRequest request) {
        return ResponseEntity.ok(reservationService.updateStatus(id, request));
    }

    /** ADMIN only — hard delete. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
