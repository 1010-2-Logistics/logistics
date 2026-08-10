package com.logistics.hubRoute.global.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Getter
@Builder
@NoArgsConstructor  // Jackson 역직렬화를 위한 기본 생성자 추가
@AllArgsConstructor // @Builder + @NoArgsConstructor 조합 시 필요한 전체 생성자 추가
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
