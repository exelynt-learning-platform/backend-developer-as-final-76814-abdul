package com.exelynt.booking.service;

import com.exelynt.booking.dto.request.ReservationRequest;
import com.exelynt.booking.dto.request.ReservationStatusUpdateRequest;
import com.exelynt.booking.dto.response.ReservationResponse;
import com.exelynt.booking.exception.AccessDeniedOwnershipException;
import com.exelynt.booking.exception.ResourceNotFoundException;
import com.exelynt.booking.model.*;
import com.exelynt.booking.repository.ReservationRepository;
import com.exelynt.booking.repository.ResourceRepository;
import com.exelynt.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    /**
     * Creates a reservation for the CURRENTLY AUTHENTICATED user.
     * The user id is deliberately never taken from the request body — only from the
     * JWT-backed SecurityContext — so a USER cannot create a reservation "as" someone else.
     */
    public ReservationResponse create(ReservationRequest request) {
        User currentUser = getCurrentUser();

        ResourceEntity resource = resourceRepository.findById(request.getResourceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Resource not found with id: " + request.getResourceId()));

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }

        Reservation reservation = Reservation.builder()
                .resource(resource)
                .user(currentUser)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(ReservationStatus.PENDING)
                .build();

        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    /** ADMIN sees every reservation; USER sees only their own. */
    public List<ReservationResponse> findAllForCurrentUser() {
        User currentUser = getCurrentUser();

        List<Reservation> reservations = isAdmin()
                ? reservationRepository.findAll()
                : reservationRepository.findByUserId(currentUser.getId());

        return reservations.stream().map(ReservationResponse::from).toList();
    }

    public ReservationResponse findById(Long id) {
        Reservation reservation = getOrThrow(id);
        assertOwnerOrAdmin(reservation);
        return ReservationResponse.from(reservation);
    }

    /** ADMIN can set any status transition (e.g. confirm/cancel). USER may only cancel their own. */
    public ReservationResponse updateStatus(Long id, ReservationStatusUpdateRequest request) {
        Reservation reservation = getOrThrow(id);

        if (isAdmin()) {
            reservation.setStatus(request.getStatus());
        } else {
            assertOwnerOrAdmin(reservation);
            if (request.getStatus() != ReservationStatus.CANCELLED) {
                throw new AccessDeniedOwnershipException(
                        "Users may only cancel their own reservations, not change status to " + request.getStatus());
            }
            reservation.setStatus(ReservationStatus.CANCELLED);
        }

        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    /** ADMIN can delete any reservation. USER can cancel (soft-delete) their own via updateStatus instead. */
    public void delete(Long id) {
        Reservation reservation = getOrThrow(id);
        if (!isAdmin()) {
            throw new AccessDeniedOwnershipException("Only administrators can permanently delete reservations");
        }
        reservationRepository.delete(reservation);
    }

    // ---- helpers ----

    private Reservation getOrThrow(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + id));
    }

    private void assertOwnerOrAdmin(Reservation reservation) {
        if (isAdmin()) {
            return;
        }
        User currentUser = getCurrentUser();
        if (!reservation.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedOwnershipException("You do not have access to this reservation");
        }
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    /** Resolves the authenticated principal (set by JwtAuthFilter) back to our User entity. */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found: " + username));
    }
}
