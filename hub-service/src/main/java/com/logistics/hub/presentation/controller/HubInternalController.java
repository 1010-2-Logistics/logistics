package com.logistics.hub.presentation.controller;

import com.logistics.hub.application.service.HubQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/internal/v1/hubs")
@RequiredArgsConstructor
public class HubInternalController {

    private final HubQueryService hubQueryService; // hub-service의 Service/Repository 연결

    @GetMapping
    public Set<UUID> validateHubIds(@RequestParam("hubIds") List<UUID> hubIds) {
        return hubQueryService.findValidHubIdsIn(hubIds);
    }
}