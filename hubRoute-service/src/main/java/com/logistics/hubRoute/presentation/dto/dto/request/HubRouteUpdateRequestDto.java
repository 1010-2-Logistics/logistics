package com.logistics.hubRoute.presentation.dto.dto.request;

import com.logistics.hubRoute.application.dto.command.HubRouteUpdateCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record HubRouteUpdateRequestDto(
        @NotNull(message="시작허브를 입력해 주세요")
        UUID startHubId,

        @NotNull(message="도착 허브를 입력해 주세요")
        UUID endHubId,

        @NotNull(message = "시간을 입력해주세요")
        Integer duration,

        @NotNull(message = "거리를 입력해 주세요")
        BigDecimal distance

) {
//    public HubRouteUpdateCommand toCommand(){return  new HubRouteUpdateCommand(hubName,hubAddress,latitude,longitude); }
}
