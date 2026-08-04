package com.logistics.hub.presentation.dto.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class HubCreateRequestDto {

    @NotBlank(message="허브 이름을 입력해 주세요")
    @Size(max =50, message = " 50자 이하로 입력해 주세요")
    private String hubName;

    @NotBlank(message="허브 주소를 입력해 주세요")
    @Size(max =100, message = " 100자 이하로 입력해 주세요")
    private String hubAddress;

    @NotNull(message = "위도를 입력해 주세요")
    private BigDecimal latitude;

    @NotNull(message = "경도를 입력해 주세요")
    private BigDecimal longitude;

    //유저 정보가 없어서 임시로 추가
    private Long createdBy = 1L;

}
