package com.logistics.order.infrastructure.adapter;


import com.logistics.order.application.dto.result.ProductGetResult;
import com.logistics.order.application.port.ProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductClientAdapter implements ProductPort {

    @Override
    public ProductGetResult getProduct(
            UUID productId
    ) {
        return null;
    }
}
