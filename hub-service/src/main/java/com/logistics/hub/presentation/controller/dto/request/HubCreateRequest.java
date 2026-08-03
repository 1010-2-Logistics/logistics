package com.logistics.hub.presentation.controller.dto.request;

import com.logistics.hub.application.dto.command.CreateHubCommand;
import jakarta.validation.constraints.NotBlank;

public record HubCreateRequest(@NotBlank String name) {

    public CreateHubCommand toCommand() {
        return new CreateHubCommand(name);
    }
}
