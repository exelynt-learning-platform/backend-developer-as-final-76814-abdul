package com.exelynt.booking.service;

import com.exelynt.booking.dto.request.ResourceRequest;
import com.exelynt.booking.dto.response.ResourceResponse;
import com.exelynt.booking.exception.ResourceNotFoundException;
import com.exelynt.booking.model.ResourceEntity;
import com.exelynt.booking.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;

    /** Available to USER and ADMIN — read-only listing. */
    public List<ResourceResponse> findAll() {
        return resourceRepository.findAll().stream()
                .map(ResourceResponse::from)
                .toList();
    }

    public ResourceResponse findById(Long id) {
        return ResourceResponse.from(getOrThrow(id));
    }

    /** ADMIN only — enforced at the HTTP layer in SecurityConfig, and again here for defense in depth. */
    public ResourceResponse create(ResourceRequest request) {
        ResourceEntity resource = ResourceEntity.builder()
                .name(request.getName())
                .type(request.getType())
                .description(request.getDescription())
                .available(request.isAvailable())
                .build();
        return ResourceResponse.from(resourceRepository.save(resource));
    }

    public ResourceResponse update(Long id, ResourceRequest request) {
        ResourceEntity resource = getOrThrow(id);
        resource.setName(request.getName());
        resource.setType(request.getType());
        resource.setDescription(request.getDescription());
        resource.setAvailable(request.isAvailable());
        return ResourceResponse.from(resourceRepository.save(resource));
    }

    public void delete(Long id) {
        ResourceEntity resource = getOrThrow(id);
        resourceRepository.delete(resource);
    }

    private ResourceEntity getOrThrow(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
    }
}
