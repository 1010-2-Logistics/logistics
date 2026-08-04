package com.logistics.delivery.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.logistics.delivery.global.exception.CustomException;
import com.logistics.delivery.global.exception.DeliveryErrorCode;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DeliveryManagerTest {

    @Test
    void 업체_배송담당자인데_hubId가_없으면_생성_실패() {
        // when & then
        assertThatThrownBy(() -> DeliveryManager.create(1L, null, "U01", ManagerType.COMPANY_DELIVERY_MANAGER, 0))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_MANAGER_TYPE_HUB_MISMATCH);
    }

    @Test
    void 허브_배송담당자인데_hubId가_있으면_생성_실패() {
        // given
        UUID hubId = UUID.randomUUID();

        // when & then
        assertThatThrownBy(() -> DeliveryManager.create(2L, hubId, "U02", ManagerType.HUB_DELIVERY_MANAGER, 0))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(DeliveryErrorCode.DELIVERY_MANAGER_TYPE_HUB_MISMATCH);
    }

    @Test
    void 업체_배송담당자면서_hubId가_있으면_정상_생성() {
        // given
        UUID hubId = UUID.randomUUID();

        // when
        DeliveryManager manager = DeliveryManager.create(3L, hubId, "U03", ManagerType.COMPANY_DELIVERY_MANAGER, 0);

        // then
        assertThat(manager.getHubId()).isEqualTo(hubId);
        assertThat(manager.getManagerType()).isEqualTo(ManagerType.COMPANY_DELIVERY_MANAGER);
    }

    @Test
    void 허브_배송담당자면서_hubId가_없으면_정상_생성() {
        // when
        DeliveryManager manager = DeliveryManager.create(4L, null, "U04", ManagerType.HUB_DELIVERY_MANAGER, 0);

        // then
        assertThat(manager.getHubId()).isNull();
        assertThat(manager.getManagerType()).isEqualTo(ManagerType.HUB_DELIVERY_MANAGER);
    }
}