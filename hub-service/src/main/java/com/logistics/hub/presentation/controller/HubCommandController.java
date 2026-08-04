package com.logistics.hub.presentation.controller;

import com.logistics.hub.application.dto.command.HubCreateCommand;
import com.logistics.hub.application.dto.command.HubUpdateCommand;
import com.logistics.hub.application.facade.HubFacade;
import com.logistics.hub.application.service.HubCommandService;
import com.logistics.hub.global.response.ApiResponse;
import com.logistics.hub.presentation.dto.dto.request.HubCreateRequestDto;
import com.logistics.hub.presentation.dto.dto.request.HubUpdateRequestDto;
import com.logistics.hub.presentation.dto.dto.response.HubCreateResponseDto;
import com.logistics.hub.presentation.dto.dto.response.HubResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hubs")
@RequiredArgsConstructor
public class HubCommandController {

    //TODO 유저 기능이 생기면 권한 설정 및 Entity에 임시로 넣은 createdBy 삭제 할 것

    private final HubCommandService hubCommandService;

    private final HubFacade hubFacade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<HubCreateResponseDto> createHub(
            @Valid @RequestBody HubCreateRequestDto hubCreateRequestDto) {

        HubCreateCommand hubCreateCommand = HubCreateCommand.from(hubCreateRequestDto);
        HubCreateResponseDto hubCreateResponseDto = hubCommandService.createHub(hubCreateCommand);

        return ApiResponse.success(201, "허브 생성 성공", hubCreateResponseDto);
    }

    @PutMapping("/{hubId}")
    public ApiResponse<Void> updateHub(@PathVariable UUID hubId,
                                    @Valid @RequestBody HubUpdateRequestDto request) {

        HubResponseDto hubResponseDto = hubCommandService.updateHub(hubId, request.toCommand());

        return ApiResponse.success(200, "허브 수정 성공", null);
    }

    @DeleteMapping("/{hubId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID hubId) {
        // TODO: 인증 붙으면 실제 로그인 사용자로 교체
        hubFacade.deleteHub(hubId, 1L);
    }
}
