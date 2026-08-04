package com.logistics.hub.domain.repository;

import com.logistics.hub.domain.entity.Hub;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface HubCommandRepository {

    Hub save(Hub hub);

    Optional<Hub> findByIdAndDeletedAtIsNull(UUID hubId);

    //위도와 경도가 동일한 허브가 있는지 탐색
    boolean existsByLatitudeAndLongitudeAndDeletedAtIsNull(BigDecimal latitude, BigDecimal longitude);

    //허브주소가 동일한 허브가 있는지 검사
    boolean existsByHubAddressAndDeletedAtIsNull(String hubAddress);

    //허브 수정 - 허브아이디가 같은 경우는 체크 헤제
    boolean existsByLatitudeAndLongitudeAndHubIdNotAndDeletedAtIsNull(BigDecimal latitude, BigDecimal longitude, UUID hubId);
    boolean existsByHubAddressAndHubIdNotAndDeletedAtIsNull(String hubAddress, UUID hubId);

    boolean findByhubIdAndDeletedAtIsNull(UUID hubId);
}
