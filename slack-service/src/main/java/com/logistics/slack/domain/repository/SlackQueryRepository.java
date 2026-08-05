package com.logistics.slack.domain.repository;

import com.logistics.slack.domain.entity.Slack;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SlackQueryRepository {

    Optional<Slack> findByIdAndDeletedAtIsNull(UUID sampleId);

    Page<Slack> search(String keyword, Pageable pageable);
}
