package com.logistics.hubRoute.presentation.controller;

import com.logistics.hubRoute.application.dto.command.HubRouteCreateCommand;
import com.logistics.hubRoute.application.facade.HubRouteFacade;
import com.logistics.hubRoute.application.service.HubRouteCommandService;
import com.logistics.hubRoute.global.response.ApiResponse;
import com.logistics.hubRoute.presentation.dto.dto.request.HubRouteCreateRequestDto;
import com.logistics.hubRoute.presentation.dto.dto.request.HubRouteUpdateRequestDto;
import com.logistics.hubRoute.presentation.dto.dto.response.HubRouteCreateResponseDto;
import com.logistics.hubRoute.presentation.dto.dto.response.HubRouteResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/hubRoute")
@RequiredArgsConstructor
public class HubRouteCommandController {

    //TODO 유저 기능이 생기면 권한 설정 및 Entity에 임시로 넣은 createdBy 삭제 할 것

    private final HubRouteCommandService hubRouteCommandService;

    private final HubRouteFacade hubRouteFacade;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<HubRouteCreateResponseDto> createHubRoute(
            @Valid @RequestBody HubRouteCreateRequestDto hubRouteCreateRequestDto) {

        HubRouteCreateCommand hubRouteCreateCommand = HubRouteCreateCommand.from(hubRouteCreateRequestDto);
        HubRouteCreateResponseDto hubRouteCreateResponseDto = hubRouteCommandService.createHubRoute(hubRouteCreateCommand);

        return ApiResponse.success(201, "허브 생성 성공", hubRouteCreateResponseDto);
    }

//    @PutMapping("/{hubRouteId}")
//    public ApiResponse<HubRouteResponseDto> updateHub(@PathVariable UUID hubId,
//                                                      @Valid @RequestBody HubRouteUpdateRequestDto request) {
//
//        HubRouteResponseDto hubRouteResponseDto = hubRouteCommandService.updateHub(hubId, request.toCommand());
//
//        return ApiResponse.success(200, "허브 수정 성공", hubRouteResponseDto);
//    }

//    @DeleteMapping("/{hubRouteId}")
//    @ResponseStatus(HttpStatus.NO_CONTENT)
//    public void delete(@PathVariable UUID hubId) {
//        // TODO: 인증 붙으면 실제 로그인 사용자로 교체
//        hubRouteFacade.deleteHub(hubId, 1L);
//    }
}
