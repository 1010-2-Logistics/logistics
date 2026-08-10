package com.logistics.hubRoute.application.service;

import com.logistics.hubRoute.application.dto.query.GetHubRouteQuery;
import com.logistics.hubRoute.application.dto.query.HubRouteEdge;
import com.logistics.hubRoute.application.dto.query.SearchHubRouteQuery;
import com.logistics.hubRoute.domain.entity.HubRoute;
import com.logistics.hubRoute.domain.repository.HubRouteQueryRepository;
import com.logistics.hubRoute.global.exception.CustomException;
import com.logistics.hubRoute.global.exception.HubRouteErrorCode;
import com.logistics.hubRoute.global.response.PageResponse;
import com.logistics.hubRoute.infrastructure.feign.client.HubClient;
import com.logistics.hubRoute.presentation.dto.dto.request.HubRouteFindRequestDto;
import com.logistics.hubRoute.presentation.dto.dto.response.HubRouteFindResponseDto;
import com.logistics.hubRoute.presentation.dto.dto.response.HubRouteResponseDto;
import com.logistics.hubRoute.presentation.dto.dto.response.HubRouteSummaryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HubRouteQueryService {

    private final HubRouteQueryRepository hubRouteQueryRepository;
    private final HubClient hubClient;

    public HubRoute get(GetHubRouteQuery query) {
        return hubRouteQueryRepository.findByIdAndDeletedAtIsNull(query.hubRouteId())
                .orElseThrow(() -> new CustomException(HubRouteErrorCode.HUB_ROUTE_NOT_FOUND));
    }


    @Cacheable(
            value = "hubRoute",
            key = "'search:' + (#query.hubRouteId() != null ? #query.hubRouteId().toString() : 'all') + ':page:' + #query.pageable().pageNumber + ':size:' + #query.pageable().pageSize",
            unless = "#result == null"
    )
    public PageResponse<HubRouteSummaryResponseDto> search(SearchHubRouteQuery query) {
        // 기존 레포지터리 조회 로직 유지
        Page<HubRoute> page = hubRouteQueryRepository.search(query.hubRouteId(), query.pageable());
        Page<HubRouteSummaryResponseDto> responsePage = page.map(HubRouteSummaryResponseDto::from);

        return PageResponse.of(responsePage);
    }

    @Cacheable(value = "hubRoute", key = "#query.hubRouteId()")
    public HubRouteResponseDto getCachedDto(GetHubRouteQuery query) {
        HubRoute hubRoute = get(query);
        return HubRouteResponseDto.from(hubRoute);
    }



    //허브 경로 조회[연결 되어 있지 않은 허브도 조회 가능]
    @Cacheable(
            value = "hubRoute",
            key = "#requestDto.startHubId().toString() + ':' + #requestDto.endHubId().toString()",
            unless = "#result == null"
    )
    public HubRouteFindResponseDto findHubRoute(HubRouteFindRequestDto requestDto) {

        UUID startHubId = requestDto.startHubId();
        UUID endHubId = requestDto.endHubId();

        //출발 허브와 도착허브가 동일한지 확인
        if (startHubId.equals(endHubId)) {
            throw new CustomException(HubRouteErrorCode.HUB_START_END_SAME);
        }

        //출발 허브와 도착 허브가 존재하는지 확인
        List<UUID> targetHubIds = List.of(startHubId,endHubId);
        Set<UUID> existingHubIds = hubClient.validateHubIds(targetHubIds);

        if(!existingHubIds.contains(startHubId)){
            throw new CustomException(HubRouteErrorCode.START_HUB_NOT_FOUND);
        }
        if(!existingHubIds.contains(endHubId)){
            throw new CustomException(HubRouteErrorCode.END_HUB_NOT_FOUND);
        }

        //DB에 존재하는 경로인지 확인 -> 존재한다면 해당 경로 반환
        Optional<HubRoute> existingRoute = hubRouteQueryRepository.findByStartHubIdAndEndHubIdAndDeletedAtIsNull(startHubId, endHubId);
        if(existingRoute.isPresent()){
            return HubRouteFindResponseDto.from(existingRoute.get());
        }

        //직통 경로가 없는 경우
        //그래프 구성
        Map<UUID, List<HubRouteEdge>> graph = buildGrap();

        List<HubRouteEdge> calculatedPath = calculateShortestPathByDijkstra(graph,startHubId,endHubId);
        if(calculatedPath.isEmpty()){
            throw new CustomException(HubRouteErrorCode.HUB_ROUTE_NOT_FOUND);
        }

        return HubRouteFindResponseDto.fromEdges(startHubId,endHubId,calculatedPath);
    }

    //DB에 있는 전체 허브 경로 추출 및 그래프 생성
    private Map<UUID, List<HubRouteEdge>> buildGrap(){
        //전체 검색과 다르게 페이지 정보는 제외하고 전체 검색
        List<HubRoute> allRoutes = hubRouteQueryRepository.findAllByDeletedAtIsNull();
        Map<UUID, List<HubRouteEdge>> graph = new HashMap<>();

        for (HubRoute route : allRoutes){
            graph.computeIfAbsent(route.getStartHubId(), k -> new ArrayList<>())
                    .add(new HubRouteEdge(
                            route.getStartHubId(),
                            route.getEndHubId(),
                            route.getDuration(),
                            route.getDistance()
                    ));
        }
        return  graph;
    }


    //길찾기 알고리즘 다익스트라
    //너비우선탐색은 가중치를 고려하지 않음 -> 말그대로 최소 환승, 최소 경유지
    //다익스트라는 가중치를 고려함 -> 최단거리나, 최소 소요시간에 알맞음
    private List<HubRouteEdge> calculateShortestPathByDijkstra(
            Map<UUID, List<HubRouteEdge>> graph,
            UUID startHubId,
            UUID endHubId
    ){
        //우선 순위 큐, 누적 소요시간 기준 오름차순
        PriorityQueue<NodeCost> pq = new PriorityQueue<>(Comparator.comparingInt(NodeCost::cost));
       //각 허브 노드까지의 최소 소요 시간 기록하는 테이블
        Map<UUID, Integer> minCosts = new HashMap<>();
        // 경로 역추적용
        Map<UUID, HubRouteEdge> parentEdges = new HashMap<>();


        //시작지점 설정, 비용 0
        pq.add(new NodeCost(startHubId,0));
        minCosts.put(startHubId,0);

        while(!pq.isEmpty()){
            NodeCost current = pq.poll();
            UUID currentHub = current.hubId();
            int currentCost = current.cost();

            //목적지 도착, 최단 경로 확정, 역추적 실행
            if(currentHub.equals(endHubId)){
                return reconstructPath(startHubId,endHubId,parentEdges);
            }

            //큐에서 꺼낸 비용이 이미 계산된 최소 비용보다 크면 이미 방문한 노드
            if(currentCost > minCosts.getOrDefault(currentHub, Integer.MAX_VALUE)){
                continue;
            }

            //인접한 허브 간선들을 순회하며 누적 소요 시간 계산
            List<HubRouteEdge> neighbors = graph.getOrDefault(currentHub, Collections.emptyList());
            for(HubRouteEdge edge : neighbors){
                UUID nextHub = edge.endHubId();
                int newCost = currentCost + edge.duration();

                //더 적은 시간으로 도달가능한 경로를 발견한 경우
                if(newCost < minCosts.getOrDefault(nextHub, Integer.MAX_VALUE)){
                    minCosts.put(nextHub,newCost);
                    parentEdges.put(nextHub,edge);
                    pq.add(new NodeCost(nextHub, newCost));
                }
            }
        }

        //경로를 찾지 못한 경우
        return Collections.emptyList();
    }

    //목적지로부터 시작 허브까지 역으로 추적한 후,
    //순서에 맞게(출발지 -> 목적지)로 간선 리스트를 뒤집는 메서드
    private List<HubRouteEdge> reconstructPath(UUID startHubId,
                                               UUID endHubId,
                                               Map<UUID, HubRouteEdge> parentEdges){
        LinkedList<HubRouteEdge> path = new LinkedList<>();
        UUID curr = endHubId;

        //도착 허브에서 시작허브까지 백트래킹
        while(!curr.equals(startHubId)){
            HubRouteEdge edge = parentEdges.get(curr);
            if(edge == null) break;
            path.addFirst(edge);
            curr = edge.startHubId();
        }
        return path;
    }

    //다익스트라에 사용하는 내부 레코드 (현재 허브Id, 누적 소요시간)
    private record NodeCost(UUID hubId, int cost){}
}
