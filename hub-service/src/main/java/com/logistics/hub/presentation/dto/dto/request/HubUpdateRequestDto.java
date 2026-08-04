package com.logistics.hub.presentation.dto.dto.request;

import com.logistics.hub.application.dto.command.HubUpdateCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record HubUpdateRequestDto(
        @NotBlank(message="허브 이름을 입력해 주세요")
        @Size(max =50, message = " 50자 이하로 입력해 주세요")
        String hubName,

        @NotBlank(message="허브 주소를 입력해 주세요")
        @Size(max =100, message = " 100자 이하로 입력해 주세요")
        String hubAddress,

        @NotNull(message = "위도를 입력해 주세요")
        BigDecimal latitude,

        @NotNull(message = "경도를 입력해 주세요")
        BigDecimal longitude

) {
    public HubUpdateCommand toCommand(){return  new HubUpdateCommand(hubName,hubAddress,latitude,longitude); }
}
