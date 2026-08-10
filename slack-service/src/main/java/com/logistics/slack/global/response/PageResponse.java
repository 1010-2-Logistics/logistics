package com.logistics.slack.global.response;

import java.util.List;

import lombok.*;
import org.springframework.data.domain.Page;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
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
