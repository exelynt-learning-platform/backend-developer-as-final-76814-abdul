package com.exelynt.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResourceRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String type;

    private String description;

    private boolean available = true;
}
