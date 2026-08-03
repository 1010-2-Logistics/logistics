package com.logistics.order.presentation.controller;


import com.logistics.order.application.service.OrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/samples")
@RequiredArgsConstructor
public class QueryController {

    private final OrderQueryService sampleQueryService;

}
