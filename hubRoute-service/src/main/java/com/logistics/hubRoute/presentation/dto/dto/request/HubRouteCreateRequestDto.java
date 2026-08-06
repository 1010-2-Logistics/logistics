package com.logistics.hubRoute.presentation.dto.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    private Integer duration;

    @NotNull(message = "거리(Km)를 입력해 주세요")
    private BigDecimal distance;

    //유저 정보가 없어서 임시로 추가
    private Long createdBy = 1L;

}
