package com.logistics.slack.application.service;

import com.logistics.slack.application.dto.query.SlackSearchQuery;
import com.logistics.slack.application.dto.result.SlackDetailResult;
import com.logistics.slack.application.dto.result.SlackListResult;
import com.logistics.slack.domain.entity.Slack;
import com.logistics.slack.domain.repository.SlackQueryRepository;
import com.logistics.slack.global.exception.CustomException;
import com.logistics.slack.global.exception.SlackErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SlackQueryService {

    private final SlackQueryRepository slackQueryRepository;

    public SlackDetailResult getSlack(UUID slackMessageId) {
        Slack slack = slackQueryRepository.findByIdAndDeletedAtIsNull(slackMessageId)
                .orElseThrow(() -> new CustomException(SlackErrorCode.SLACK_NOT_FOUND));

        return SlackDetailResult.from(slack);
    }

    public Page<SlackListResult> getSlacks(
            SlackSearchQuery slackSearchQuery
    ) {
        Page<Slack> slackPage = slackQueryRepository.search(
                slackSearchQuery.status(),
                slackSearchQuery.senderId(),
                slackSearchQuery.receiverId(),
                slackSearchQuery.referenceId(),
                slackSearchQuery.createdFrom(),
                slackSearchQuery.createdTo(),
                slackSearchQuery.pageable()
        );

        return slackPage.map(SlackListResult::from);
    }
}
