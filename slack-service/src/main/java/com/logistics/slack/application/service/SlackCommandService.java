package com.logistics.slack.application.service;

import com.logistics.slack.application.dto.command.SlackCreateCommand;
import com.logistics.slack.application.dto.result.SlackCreateResult;
import com.logistics.slack.domain.repository.SlackCommandRepository;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SlackCommandService {
    private final SlackCommandRepository slackCommandRepository;

    public SlackCreateResult createSlack(SlackCreateCommand slackCreateCommand) {
        return null;
    }

    public void deleteSlack(UUID sampleId, String deletedBy) {

    }
}
