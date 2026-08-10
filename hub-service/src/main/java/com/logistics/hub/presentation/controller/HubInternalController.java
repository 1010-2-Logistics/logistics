package com.logistics.hub.presentation.controller;

import com.logistics.hub.application.service.HubQueryService;
import com.logistics.hub.presentation.dto.dto.response.HubResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Tag(name= "Hub Internal")
@RestController
@RequestMapping("/internal/v1/hubs")
@RequiredArgsConstructor
public class HubInternalController {

    private final HubQueryService hubQueryService;

    @Operation(
            summary = "내부용 허브 존재여부 조회"
    )
    @GetMapping
    public Set<UUID> validateHubIds(@RequestParam("hubIds") List<UUID> hubIds) {
        return hubQueryService.findValidHubIdsIn(hubIds);
    }

    @Operation(
            summary = "내부용 허브 정보 조회"
    )
    @GetMapping("/getHubInfos")
    public Set<HubResponseDto> getHubInfo(@RequestParam("hubIds") List<UUID> hubIds){
        return hubQueryService.getHubsInternal(hubIds);
    }
}