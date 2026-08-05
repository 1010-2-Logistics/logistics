package com.logistics.template.presentation.controller.dto.request;

import com.logistics.template.application.dto.command.CreateSampleCommand;
import jakarta.validation.constraints.NotBlank;

public record SampleCreateRequest(@NotBlank String name) {

    public CreateSampleCommand toCommand() {
        return new CreateSampleCommand(name);
    }
}
