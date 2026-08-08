package com.logistics.slack.application.service;

import com.logistics.slack.application.dto.query.SlackSearchQuery;
import com.logistics.slack.application.dto.result.SlackDetailResult;
import com.logistics.slack.application.dto.result.SlackListResult;
import com.logistics.slack.domain.entity.Slack;
import com.logistics.slack.domain.entity.SlackStatus;
import com.logistics.slack.domain.repository.SlackQueryRepository;
import com.logistics.slack.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class SlackQueryServiceTest {
    UUID slackMessageId = UUID.randomUUID();
    UUID referenceId = UUID.randomUUID();
    Long senderId = 1L;
    Long receiverId = 2L;

    @InjectMocks
    SlackQueryService slackQueryService;

    @Mock
    private SlackQueryRepository slackQueryRepository;

    private Slack createSlack() {
        Slack slack = Slack.create(
                senderId,
                receiverId,
                "주문 생성",
                referenceId
        );

        ReflectionTestUtils.setField(
                slack,
                "slackMessageId",
                slackMessageId
        );

        return slack;
    }

    @Nested
    @DisplayName("슬랙 메시지 상세 조회")
    class slack_detail {
        @Test
        @DisplayName("슬랙 메시지 상세 조회 성공")
        void slack_detail_success() {
            Slack slack = createSlack();

            given(slackQueryRepository.findByIdAndDeletedAtIsNull(slackMessageId)).willReturn(Optional.of(slack));

            SlackDetailResult slackDetailResult = slackQueryService.getSlack(slackMessageId);

            assertThat(slackDetailResult).isNotNull();
            verify(slackQueryRepository).findByIdAndDeletedAtIsNull(slackMessageId);
        }

        @Test
        @DisplayName("존재하지 않는 슬랙 메시지 상세 조회 시 예외")
        void slack_detail_not_found() {
            given(slackQueryRepository.findByIdAndDeletedAtIsNull(slackMessageId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> slackQueryService.getSlack(slackMessageId))
                    .isInstanceOf(CustomException.class);
        }
    }

    @Nested
    @DisplayName("슬랙 메시지 전체 조회")
    class slack_list {
        @Test
        @DisplayName("슬랙 메시지 전체 조회 성공")
        void slack_lise_empty() {
            LocalDateTime createdFrom = LocalDateTime.of(2030, 8, 1, 0, 0);
            LocalDateTime createdTo = LocalDateTime.of(2030, 8, 2, 0, 0);
            Pageable pageable = PageRequest.of(
                    0,
                    10,
                    Sort.by(Sort.Direction.DESC, "createdAt")
            );
            SlackSearchQuery slackSearchQuery = new SlackSearchQuery(
                    SlackStatus.SUCCESS,
                    senderId,
                    receiverId,
                    referenceId,
                    createdFrom,
                    createdTo,
                    "createdAt",
                    0,
                    10
            );
            Slack slack = createSlack();
            Page<Slack> slackPage = new PageImpl<>(
                    List.of(slack),
                    pageable,
                    1
            );

            given(slackQueryRepository.search(
                    SlackStatus.SUCCESS,
                    senderId,
                    receiverId,
                    referenceId,
                    createdFrom,
                    createdTo,
                    pageable
            )).willReturn(slackPage);
            Page<SlackListResult> result = slackQueryService.getSlacks(slackSearchQuery);

            assertThat(result).hasSize(1);

            verify(slackQueryRepository).search(
                    SlackStatus.SUCCESS,
                    senderId,
                    receiverId,
                    referenceId,
                    createdFrom,
                    createdTo,
                    pageable
            );
        }
    }

}