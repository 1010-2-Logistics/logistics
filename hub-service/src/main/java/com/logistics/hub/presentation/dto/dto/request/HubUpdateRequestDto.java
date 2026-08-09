package com.logistics.hub.presentation.dto.dto.request;

import com.logistics.hub.application.dto.command.HubUpdateCommand;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record HubUpdateRequestDto(
        @NotBlank(message="허브 이름을 입력해 주세요")
        @Size(max =50, message = " 50자 이하로 입력해 주세요")
        String hubName,

        @NotBlank(message="허브 주소를 입력해 주세요")
        @Size(max =100, message = " 100자 이하로 입력해 주세요")
        String hubAddress,

        @NotNull(message = "위도를 입력해 주세요")
        @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다")
        @DecimalMax(value = "90.0", message = "위도는 90 이하여야 합니다")
        @Digits(integer = 3, fraction = 7, message = "위도는 정수 최대 3자리, 소수점 최대 7자리까지 입력 가능합니다")
        BigDecimal latitude,

        @NotNull(message = "경도를 입력해 주세요")
        @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다")
        @DecimalMax(value = "180.0", message = "경도는 180 이하여야 합니다")
        @Digits(integer = 3, fraction = 7, message = "경도는 정수 최대 3자리, 소수점 최대 7자리까지 입력 가능합니다")
        BigDecimal longitude

) {
    public HubUpdateCommand toCommand(){return  new HubUpdateCommand(hubName,hubAddress,latitude,longitude); }
}
