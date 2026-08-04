package com.logistics.delivery.global.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@Builder
public class PageResponse<T> {

    private List<T> content;
    private PageInfo pageInfo;

    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .pageInfo(PageInfo.of(page))
                .build();
    }
}
