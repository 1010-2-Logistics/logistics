package com.logistics.slack.application.service;

import com.logistics.slack.domain.entity.Slack;
import com.logistics.slack.domain.repository.SlackQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;


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

        }

        @Test
        @DisplayName("존재하지 않는 슬랙 메시지 상세 조회 시 예외")
        void slack_detail_not_found() {

        }
    }

    @Nested
    @DisplayName("슬랙 메시지 전체 조회")
    class slack_list {
        @Test
        @DisplayName("슬랙 메시지 전체 조회 성공")
        void slack_lise_empty() {

        }
    }

}