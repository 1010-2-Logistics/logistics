# user-service

새 서비스를 만들 때 이 폴더를 복사해서 시작하는 템플릿입니다.

## 사용 방법

1. 폴더 전체를 복사해서 이름을 바꾸세요 (예: `order-service`, `delivery-service`)
2. 패키지명을 `com.logistics.user` → `com.logistics.{service}`로 바꾸세요 (IDE 리팩터링 기능 사용 권장)
3. `User` 관련 이름들을 실제 도메인 이름으로 바꾸세요
   - `User.java` → `{Domain}.java`
   - `UserStatus.java` → `{Domain}Status.java`
   - `UserCommandService`, `UserQueryService`, `UserFacade` 등도 동일하게
   - `UserErrorCode` → `{Domain}ErrorCode` (네이밍 컨벤션: `{도메인명}_{에러타입}`)
4. `settings.gradle`에 본인 서비스 include 라인 주석 해제
5. `application.yml`의 `spring.application.name`, `server.port`, `application-local.yml`의 스키마명을 컨벤션 포트표에 맞게 수정
6. `Dockerfile`의 `:user-service` 부분을 서비스명으로 수정
7. `infrastructure/feign/client/HubClient.java`는 예시입니다 — 실제로 호출할 서비스에 맞게 수정/삭제하세요

## 구조
```
{service}/
├── presentation/    HTTP 요청/응답 (Controller, Request/Response DTO)
├── application/     유스케이스 조합 (Facade, CommandService/QueryService, Command/Query DTO, Event, Port)
├── domain/          순수 비즈니스 로직 (Entity, Repository 인터페이스)
├── infrastructure/  외부 연동 구현체 (JPA Repository 구현체, Feign, Messaging, Config)
└── global/          이 서비스만의 공통 응답/예외/BaseEntity
    (팀 공용 common 모듈은 사용하지 않기로 했으므로, 이 global 패키지를 각 서비스가 자체적으로 가집니다)
```

## 주의사항
- `global` 패키지는 각 서비스마다 로컬로 복사해서 씁니다 (팀 common 모듈 미사용 결정에 따름).
  나중에 `common`에서 뭔가 고쳐져도 자동으로 반영되지 않으니, 팀 공지 있으면 각자 반영하세요.
