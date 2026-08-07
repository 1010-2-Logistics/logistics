package com.logistics.hubRoute.presentation.dto.dto.response;

import com.logistics.hubRoute.application.dto.query.HubRouteEdge;
import com.logistics.hubRoute.domain.entity.HubRoute;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record HubRouteFindResponseDto(
        UUID startHubId,
        UUID endHubId,
        int totalDuration,              //총 소요 시간
        BigDecimal totalDistance,        //총 이동 거리
        List<HubRouteStepDto> steps    //경유 하는 허브 순서
) {
    public record HubRouteStepDto(
            int sequence,       //거치는 허브 순서
            UUID startHubId,
            UUID endHubId,
            int duration,
            BigDecimal distance
    ){}

    //경로가 존재하는 경우
    public static HubRouteFindResponseDto from(HubRoute route){
        HubRouteStepDto step = new HubRouteStepDto(
                1,
                route.getStartHubId(),
                route.getEndHubId(),
                route.getDuration(),
                route.getDistance()
        );
        return new HubRouteFindResponseDto(
                route.getStartHubId(),
                route.getEndHubId(),
                route.getDuration(),
                route.getDistance(),
                List.of(step)
        );
    }

    //직통 경로가 없는 경우(알고리즘)
    public static HubRouteFindResponseDto fromEdges(UUID startHubId, UUID endHubId, List<HubRouteEdge> edges){
        List<HubRouteStepDto> steps = new ArrayList<>();
        int totalDuration =0;
        BigDecimal totalDistance = BigDecimal.ZERO;

        for(int i = 0; i<edges.size();i++){
            HubRouteEdge edge = edges.get(i);
            steps.add(new HubRouteStepDto(
                    i+1,
                    edge.startHubId(),
                    edge.endHubId(),
                    edge.duration(),
                    edge.distance()
            ));
            totalDuration += edge.duration();
            totalDistance = totalDistance.add(edge.distance());
        }
        return new HubRouteFindResponseDto(
                startHubId,
                endHubId,
                totalDuration,
                totalDistance,
                steps
        );
    }

}
