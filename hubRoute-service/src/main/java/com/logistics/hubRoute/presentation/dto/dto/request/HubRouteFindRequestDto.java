package com.logistics.hubRoute.presentation.dto.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;


public record HubRouteFindRequestDto(
        @NotNull(message = "출발 허브를 입력해주세요.")
        UUID startHubId,

        @NotNull(message = "도착 허브를 입력해주세요.")
        UUID endHubId

) {
}
