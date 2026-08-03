package com.logistics.template.presentation.controller.dto.request;

import com.logistics.template.application.dto.command.CreateInventoryCommand;
import jakarta.validation.constraints.NotBlank;

public record SampleCreateRequest(@NotBlank String name) {

    public CreateInventoryCommand toCommand() {
        return new CreateInventoryCommand(name);
    }
}
