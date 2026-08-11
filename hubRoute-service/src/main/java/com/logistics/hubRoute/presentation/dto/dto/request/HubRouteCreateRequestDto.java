package com.logistics.hubRoute.presentation.dto.dto.request;


import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class HubRouteCreateRequestDto {

    @NotNull(message="시작허브를 입력해 주세요")
    private UUID startHubId;

    @NotNull(message="도착 허브를 입력해 주세요")
    private UUID endHubId;

    @NotNull(message="시간(분단위)을 입력해 주세요")
    @Min(value = 1, message = "시간은 1분 이상이어야 합니다")
    private Integer duration;

    @NotNull(message = "거리(Km)를 입력해 주세요")
    @DecimalMin(value = "0.01", message = "거리는 0Km보다 커야 합니다.")
    private BigDecimal distance;

}
