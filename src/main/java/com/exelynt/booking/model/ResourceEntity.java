package com.exelynt.booking.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Named "ResourceEntity" (not "Resource") to avoid clashing with
 * org.springframework.core.io.Resource which is commonly imported in Spring apps.
 */
@Entity
@Table(name = "resource")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String type; // e.g. ROOM, VEHICLE, EQUIPMENT

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean available;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}
