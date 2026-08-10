package com.logistics.hubRoute.presentation.controller;

import com.logistics.hubRoute.application.dto.command.HubRouteCreateCommand;
import com.logistics.hubRoute.application.facade.HubRouteFacade;
import com.logistics.hubRoute.application.service.HubRouteCommandService;
import com.logistics.hubRoute.global.response.ApiResponse;
import com.logistics.hubRoute.presentation.dto.dto.request.HubRouteCreateRequestDto;
import com.logistics.hubRoute.presentation.dto.dto.request.HubRouteUpdateRequestDto;
import com.logistics.hubRoute.presentation.dto.dto.response.HubRouteCreateResponseDto;
import com.logistics.hubRoute.presentation.dto.dto.response.HubRouteFindResponseDto;
import com.logistics.hubRoute.presentation.dto.dto.response.HubRouteResponseDto;
import com.logistics.hubRoute.presentation.dto.dto.response.HubRouteUpdateResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name= "HubRoute")
@RestController
@RequestMapping("/api/v1/hubRoute")
@RequiredArgsConstructor
public class HubRouteCommandController {

    //TODO 유저 기능이 생기면 권한 설정 및 Entity에 임시로 넣은 createdBy 삭제 할 것

    private final HubRouteCommandService hubRouteCommandService;

    @Operation(
            summary = "허브 경로 생성",
            description = """
                     접근 권한:
                    - MASTER: 허브 경로 생성 가능
                    """
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<HubRouteCreateResponseDto> createHubRoute(
            @Valid @RequestBody HubRouteCreateRequestDto hubRouteCreateRequestDto) {

        HubRouteCreateCommand hubRouteCreateCommand = HubRouteCreateCommand.from(hubRouteCreateRequestDto);
        HubRouteCreateResponseDto hubRouteCreateResponseDto = hubRouteCommandService.createHubRoute(hubRouteCreateCommand);

        return ApiResponse.success(201, "허브 경로 생성 성공", hubRouteCreateResponseDto);
    }

    @Operation(
            summary = "허브 경로 수정",
            description = """
                     접근 권한:
                    - MASTER: 허브 경로 수정 가능
                    """
    )
    @PutMapping("/{hubRouteId}")
    public ApiResponse<HubRouteUpdateResponseDto> updateHubRoute(@PathVariable UUID hubRouteId,
                                                                 @Valid @RequestBody HubRouteUpdateRequestDto hubRouteUpdateRequestDto) {

        HubRouteUpdateResponseDto hubRouteUpdateResponseDto = hubRouteCommandService.updateHubRoute(hubRouteId, hubRouteUpdateRequestDto);

        return ApiResponse.success(200, "허브 경로 수정 성공", hubRouteUpdateResponseDto);
    }

    @Operation(
            summary = "허브 경로 삭제",
            description = """
                     접근 권한:
                    - MASTER: 허브 경로 삭제 가능
                    """
    )
    @DeleteMapping("/{hubRouteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID hubRouteId) {
        // TODO: 인증 붙으면 실제 로그인 사용자로 교체
        hubRouteCommandService.deleteHubRoute(hubRouteId,1L);
    }
}
