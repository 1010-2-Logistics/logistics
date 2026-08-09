package com.logistics.user.infrastructure.initializer;

import com.logistics.user.domain.entity.User;
import com.logistics.user.domain.entity.UserRole;
import com.logistics.user.domain.repository.UserCommandRepository;
import com.logistics.user.domain.repository.UserQueryRepository;
import com.logistics.user.global.config.MasterAccountProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 최초 실행 시 MASTER 계정을 생성한다.
 *
 * 활성 MASTER가 이미 존재하면 아무 작업도 하지 않는다.
 */
@Component
@RequiredArgsConstructor
public class MasterAccountInitializer
        implements ApplicationRunner {

    private final UserQueryRepository userQueryRepository;
    private final UserCommandRepository userCommandRepository;
    private final PasswordEncoder passwordEncoder;
    private final MasterAccountProperties properties;

    @Override
    public void run(
            ApplicationArguments args
    ) {

        /*
         * 이미 활성 MASTER가 있다면
         * 초기 계정을 중복 생성하지 않는다.
         */
        boolean masterExists =
                userQueryRepository.existsByRole(
                        UserRole.MASTER
                );

        if (masterExists) {
            return;
        }

        validateProperties();

        System.out.println(
                "MASTER username = [" + properties.username() + "]"
        );

        System.out.println(
                "MASTER username length = "
                        + properties.username().length()
        );

        System.out.println(
                "MASTER slackId = [" + properties.slackId() + "]"
        );

        System.out.println(
                "MASTER slackId length = "
                        + properties.slackId().length()
        );

        /*
         * 환경변수에서 받은 평문 비밀번호를
         * BCrypt 해시로 변환한다.
         */
        String encodedPassword =
                passwordEncoder.encode(
                        properties.password()
                );

        /*
         * 일반 사용자 생성 규칙을 이용해
         * 최초 MASTER를 PENDING으로 생성한다.
         *
         * MASTER는 소속이 없기 때문에
         * companyId, hubId는 null이다.
         */
        User master = User.create(
                properties.username(),
                encodedPassword,
                properties.slackId(),
                UserRole.MASTER,
                null,
                null
        );

        /*
         * 초기 MASTER는 별도 승인자가 존재하지 않으므로
         * 즉시 승인 상태로 만든다.
         */
        master.approve();

        userCommandRepository.save(master);
    }

    private void validateProperties() {
        if (properties.username() == null
                || properties.username().isBlank()
                || properties.password() == null
                || properties.password().isBlank()
                || properties.slackId() == null
                || properties.slackId().isBlank()) {

            throw new IllegalStateException(
                    "초기 MASTER 계정 환경변수가 설정되지 않았습니다."
            );
        }
    }
}