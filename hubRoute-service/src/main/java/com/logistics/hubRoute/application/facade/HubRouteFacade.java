package com.logistics.hubRoute.application.facade;

import com.logistics.hubRoute.application.service.HubRouteCommandService;
import com.logistics.hubRoute.application.service.HubRouteQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

// Controller가 바라보는 단일 진입점. 여러 서비스를 조합해야 하는 복잡한 유스케이스에서만 쓰고,
// 단순 CRUD는 Controller가 SampleCommandService/SampleQueryService를 바로 호출해도 됩니다.
@Component
@RequiredArgsConstructor
public class HubRouteFacade {

    private final HubRouteCommandService hubRouteCommandService;
    private final HubRouteQueryService hubRouteQueryService;

//    public void deleteHub(UUID hubId,Long deletedBy){
//        hubRouteCommandService.deleteHub(hubId,deletedBy);
//
//    }
}
