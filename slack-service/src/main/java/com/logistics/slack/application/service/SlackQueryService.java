package com.logistics.slack.application.service;

import com.logistics.slack.application.authorization.SlackAuthorizationService;
import com.logistics.slack.application.dto.auth.AuthenticatedUser;
import com.logistics.slack.application.dto.query.SlackSearchQuery;
import com.logistics.slack.application.dto.result.SlackDetailResult;
import com.logistics.slack.application.dto.result.SlackListResult;
import com.logistics.slack.domain.entity.Slack;
import com.logistics.slack.domain.repository.SlackQueryRepository;
import com.logistics.slack.global.exception.CustomException;
import com.logistics.slack.global.exception.SlackErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SlackQueryService {
    private final SlackQueryRepository slackQueryRepository;
    private final SlackAuthorizationService slackAuthorizationService;

    public SlackDetailResult getSlack(
            UUID slackMessageId,
            AuthenticatedUser authenticatedUser
    ) {
        Slack slack = slackQueryRepository.findByIdAndDeletedAtIsNull(slackMessageId)
                .orElseThrow(() -> new CustomException(SlackErrorCode.SLACK_NOT_FOUND));

        slackAuthorizationService.validateAccess(
                authenticatedUser,
                slack
        );

        return SlackDetailResult.from(slack);
    }

    public Page<SlackListResult> getSlacks(
            SlackSearchQuery slackSearchQuery,
            AuthenticatedUser authenticatedUser
    ) {
        int page = validatePage(slackSearchQuery.page());
        int size = normalizeSize(slackSearchQuery.size());
        String sortProperty = validateSort(slackSearchQuery.sort());

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, sortProperty)
        );

        Page<Slack> slackPage = slackQueryRepository.search(
                slackSearchQuery.status(),
                slackSearchQuery.senderId(),
                slackSearchQuery.receiverId(),
                slackSearchQuery.referenceId(),
                slackSearchQuery.createdFrom(),
                slackSearchQuery.createdTo(),
                pageable
        );

        List<SlackListResult> content = slackPage.getContent()
                .stream()
                .filter(slack ->
                        slackAuthorizationService.canRead(
                                authenticatedUser,
                                slack
                        )
                )
                .map(SlackListResult::from)
                .toList();

        return new PageImpl<>(
                content,
                pageable,
                slackPage.getTotalElements()
        );
    }

    private int validatePage(Integer page) {
        if (page == null) {
            return 0;
        }

        if (page < 0) {
            throw new CustomException(SlackErrorCode.SLACK_INVALID_REQUEST);
        }
        return page;
    }

    private int normalizeSize(Integer size) {
        if (size == null) {
            return 10;
        }
        if (size == 10 || size == 30 || size == 50) {
            return size;
        }

        return 10;
    }

    private String validateSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return "createdAt";
        }
        if ("createdAt".equals(sort) || "updatedAt".equals(sort)) {
            return sort;
        }

        throw new CustomException(SlackErrorCode.SLACK_INVALID_REQUEST);
    }
}
