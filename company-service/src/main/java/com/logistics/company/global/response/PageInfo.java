package com.logistics.company.global.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.domain.Page;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PageInfo {

    private String paginationType;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public static PageInfo of(Page<?> page) {
        return PageInfo.builder()
                .paginationType("OFFSET")
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
