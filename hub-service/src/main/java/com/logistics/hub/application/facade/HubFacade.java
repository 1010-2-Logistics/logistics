package com.logistics.hub.application.facade;

import com.logistics.hub.application.service.HubCommandService;
import com.logistics.hub.application.service.HubQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

// Controller가 바라보는 단일 진입점. 여러 서비스를 조합해야 하는 복잡한 유스케이스에서만 쓰고,
// 단순 CRUD는 Controller가 SampleCommandService/SampleQueryService를 바로 호출해도 됩니다.
@Component
@RequiredArgsConstructor
public class HubFacade {

    private final HubCommandService hubCommandService;
    private final HubQueryService hubQueryService;

    public void deleteHub(UUID hubId,Long deletedBy){
        hubCommandService.deleteHub(hubId,deletedBy);

        //TODO
        //이후 허브 삭제로 인해 영향이 갈 수 있는 도메인에 이벤트 전파
        //허브 경로, 주문 등에 삭제 시 영향을 줄 수 있음
        //허브 경로는 연동된 허브가 사라져서 다시 경로를 설정해주어야됨
        //주문 테이블 - 출발 허브나 도착 허브가 사라질 수 있음
        //특히 도착 허브가 사라진 경우 예외 처리가 필요할 것 같음
    }
}
