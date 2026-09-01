package com.exelynt.booking.dto.response;

import com.exelynt.booking.model.ResourceEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResourceResponse {
    private Long id;
    private String name;
    private String type;
    private String description;
    private boolean available;

    public static ResourceResponse from(ResourceEntity r) {
        return new ResourceResponse(r.getId(), r.getName(), r.getType(), r.getDescription(), r.isAvailable());
    }
}
