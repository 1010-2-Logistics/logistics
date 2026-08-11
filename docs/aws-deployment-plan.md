# AWS 배포 아키텍처 설계 및 IaC 구축 계획

## Context

이 프로젝트(Sparta Logistics)는 Spring Boot 4.1 / Java 21 기반의 **12개 모듈** MSA다. 현재는 `docker-compose.yml`로 미들웨어(PostgreSQL·Redis·RabbitMQ·Zipkin)만 띄우고 애플리케이션은 IDE에서 로컬 실행하는 상태이며, AWS에 배포된 적이 없다.

**목표**: 포트폴리오/학습 목적으로 전체 시스템을 AWS에 Terraform으로 배포하고, 약 1주간 시연한 뒤 `terraform destroy`로 정리한다.

**이 문서의 성격**: 각 단계에 구체적 TODO와 **검증 게이트**를 두고, 게이트를 통과해야만 다음 단계로 넘어가는 직렬 구조다. 게이트는 "했다"가 아니라 **명령어와 기대 출력**으로 정의한다.

> **개정 이력**
> - 2026-08-09 초안 — 11개 모듈 기준
> - 2026-08-09 개정 — `ai-service` 추가(develop PR #128)로 12개 모듈 기준 재계산. Redis·RabbitMQ를 프라이빗 서브넷으로 이동. 차단 요소 B-1 정정 및 B-4·B-5 추가
> - 2026-08-09 개정 — 인증/인가 조사. 차단 요소 B-6·B-7 추가, 보안 미결 항목 S-1 기록. 누락 환경변수 4개 반영
> - 2026-08-09 개정 — S-1(Gateway 인증)을 배포 범위에 포함. **Phase 1.5 신설**, B-6 해결책을 이에 맞게 교체
> - 2026-08-09 개정 — **Phase 0 신설.** 초안은 AWS 계정·도구가 준비돼 있다고 가정했으나 미확인이었음. AWS CLI·Terraform·gh 미설치, 자격증명 미설정 확인. vCPU 쿼터 리드타임 리스크 반영
> - 2026-08-09 개정 — **G0 통과**(비용 알림 제외). vCPU 쿼터 32로 확인되어 리드타임 리스크 해소
> - 2026-08-09 개정 — **`ai-service` 보류 결정**으로 전 구간 기대값 재계산(11개 기준). **B-1·B-2를 환경변수 오버라이드 방식으로 전환**하여 수정 파일 16개 → 5개로 축소(팀원 브랜치 충돌 회피). `docker-compose.prod.yml` → `app.yml`/`data.yml`로 분리. B-3 정정(주석 해제가 아니라 **8개 신규 작성**), B-4 축소(`hubroute.sql` 1개)
> - 2026-08-09 개정 — **"서비스 내부 코드 수정 최소화" 원칙 적용.** 배포에 불필요한 수정(`company`·`product` 프로파일, Redis TTL, `app.master.*` yml 명시)을 전부 제외. **서비스 내부 코드 수정은 `@Profile("local")` 삭제 1줄로 축소**
> - 2026-08-09 개정 — `infra/postgres/hubroute.sql`을 `hub.sql`에 병합 (별도 파일 대신 `order-inventory-slack.sql`처럼 한 파일에 여러 스키마를 두는 기존 관례를 따름). 이하 문서의 `hubroute.sql` 언급은 병합 전 작업 당시 기록
> - 2026-08-09 개정 — **Phase 1·1.5·2 실행 완료(G1·G1.5·G2 통과).** B-3·B-4·B-6 반영. `infra/postgres/init.sql`(오래된 중복 파일, `p_user` 스키마를 실제로 막고 있었음) 삭제. Dockerfile 11개를 jar 사전빌드 + alpine 베이스 구조로 전환(Phase 2를 Phase 1로 선당김). G2의 이미지 크기 기준을 실측 기반으로 350MB → 460MB로 수정. ECR 리포지토리명 제약으로 `hubRoute-service` → `hubroute-service`(소문자) 정정

### 모듈 구성 (전체 12개 / 배포 대상 11개)

| 구분 | 모듈 |
|---|---|
| 인프라 (2) | `eureka-server`(19090), `gateway-service`(19091) |
| 비즈니스 (9) | `user`, `hub`, `hubRoute`, `company`, `product`, `order`, `inventory`, `delivery`, `slack` |
| **보류 (1)** | ~~`ai`~~ — 미완성으로 이번 배포 제외 (B-5) |

이 문서의 모든 기대값은 **배포 대상 11개** 기준이다.

| 지표 | 값 |
|---|---:|
| 배포 모듈 | 11 |
| Eureka 등록 (비즈니스 9 + gateway) | 10 |
| Gateway 라우트 | 9 |
| DB 스키마 | 9 |
| ECR 리포지토리 | 11 |
| 로컬 검증 컨테이너 (앱 11 + 미들웨어 4) | 15 |
| App EC2 컨테이너 (앱 11 + Zipkin) | 12 |
| Data EC2 컨테이너 | 3 |

---

## 진행 상황 (2026-08-09 기준)

브랜치 `feature/aws-deployment-iac` (기준 `origin/develop` = `57cc5f6`). **아직 커밋 없음 — 전부 워킹트리 상태.**

| Phase | 상태 |
|---|---|
| **Phase 0** | ✅ **G0 통과** (비용 알림 1건만 미등록). AWS CLI 2.36.17 / Terraform 1.15.8 설치, IAM `user/Admin`, `ap-northeast-2`, vCPU 쿼터 32 |
| **Phase 1** | ✅ **G1 통과** |
| **Phase 1.5** | ✅ **G1.5 통과** |
| **Phase 2** | ✅ **G2 통과** |
| **Phase 3** | ✅ **G3 통과** |
| **Phase 4** | ✅ **G4 통과** |
| **Phase 5** | ✅ **G5 통과** |
| **Phase 6** | ✅ **G6 통과 — 배포 완료** |
| **Phase 7** | ✅ **G7 통과** — `main` 병합 후 실제 자동 배포 성공 |
| Phase 8 | 🔄 진행 중 — README·비용 기록 완료, destroy는 시연 종료 후 |

**Phase 1**
- B-6: `MockGatewayAuthenticationFilter:37`의 `@Profile("local")` 삭제
- B-4: `infra/postgres/hubroute.sql` 신규 (엔티티 기반 DDL)
- B-3: `gateway-service/application.yml`에 라우트 8개 신규 작성 (총 9개)
- **`infra/postgres/init.sql` 삭제** — 오래된 중복 파일. 파일명 알파벳 순으로 `user-slack.sql`보다 먼저 실행되면서 `p_user`에 `rejection_reason` 컬럼이 없는 옛 버전을 만들어 최신 정의를 무력화시키고 있었음 (검증 중 발견)
- `logistics_pgdata` 볼륨 재생성 — 팀 로컬 개발용 볼륨과 이름이 같아 예전 스키마로 이미 초기화돼 있었음
- Dockerfile 11개를 Phase 2에서 앞당겨 재작성 — 컨테이너 내부 Gradle 빌드 제거, 사전 빌드한 jar만 복사하는 구조로 전환 (`delivery-service/Dockerfile`의 `template-service` 오타도 같이 정정됨)
- G1 게이트 전 항목 통과 (컨테이너 15개, Eureka 10, 라우트 9, 환경변수 오버라이드 실측 확인, MASTER 부트스트랩 확인)

**Phase 1.5**
- `gateway-service`에 `JwtAuthenticationGlobalFilter` 신규 — 헤더 스트리핑(최우선) → 화이트리스트 → JWT 검증 → 헤더 재주입
- G1.5 게이트 전 항목 통과 (헤더 위조 401 확인 — S-1 핵심 회귀 테스트)
- `gateway-service/application-prod.yml`은 생성하지 않음 — prod에서만 달라지는 값이 없어(`jwt.secret`은 기본 yml에서 이미 환경변수로 풀림) 빈 파일을 만드는 실익이 없다고 판단

**Phase 2**
- Dockerfile 11개를 `eclipse-temurin:21-jre-alpine` + 선택 스테이지 구조로 추가 전환 — 기존 `eclipse-temurin:21-jre` 태그가 Ubuntu 풀 이미지로 바뀌며 700~830MB까지 커져 있었음. 전환 후 402~455MB (G2 기준을 350MB → 460MB로 수정)
- alpine 전환으로 `docker-compose.app.yml`의 healthcheck 11개를 `bash /dev/tcp` → `nc -z`로 변경 (alpine엔 bash 없음)
- `infra/terraform/` 신규 (`providers.tf`/`variables.tf`/`ecr.tf`/`outputs.tf`) — ECR 리포지토리 11개 + 라이프사이클 정책(이미지 3개 유지) `terraform apply` 완료
- `.gitignore`에 `*.tfstate*`/`.terraform/`/`terraform.tfvars` 추가
- ECR 리포지토리명은 소문자만 허용되어 `hubRoute-service` → `hubroute-service`로 정정
- `gateway-service` 이미지 1개를 ECR에 푸시해 연동 확인 (나머지 10개는 Phase 7 CI/CD에서 자동 푸시)

**Phase 3**
- `infra/terraform/network.tf` 신규 — VPC(`10.0.0.0/16`), 퍼블릭/프라이빗 서브넷, IGW, 라우팅 테이블 2개, 보안그룹 3개(`app-sg`/`data-sg`/`nat-sg`), NAT Instance(t3.nano), SSH 키페어
- NAT AMI는 최신 Amazon Linux 2023을 `aws_ssm_parameter`(`/aws/service/ami-amazon-linux-latest/...`)로 조회 — 하드코딩 없음. fck-nat 대신 문서의 기본안(AL2023 + user-data)으로 구현
- SG `description`은 AWS 제약상 영문만 가능해 한글 설명을 영문으로 작성
- `terraform.tfvars` 신규(gitignore 대상) — `my_ip`(SSH/Gateway/Zipkin 접근 제한용, `1.239.41.239/32`), `ssh_public_key`(사용자가 직접 `ssh-keygen`으로 생성, 개인키는 다루지 않음)
- G3 게이트 전 항목 통과 — `plan` 무변경, `0.0.0.0/0` 인바운드 0개, NAT `source_dest_check` 비활성, 프라이빗 라우팅이 NAT ENI를 가리킴 확인

**Phase 4**
- `infra/terraform/compute.tf` 신규 — App EC2(m5.large, 퍼블릭), Data EC2(t3.small, 프라이빗), IAM 역할+인스턴스 프로파일(`AmazonEC2ContainerRegistryReadOnly`), 양쪽 공통 user-data(Docker+compose plugin+swap 2GB)
- **NAT 장애 발견 및 수정 (G4 검증 중)**: Amazon Linux 2023엔 `iptables` 명령어가 기본 설치돼 있지 않아(nftables 네이티브) NAT Instance의 MASQUERADE 규칙 추가가 조용히 실패 — Data EC2가 완전히 인터넷 차단된 상태였음. `iptables-services` 패키지 설치 + `systemctl enable iptables`로 재부팅 복원까지 포함해 수정. NAT 인스턴스에 라이브 패치 후 `network.tf`에도 반영(단, `user_data` 재적용은 인스턴스 stop/start를 유발해 공인 IP가 바뀌므로 파일만 갱신하고 재적용은 보류)
- 배스천 경유 SSH는 `-J`(ProxyJump)가 아니라 `ProxyCommand`로 구현 — `-J`는 점프 호스트 인증에 커스텀 키 경로(`~/.ssh/logistics`, 기본 경로 아님)를 안 물려받아 실패함
- G4 게이트 전 항목 통과 — App/Data EC2 접속, NAT 경유 인터넷 아웃바운드(Docker Hub 401 확인), Docker+swap 2GB 양쪽 정상, App EC2의 ECR 로그인 성공

**Phase 5**
- `docker-compose.data.yml` · `.env` · `infra/postgres/*.sql`(6개)를 scp로 Data EC2에 전송 후 `docker compose up -d`
- Postgres 볼륨은 문서의 `/data/pgdata` 바인드 대신 **기존에 검증된 named volume(`pgdata`) 방식 그대로 사용** — 로컬 G1에서 검증한 것과 동일한 compose 파일을 그대로 재사용하는 편이 "로컬에서만 되는 구성"을 만들지 않는다는 Phase 1 원칙에 더 맞는다고 판단. 어차피 같은 EBS 루트 볼륨 위에 있어 영속성 차이는 없음
- G5 게이트 전 항목 통과 — 스키마 9개, 테이블 12개(엔티티와 대조), App→Data 3개 포트(5432/6379/5672) 연결, Postgres 재시작 후 데이터 영속 확인

**Phase 6 — 배포 완료**
- 나머지 10개 이미지(gateway-service는 Phase 2에서 이미 푸시) ECR 푸시 완료, 11개 리포 전부 이미지 보유
- App EC2용 `.env`는 로컬 `.env`를 scp로 복사 후 원격 `sed`로 `DB_HOST`/`REDIS_HOST`/`RABBITMQ_HOST`만 Data EC2 프라이빗 IP(`10.0.2.181`)로 치환 + `IMAGE_REGISTRY`/`IMAGE_TAG` 추가 — 시크릿 값은 내가 직접 보지 않음
- 3배치 순차 기동(Zipkin+eureka → gateway → 업무 서비스 9개를 3개씩) 전부 정상, 최종 여유 메모리 3.99GB
- G6 게이트 전 항목 통과 — Eureka 10개, 외부(내 PC)에서 회원가입→승인→로그인→토큰→리소스 생성까지 실제 왕복 성공, 재부팅 후 12개 컨테이너 자동 복구
- ~~**미해결 이슈**: Zipkin 수신 자체는 정상(수동 span 202 확인)이지만 12개 서비스 전부 span을 전송하지 않음~~ → **2026-08-11 해결** (아래 참조)

**Phase 7 (인프라·워크플로 작성 완료, 실행 검증은 보류)**
- `infra/terraform/cd.tf` 신규 — GitHub OIDC 프로바이더(`token.actions.githubusercontent.com`), `main` 브랜치 푸시로만 assume 가능한 IAM 역할(`sub: repo:1010-2-Logistics/logistics:ref:refs/heads/main`), ECR push 권한(11개 리포로 스코프) + SSM SendCommand 권한(App EC2 인스턴스로 스코프)
- App EC2의 기존 IAM 역할에 `AmazonSSMManagedInstanceCore` 추가 — AL2023은 SSM 에이전트가 기본 구동 상태라 정책만 붙이면 관리 대상으로 등록됨
- `.github/workflows/deploy.yml` 신규 — `main` 푸시 트리거, JDK 21 빌드 → OIDC로 자격증명 획득 → 11개 이미지 빌드/푸시(`:latest` + `:{sha}`) → SSM Send-Command로 App EC2에서 pull & 재기동
- **헬스체크는 App EC2 컨테이너 내부(localhost)에서 수행** — `app-sg`의 19091이 본인 IP로만 열려 있어 GitHub Actions 러너 IP에서는 외부 헬스체크가 원천적으로 불가능. SSM 스크립트 안에 재시도 루프로 내장
- 리포지토리 시크릿은 전혀 쓰지 않음 — 역할 ARN·인스턴스 ID·ECR 레지스트리 전부 시크릿이 아니라 워크플로 파일에 평문으로 기록(민감정보 아님)
- `gh` CLI 미설치 상태로 확인됨 — Phase 0에서 "필요해지면 설치"로 미뤄둔 항목. 사용자가 설치·로그인 완료

**G7 실행 결과 — 첫 실행 실패 후 원인 2건 해결하고 통과**

1. **OIDC `sub` 클레임 형식 불일치 (직접 원인)** — `Not authorized to perform sts:AssumeRoleWithWebIdentity`로 실패. GitHub이 조직·리포지토리 **불변 ID를 포함한 형식**으로 토큰을 발급하고 있었음:
   ```
   신뢰 조건: repo:1010-2-Logistics/logistics:ref:refs/heads/main
   실제 토큰: repo:1010-2-Logistics@312060404/logistics@1320049011:ref:refs/heads/main
   ```
   확인 방법은 `gh api repos/{owner}/{repo}/actions/oidc/customization/sub`. ID 기반 접두사를 `github_sub_prefix` 변수로 분리해 조건을 수정. **이름이 바뀌어도 동작하고 동명의 다른 리포지토리에는 권한이 넘어가지 않아 오히려 더 안전한 형식이다.**

2. **CRLF 줄바꿈으로 인한 의도치 않은 인스턴스 재시작 (사전 차단)** — 팀 브랜치를 pull하면서 `core.autocrlf=true`가 `.tf` 파일을 CRLF로 변환. Terraform이 `user_data` 변경으로 인식해 **App/Data EC2 stop/start(=공인 IP 변경, 서비스 중단)** 가 plan에 잡힘. `plan`에서 발견해 적용 전에 차단했고, `.gitattributes`로 `.sh`·`.tf`·`.yml`·`.sql`을 LF 고정. 셸 스크립트가 CRLF로 배포되면 리눅스에서 `$'\r': command not found`로 실패하므로 이 설정은 팀 전체에 유효하다.

**G7 게이트 전 항목 통과 (2026-08-10)**

| 검증 | 결과 |
|---|---|
| 워크플로 성공 | 13개 스텝 전부 성공 (빌드→OIDC→ECR→SSM→헬스체크) |
| ECR 이미지 태그 = 커밋 SHA | `e573933d…` + `latest` 동시 확인 |
| 순차 기동 동작 | 컨테이너 uptime이 4분/2분/1분으로 갈려 3배치 순차 기동이 실제로 작동했음을 확인 |
| 배포 후 서비스 정상 | 12개 컨테이너 healthy, 외부에서 로그인→토큰→API 호출 200 |
| S-1 회귀 | 위조 헤더 요청 401 유지 |
| 리포지토리 시크릿에 AWS 키 없음 | `gh secret list` 빈 출력 (OIDC만 사용) |
- **재배포 시 순차 기동 리스크 발견 및 수정**: 최초 `deploy.yml`은 재배포 때 `docker compose up -d`로 11개 컨테이너를 한 번에 올리는 구조였음 — Phase 6에서 확인된 "m5.large(2vCPU)에서 11개 JVM 동시 부팅 시 CPU 포화로 Eureka 등록 타임아웃" 문제가 재배포마다 재현될 수 있었음. `infra/scripts/deploy-app.sh` 신규 작성으로 Phase 6과 동일한 순차 기동(eureka→gateway→3개씩 3배치, 각 단계 헬스체크 대기)을 SSM 스크립트에도 적용. `aws ssm wait command-executed`의 기본 타임아웃도 순차 기동 전체 소요시간보다 짧을 수 있어 직접 폴링(최대 15분)으로 교체

---

## Zipkin 트레이스 미수집 — 해결 (2026-08-11)

Phase 6에서 "부차" 우선순위로 미해결 기록했던 항목. **원인이 두 겹이었고 둘 다 조용히 실패하는 유형**이라 발견이 늦었다.

### 원인 ① Spring Boot 4에서 프로퍼티 경로가 바뀜

```
Boot 3.x:  management.zipkin.tracing.endpoint          ← 기존 설정 (무시됨)
Boot 4.x:  management.tracing.export.zipkin.endpoint   ← 실제 필요한 키
```

Spring Boot는 모르는 프로퍼티가 있어도 에러를 내지 않고 무시한다. 그래서 **설정이 존재하는데도 기본값(`http://localhost:9411/api/v2/spans`)으로 동작**했고, 컨테이너에서 `localhost`는 자기 자신이므로 span이 어디에도 도달하지 않았다. B-1(Eureka·Zipkin 주소 하드코딩)과 같은 유형의 함정이다.

확인 방법 — 라이브러리에 동봉된 메타데이터가 정답을 갖고 있다:
```bash
unzip -p <app>.jar 'BOOT-INF/lib/spring-boot-zipkin-*.jar' > /tmp/z.jar
unzip -p /tmp/z.jar META-INF/spring-configuration-metadata.json
```

의존성도 함께 교체했다: `io.zipkin.reporter2:zipkin-reporter-brave` → `org.springframework.boot:spring-boot-starter-zipkin`.

### 원인 ② 샘플링 결정은 최초 진입점에서 내려져 하위로 전파된다

프로퍼티를 고친 뒤에도 `gateway-service`·`eureka-server`만 수집되고 업무 서비스는 여전히 누락됐다.

업무 서비스 9개는 전부 `management.tracing.sampling.probability: 1.0`을 갖고 있었지만, **`gateway-service`에는 샘플링 설정 자체가 없어 기본값 10%로 동작**하고 있었다. 분산 추적은 요청이 처음 진입한 지점에서 "이 요청을 기록할지"를 결정하고 그 결정을 전파 헤더로 하위에 넘기므로, **하위 서비스가 1.0이어도 Gateway가 "기록 안 함"으로 판단하면 따를 수밖에 없다.**

증상이 특히 헷갈렸던 이유 — 로그에는 trace ID가 정상적으로 찍힌다:
```
ERROR 1 --- [user-service] [io-19092-exec-5] [6a7aab8a…-9738911213cd7428]
                                              └ traceId ┘ └ spanId ┘
```
추적 컨텍스트는 생성되지만 **샘플링되지 않아 전송만 안 되는** 상태였다. "설정도 맞고 연결도 되는데 왜 안 오지"의 정체가 이것이다.

`eureka-server`만 예외적으로 수집됐던 건 클라이언트 10개가 30초마다 하트비트를 보내 **10%만 걸려도 절대량이 컸기 때문**이다.

### 수정 파일

| 경로 | 내용 |
|---|---|
| `build.gradle` (루트) | `zipkin-reporter-brave` → `spring-boot-starter-zipkin` |
| 서비스 `application.yml` 10개 | `management.zipkin.tracing.endpoint` → `management.tracing.export.zipkin.endpoint` |
| `gateway-service/application.yml` | `management.tracing.sampling.probability: 1.0` 추가 |
| `docker-compose.app.yml` | `MANAGEMENT_ZIPKIN_TRACING_ENDPOINT` → `MANAGEMENT_TRACING_EXPORT_ZIPKIN_ENDPOINT` (3곳) |

`management.tracing.sampling.probability`는 Boot 4에서도 이름이 그대로라 수정 대상이 아니다.

### 검증 (로컬)

```bash
curl -s "http://localhost:9411/api/v2/services"
# ["eureka-server","gateway-service","hub-service","inventory-service",
#  "order-service","product-service","user-service"]
```

호출한 서비스가 순차적으로 나타나는 것을 확인. Zipkin은 **span을 받은 적 있는 서비스만** 목록에 보여주므로, 목록에 없는 서비스는 설정 문제가 아니라 해당 API를 호출하지 않았기 때문이다(이 점 때문에 중간에 "여전히 안 된다"고 오판했다).

> **참고**: `probability: 1.0`(전량 수집)은 시연·학습 목적이라 택한 값이다. 운영에서는 저장 비용과 오버헤드 때문에 보통 0.1~0.01을 쓰고, Gateway 같은 진입점에서만 결정하면 되므로 하위 서비스 설정은 사실상 불필요하다.

---

## 작업 브랜치 전략

**모든 git 작업은 새 브랜치에서 수행한다.**

- 기준 브랜치: `develop` (`.github/workflows/ci.yml`이 `develop`·`main` 대상 PR에서 동작하므로 팀 컨벤션을 따름)
- 작업 브랜치: `feature/aws-deployment-iac` (`origin/develop` 커밋 `57cc5f6` 기준)
- `git checkout -b X origin/develop`은 upstream을 `origin/develop`으로 설정하므로 `git branch --unset-upstream` 필수. 안 하면 `git push`가 develop으로 나간다
- Phase별로 커밋을 나눈다 — 각 게이트 통과 시점이 자연스러운 커밋 경계다. 게이트 실패 시 직전 커밋으로 되돌릴 수 있다
- 커밋/푸시는 사용자가 요청할 때만 수행하며, 임의로 진행하지 않는다
- Phase 3 착수 전에 `.gitignore`에 `*.tfstate*`, `.terraform/`, `terraform.tfvars`가 반영되어 있는지 반드시 확인한다 (state에 DB 비밀번호가 평문으로 남는다)

> Phase 7에서 `deploy.yml`이 `main` 푸시 트리거이므로, CD 검증은 브랜치를 병합한 뒤에야 완전히 확인 가능하다. 병합 시점은 사용자가 결정한다.

---

## 역할 분담

### 원칙

1. **시크릿은 Claude가 만들지도 입력하지도 않는다.** 액세스 키·비밀번호·API 키는 대화 기록에 남으면 안 된다. Claude는 값이 비어 있는 틀(`.env.example`, `${ENV_VAR}` 참조)까지만 만든다
2. **과금·파괴적 작업은 사용자 승인 후에만 실행한다.** `terraform apply`/`destroy`는 승인 시 Claude가 실행할 수 있으나, 승인 없이는 하지 않는다
3. **AWS 콘솔 작업과 대화형 명령은 사용자 몫이다.** Claude는 비대화형 CLI만 다룬다
4. **커밋·푸시는 사용자가 요청할 때만 한다**

### 작업 주체

| 작업 | 주체 | 비고 |
|---|---|---|
| **AWS 계정 (Phase 0)** | | |
| IAM 사용자 생성, 액세스 키 발급 | **사용자** | 콘솔 작업. 루트 키 사용 금지 |
| `aws configure` | **사용자** | 대화형 + 시크릿 입력. `! aws configure`로 세션에서 실행 가능 |
| 비용 알림(Budgets) 등록 | **사용자** | 콘솔 작업 |
| vCPU 쿼터 증설 신청 | **사용자** | 콘솔 작업. 승인 대기 있음 |
| 도구 설치 (AWS CLI·Terraform) | **사용자** | `winget install --id Amazon.AWSCLI -e` / `--id Hashicorp.Terraform -e`. gh는 Phase 7에서 필요해지면 설치 |
| SSH 키페어 생성 | **사용자** | 개인키를 다루므로 |
| 쿼터·기존 리소스·VPC·ECR 조회 (G0) | Claude | 읽기 전용 |
| **코드·설정 (Phase 1~2)** | | |
| `application.yml` 수정 (B-1·B-2·B-7) | Claude | |
| Gateway 라우트 복원 (B-3) | Claude | |
| 누락 DDL 작성 (B-4) | Claude | 엔티티 기반 |
| `SecurityConfig`·`GlobalFilter` 작성 (B-6·S-1) | Claude | |
| `docker-compose.app.yml`·`docker-compose.data.yml`·`Dockerfile` 작성 | Claude | |
| `.env.example` 작성 (값 없는 틀) | Claude | |
| **`.env`에 실제 시크릿 값 입력** | **사용자** | `JWT_SECRET`, `MASTER_PASSWORD`, `DB_PASSWORD`, `SLACK_WEBHOOK_URL`, Gemini 키 |
| `ai-service` 시연 포함 여부 결정 (B-5) | **사용자** | 팀 협의 필요 |
| 로컬 빌드·기동, 게이트 G1·G1.5 검증 | Claude | 로컬 작업 |
| **인프라 (Phase 3~6)** | | |
| Terraform 코드 작성 | Claude | |
| `terraform init` / `validate` / `fmt` / `plan` | Claude | 과금 없음 |
| **`terraform apply`** | 사용자 승인 후 Claude | **과금 시작 지점** |
| **`terraform destroy`** | 사용자 승인 후 Claude | 파괴적 |
| EC2 부트스트랩, 게이트 G3~G6 검증 | Claude | 자격증명 설정 후 |
| **CD·마무리 (Phase 7~8)** | | |
| `deploy.yml` 작성 | Claude | |
| GitHub 리포지토리 Secrets 설정 | **사용자** | |
| OIDC 신뢰관계 확인 | **사용자** | 콘솔 |
| `README.md` 갱신 | Claude | |
| **git commit / push** | 사용자 요청 시 Claude | 임의로 하지 않음 |
| develop 병합 시점 결정 | **사용자** | 팀 협의 |

### 사용자 결정이 필요한 미결 항목

| 항목 | 내용 | 시점 |
|---|---|---|
| ~~B-5~~ | ~~`ai-service` 범위~~ → **보류로 결정 (2026-08-09)**. compose·ECR에서 제외 | 완료 |
| 팀 전달 사항 | `company`·`product`의 `application.yml`이 `active: prod`라 **IDE 로컬 실행이 깨져 있음**. 배포와 무관하므로 이 작업에서는 수정하지 않으니 담당자에게 공유 필요 | 수시 |
| S-1 담당 | Gateway 인증이 다른 팀원 담당 이슈인지 | Phase 1.5 착수 전 |
| 병합 | `feature/aws-deployment-iac` → `develop` 시점 | Phase 7 전 |

---

## 확정된 아키텍처

서브넷 분리 기준은 **"상태를 가지는가"**다. 상태 저장소(PostgreSQL·Redis·RabbitMQ)는 전부 프라이빗, 무상태 애플리케이션과 사용자 접점(Gateway·Zipkin UI)은 퍼블릭에 둔다.

```
                    Internet
                        │
              ┌─────────▼─────────┐
              │  IGW              │
              └─────────┬─────────┘
   VPC 10.0.0.0/16      │
   ┌───────────────────────────────────────────────┐
   │  Public Subnet  10.0.1.0/24  (ap-northeast-2a)│
   │   ┌────────────────────┐  ┌───────────────┐   │
   │   │ App EC2 m5.large   │  │ NAT Instance  │   │
   │   │ 2vCPU / 8GB        │  │ t3.nano       │   │
   │   │                    │  └───────┬───────┘   │
   │   │ 앱 11 컨테이너      │          │           │
   │   │ + Zipkin (UI)      │          │           │
   │   │ EBS 30GB           │          │           │
   │   └─────────┬──────────┘          │           │
   └─────────────┼─────────────────────┼───────────┘
                 │ 5432, 6379,         │ 0.0.0.0/0
                 │ 5672, 22            │
   ┌─────────────▼─────────────────────▼───────────┐
   │  Private Subnet 10.0.2.0/24                   │
   │   ┌──────────────────────┐                    │
   │   │ Data EC2 t3.small    │                    │
   │   │ postgres:17          │                    │
   │   │ redis:7              │                    │
   │   │ rabbitmq:3-management│                    │
   │   │ EBS 20GB (pgdata)    │                    │
   │   └──────────────────────┘                    │
   └───────────────────────────────────────────────┘

   ECR (11 repos)  ←── GitHub Actions push
```

| 항목 | 결정 |
|---|---|
| IaC | Terraform (로컬 state, `.gitignore`에 `*.tfstate*` 추가) |
| 리전 | ap-northeast-2 (서울), 단일 AZ |
| 앱 서버 | m5.large (2vCPU/8GB, 논버스터블) — 퍼블릭 서브넷 |
| 데이터 서버 | t3.small (2vCPU/2GB) — 프라이빗 서브넷, PG·Redis·RabbitMQ 컨테이너 |
| NAT | NAT Instance t3.nano (NAT Gateway 대비 주 $8.8 절감) |
| 매니지드 서비스 | **사용 안 함** — RDS/EFS/ALB/ElastiCache 모두 불필요 |
| 이미지 | GitHub Actions에서 빌드 → ECR → EC2는 pull만 |
| CD | GitHub Actions (OIDC 인증, 롱리브드 키 미사용) |

### 예상 비용 (1주 = 168시간, 서울)

| 항목 | 1주 |
|---|---:|
| m5.large ($0.118/h) | $19.8 |
| t3.small ($0.026/h) | $4.4 |
| t3.nano ($0.0065/h) | $1.1 |
| EBS gp3 58GB 합계 | $1.2 |
| 퍼블릭 IPv4 2개 ($0.005/h) | $1.7 |
| ECR 스토리지 + 전송 | ~$0.5 |
| **합계** | **약 $28.7 (≈4.0만원)** |

> 단가는 변동하므로 착수 전 AWS Pricing Calculator로 재확인할 것. Redis·RabbitMQ 이전은 비용에 영향이 없다(같은 인스턴스 내 재배치).

### 실측 비용 (2026-08-09 배포 완료 시점)

실제로 생성된 리소스 기준으로 재계산한 결과 **추정치와 거의 일치**했다.

| 항목 | 시간당 | 1주 |
|---|---:|---:|
| m5.large + t3.small + t3.nano | $0.1505 | $25.3 |
| 퍼블릭 IPv4 2개 | $0.010 | $1.7 |
| EBS gp3 58GB (30+20+8) | — | $1.2 |
| ECR 스토리지 (레이어 공유로 실사용 ~1GB) | — | ~$0.1 |
| **합계** | **약 $0.16/h** | **약 $28 (≈3.9만원)** |

**단일 인스턴스 구성과의 비교** — "나누면 비싸진다"는 통념과 반대 결과가 나왔다.

| 구성 | 1주 | 비고 |
|---|---:|---|
| m5.large 1대 (그대로 욱여넣기) | 약 $21.5 | 2 vCPU를 앱·DB가 경쟁, DB가 퍼블릭에 노출 |
| **현재 3대 구성** | **약 $28** | — |
| m5.xlarge 1대 (CPU 경합까지 해소) | 약 $41 | 앱 기준으로 키우면 DB에는 과잉 |

각 서버를 용도에 맞는 크기로 고를 수 있어(앱은 논버스터블 m5, DB는 대부분 idle이라 저렴한 t3), **"충분히 큰 한 대"보다 "적당한 두 대 + NAT"가 더 싸다.** 분리 비용은 단순 단일 구성 대비 주당 $6.5(약 9천원) 수준이며, 그 대가로 DB 격리를 얻는다.

> **계정에 기존 리소스 존재**: 이번 작업과 무관한 stopped 인스턴스 2개(`testec2`, `howscat-server`)가 EBS 16GB를 점유해 월 $1.5 정도가 별도로 발생 중이다. `terraform destroy` 대상이 아니므로 필요 시 수동 정리한다.

### 메모리 예산

**App EC2 (8GB)**

| 구성요소 | 예상 RSS |
|---|---:|
| Spring 서비스 11개 (`-Xmx256m`, 개당 ~440MB) | 4.84 GB |
| Zipkin (JVM, 힙 제한 시) | 0.35 GB |
| Docker 데몬 + containerd | 0.15 GB |
| OS (Amazon Linux 2023) | 0.30 GB |
| **합계 / 여유** | **5.64 GB / 2.36 GB** |

`ai-service` 보류로 여유가 2.36GB로 늘었다. 나중에 포함해도 6.08GB로 8GB 안에 들어간다.

**Data EC2 (2GB)**

| 구성요소 | 예상 RSS |
|---|---:|
| PostgreSQL 17 (커넥션 30개 기준) | 0.46 GB |
| RabbitMQ (Erlang VM) | 0.30 GB |
| Redis 7 | 0.05 GB |
| Docker + OS | 0.45 GB |
| **합계 / 여유** | **1.26 GB / 0.74 GB** |

---

## 사전 조사에서 확인된 배포 차단 요소

Phase 1에서 반드시 해결해야 하는, 코드에 이미 존재하는 문제들이다.

### B-1. Eureka·Zipkin 주소 하드코딩이 서비스마다 방향이 다름 (최우선)

각 서비스 `application.yml`의 Eureka·Zipkin 주소가 환경변수 없이 박혀 있는데, **박힌 값이 서비스마다 갈린다.**

| 설정 | `localhost` | 도커 서비스명 |
|---|---|---|
| `eureka...defaultZone` | 8개 — `gateway`, `user`, `hub`, `hubRoute`, `order`, `inventory`, `delivery`, `slack` | 3개 — `company`, `product`, `ai` |
| `management.zipkin...endpoint` | 7개 — `user`, `hub`, `hubRoute`, `order`, `inventory`, `delivery`, `slack` | 3개 — `company`, `product`, `ai` |
| (설정 자체가 없음) | — | `gateway`, `eureka-server`의 Zipkin endpoint |

`eureka-server:19090`은 도커에선 되지만 IDE 로컬 실행에서 깨지고, `localhost:19090`은 정반대다. **현재 이 리포지토리는 도커로도 IDE로도 전체를 한 번에 띄울 수 없다.**

`application-prod.yml`은 datasource만 오버라이드하므로 이 값들을 건드리지 않는다. 컨테이너에서 `localhost`는 자기 자신이므로 **서비스 디스커버리가 통째로 실패**하고, Gateway의 `lb://user-service` 라우팅도 함께 죽는다.

> **심각도 차이**: Eureka가 실패하면 전체 API가 503이 된다. Zipkin이 실패하면 추적만 안 되고 서비스는 동작한다. 게이트에서 Eureka를 우선 확인하는 이유다.

**해결: 파일을 고치지 않고 환경변수로 덮어쓴다.** Phase 1의 "접근 방식" 참조. `eureka-server/application.yml`의 `defaultZone`은 자기 자신을 가리키는 값이므로 컨테이너 안에서도 `localhost`가 맞다 — **오버라이드 대상이 아니다.**

### B-2. `spring.profiles.active`가 세 갈래로 갈려 있음

"local이 하드코딩"이 아니라 **서비스마다 다르다**는 것이 문제다.

| 값 | 서비스 |
|---|---|
| `local` | `user`, `hub`, `hubRoute`, `order`, `inventory`, `delivery`, `slack` (7개) |
| **`prod`** | **`company`, `product`, `ai` (3개)** |
| 설정 없음 | `gateway-service`, `eureka-server` (2개) |

`company`·`product`는 지금 **IDE에서 그냥 실행하면 prod로 떠서 `${DB_HOST}`가 해결되지 않아 기동에 실패한다.**

**해결: 도커는 `SPRING_PROFILES_ACTIVE=prod` 환경변수로 덮는다. 파일 수정은 하지 않는다.**

yml에 `local`이 적혀 있든 `prod`가 적혀 있든 환경변수가 이기므로 **배포 결과는 동일**하다. `company`·`product`의 IDE 개발이 깨진 것은 배포와 무관한 별개 문제이므로 **담당자에게 전달만 하고 이 작업에서는 건드리지 않는다.**

### B-3. Gateway 라우트가 user-service 하나뿐

`gateway-service/src/main/resources/application.yml:21-35` — 활성 라우트는 `user-service` **하나뿐**이고, 주석 처리된 것은 개별 서비스 라우트가 아니라 **작성 예시 템플릿 한 벌**이다. 즉 "주석을 해제"하는 작업이 아니라 **8개 라우트를 새로 작성**해야 한다.

이 상태로 배포하면 `/api/v1/users/**`, `/api/v1/auth/**` 외 모든 API가 404다.

작성 대상 8개: `hub`, `hubRoute`, `company`, `product`, `order`, `inventory`, `delivery`, `slack`. 각 컨트롤러의 `@RequestMapping` 경로를 확인해 predicate를 맞춰야 한다. 완료 후 총 라우트는 **9개**다.

> `ai-service`에는 Controller가 없다. RabbitMQ `order.created` 이벤트를 받는 **이벤트 기반 서비스**이므로 보류 여부와 무관하게 Gateway 라우트가 필요 없다.

develop에 Security 작업(`dac781f` — Gateway가 넘긴 헤더로 인증 객체 생성)이 들어왔으므로, 라우트 작성 시 헤더 전달 설정을 함께 확인한다.

### B-4. `hub_route_service` 스키마가 SQL 스크립트에 없음

`application-prod.yml`이 요구하는 스키마와 `infra/postgres/*.sql`이 만드는 스키마가 어긋난다.

```
요구(ai 보류 시 9): user, slack, delivery, hub_route, company, order, inventory, hub, product
생성(8):            user, slack, delivery,            company, order, inventory, hub, product
누락(1):                                  hub_route_service
```

`jpa.hibernate.ddl-auto: validate`이므로 **`hubRoute-service`는 prod에서 기동 자체가 실패한다.** 엔티티에 맞는 DDL(`infra/postgres/hubroute.sql`)을 작성해야 한다.

> `ai_service` 스키마도 없지만 **B-5로 보류**했으므로 이번 범위에서는 불필요하다. 나중에 포함할 때 `ai.sql`을 추가하면 된다.

### B-5. `ai-service`가 미완성 → **보류 (범위 제외)**

> **결정: 이번 배포에서 제외한다.**

`ai-service/src/main/resources/application-prod.yml:24`:

```yaml
ai:
  gemini:
    url: http://          # 값이 비어 있음
```

Gemini API 키 시크릿도 정의되지 않았다. 완성도가 부족해 시연 범위에서 뺀다.

**제외에 따른 영향**: 모듈 12 → 11, Eureka 등록 11 → 10, 스키마 10 → 9, ECR 12 → 11. Gateway 라우트는 원래 `ai-service`에 Controller가 없어 **영향 없음(9개)**.

`ai-service` 모듈 자체는 리포지토리에 그대로 두고 `./gradlew build` 대상에도 포함된다. **compose와 ECR에서만 뺀다.** 나중에 포함하려면 `docker-compose.app.yml`에 서비스 하나 추가 + `ai.sql` 작성 + Gemini 설정이면 된다.

### B-8. RabbitMQ 설정이 9개 서비스 yml에 전혀 없음

`MessagingConfig`로 코드에서는 RabbitMQ를 쓰는데 `spring.rabbitmq.*` 설정이 **어느 yml에도 없다.** Spring Boot 기본값 `localhost:5672`로 동작하므로 컨테이너에서 자기 자신을 가리켜 연결에 실패한다.

```
설정 있음: ai-service 만 (spring.rabbitmq.host: ${RABBITMQ_HOST:rabbitmq})
설정 없음: user, hub, hubRoute, company, product, order, inventory, delivery, slack (9개)
```

B-1과 같은 유형이며 해결도 같다. **`SPRING_RABBITMQ_HOST`·`_PORT`·`_USERNAME`·`_PASSWORD` 환경변수로 주입. 파일 수정 불필요.**

### B-6. `user-service`가 prod 프로파일에서 기동 실패 (신규)

`MockGatewayAuthenticationFilter`에 `@Profile("local")`이 붙어 있는데, `SecurityConfig`가 이 빈을 **필수 생성자 주입**으로 받는다.

```java
// user-service/.../global/config/SecurityConfig.java:21
@RequiredArgsConstructor
public class SecurityConfig {
    private final MockGatewayAuthenticationFilter mockGatewayAuthenticationFilter;
```

prod에서는 빈이 등록되지 않으므로 `UnsatisfiedDependencyException`으로 **기동 자체가 실패한다.**

**해결: `@Profile("local")` 한 줄을 삭제한다.** 이 필터가 local 전용이었던 이유는 "Gateway가 아직 없어서 목업으로 대신"이었는데, Phase 1.5에서 Gateway가 실제로 헤더를 주입하기 시작하면 **prod에서도 필요한 진짜 필터가 된다.** 우회책(prod용 SecurityConfig 분리, 선택적 주입)을 만들 필요 없이 원래 설계 의도대로 돌아간다.

> 클래스명의 `Mock`은 더 이상 정확하지 않지만, 이름 변경은 다른 팀원 작업과 충돌할 수 있으므로 **주석만 갱신하고 클래스명은 유지**한다.

### B-7. `jwt.secret`·`app.master.*`가 prod에 없음 (신규)

두 설정 모두 `application-local.yml`에만 정의돼 있고 `application-prod.yml`에는 없다.

```yaml
# user-service/src/main/resources/application-local.yml:22-29 (local 전용)
jwt:
  secret: ${JWT_SECRET:local-jwt-secret-key-must-be-at-least-32-bytes-long}
app:
  master:
    username: ${MASTER_USERNAME}
    password: ${MASTER_PASSWORD}
    slack-id: ${MASTER_SLACK_ID}
```

prod로 띄우면 **로그인 불가 + 최초 MASTER 계정 생성 불가**다.

**해결: 전부 환경변수로. 파일 수정 없음.**

| 프로퍼티 | 환경변수 |
|---|---|
| `jwt.secret` | `JWT_SECRET` |
| `app.master.username` | `APP_MASTER_USERNAME` |
| `app.master.password` | `APP_MASTER_PASSWORD` |
| `app.master.slack-id` | **`APP_MASTER_SLACKID`** — 대시가 제거된다. `APP_MASTER_SLACK_ID`로 착각하기 쉬움 |

케밥케이스 변환이 불확실하므로 **G1-11(MASTER 계정 생성 확인)에서 실측 검증**한다. 실패하면 그때 `application-prod.yml`에 `${MASTER_SLACK_ID}` 플레이스홀더를 추가한다 — 먼저 고치지 않는다.

---

## S-1. Gateway 인증 미구현 → **Phase 1.5에서 해결**

> **상태: 배포 범위 포함 확정.** 실행 계획은 Phase 1.5 참조.

`gateway-service`의 소스는 `GatewayApplication.java` **단 하나**다. JWT 검증도 헤더 주입도 없다. 그런데 `user`·`company`·`product` 서비스는 "Gateway가 준 헤더"(`X-User-Id`, `X-User-Role`, `X-Hub-Id`, `X-Company-Id`)를 무조건 신뢰한다.

Gateway가 외부에서 들어온 헤더를 제거하지도 검증하지도 않으므로:

```bash
curl -H 'X-User-Id: 1' -H 'X-User-Role: MASTER' http://<서버>:19091/api/v1/...
```

**이 한 줄로 누구나 MASTER가 된다.** 로그인도 토큰도 불필요한 완전한 인증 우회다.

코드 주석에 "Gateway가 아직 구현되지 않은 동안"(`MockGatewayAuthenticationFilter:26`), "실제 JWT 인증은 이후 로그인 및 Gateway 인증 이슈에서 추가"(`SecurityConfig:15`)라고 명시돼 있어 팀에서 인지 중인 미완성 지점으로 보인다.

**팀이 이미 이 구조를 전제로 코드를 작성해 두었다.** `JwtProvider`의 주석에 명시돼 있다:

```java
// user-service/.../JwtProvider.java:16, 44
* API Gateway에서는 JWT 서명 및 만료 검증
* Gateway는 이 Claim을 읽어 내부 사용자 헤더를 만든다.
```

즉 새로 설계하는 것이 아니라 **비어 있는 자리를 채우는 작업**이다. 실행 계획은 Phase 1.5에 있다.

**Phase 1.5 완료 전까지의 완화책** (Phase 3에 이미 반영됨):
- `app-sg`의 19091을 **본인 IP로만** 개방
- 공개 URL을 공유하지 않는다

---

### 부수 항목

- `gateway-service`·`eureka-server`에는 `application-prod.yml` 자체가 없음
- **인가가 구현된 서비스는 10개 중 3개뿐** — `user`, `company`, `product`. 나머지 7개(`hub`, `hubRoute`, `order`, `inventory`, `delivery`, `slack`, `ai`)는 `SecurityConfig` 자체가 없음
- **권한 문자열 규칙이 서비스마다 다름** — `user`는 `"ROLE_" + role.name()`(`hasRole()` 전제), `company`·`product`는 접두사 없음(`hasAuthority()` 전제). 같은 헤더로도 서비스마다 판정이 달라짐
- 각 `Dockerfile`이 `COPY . .` + `./gradlew :서비스:bootJar` 구조 → 각 이미지가 전체 Gradle 빌드를 수행. EC2에서 빌드 불가, CI 빌드로 전환 필요
- **HikariCP 기본 풀 10 × DB 사용 서비스 9개 = 90 커넥션 요구**, PostgreSQL 기본 `max_connections`는 100 → 한도에 근접. `SPRING_DATASOURCE_HIKARI_MAXIMUMPOOLSIZE=3`으로 27까지 낮춘다
- `@FeignClient(name = "hub-service")`와 `"HUB-SERVICE"`가 혼용됨 (`hub`·`hubRoute`가 대문자). 보통 동작하지만 일관성 없음
- `infra/postgres/init.sql`이 `company_service.p_company_service`를 만드는데 `company-product.sql`은 `p_company`를 만든다. init.sql이 오래된 중복본일 가능성
- `hubRoute-service`의 Redis 캐시 TTL이 **30초**(테스트용). 1시간짜리 설정은 주석 처리 상태 — `RedisConfig.java:22-23`

---

## Phase 0 — 계정·도구 준비

> **배포 대상: 개인 AWS 계정(기존 보유).** 기존 리소스가 있으므로 **한도는 합산**되고 **이름은 충돌**할 수 있다. 신규 계정과 달리 "현재 무엇을 쓰고 있는가"를 먼저 파악해야 한다.

### 현재 로컬 환경 (2026-08-09 확인)

| 도구 | 상태 |
|---|---|
| Docker | ✅ `AppData\Local\Programs\DockerDesktop\resources\bin\docker.exe` |
| Java 21 | ✅ `.jdks\ms-21.0.12` |
| AWS CLI | ❌ 미설치 |
| Terraform | ❌ 미설치 |
| GitHub CLI (`gh`) | ❌ 미설치 (Phase 7 게이트에서 사용) |
| `~/.aws` 자격증명 | ❌ 없음 |

Phase 1·1.5는 Docker와 Java만 필요하므로 AWS 준비와 **병렬 진행 가능**하다.

### TODO

1. **vCPU 쿼터 확인을 최우선으로** — 증설 승인에 수 시간~며칠이 걸리므로 리드타임이 있다
   - 필요 vCPU: m5.large(2) + t3.small(2) + t3.nano(2) = **6**
   - **기존 계정이므로 현재 실행 중인 인스턴스의 vCPU와 합산**해서 판단
   - 부족하면 Service Quotas에서 즉시 증설 신청하고, 대기 중 Phase 1을 진행
2. 도구 설치 — AWS CLI v2, Terraform, `gh`
3. IAM 사용자 확인 또는 생성 — **루트 계정 액세스 키는 사용하지 않는다.** 액세스 키를 로컬에 두게 되므로
4. `aws configure` — 리전 `ap-northeast-2`
5. **기존 리소스 파악** (기존 계정 전용 항목)
   - 실행 중인 EC2와 vCPU 합계
   - VPC 개수 — **리전당 기본 한도 5개**. 이미 5개면 새 VPC를 못 만든다
   - 기존 ECR 리포지토리 이름 — 11개를 새로 만들 때 충돌 확인
6. **비용 알림을 착수 전에 설정** — AWS Budgets $50 임계값. destroy를 잊었을 때의 안전장치는 리소스를 만들기 *전에* 있어야 의미가 있다
7. 리소스 네이밍 prefix 결정 (`logistics-`) — 기존 리소스와 구분되도록 Terraform 전체에 일관 적용
8. SSH 키페어 생성 (`ssh-keygen`) — 공개키를 Phase 3의 `aws_key_pair`에 등록

### 🚦 게이트 G0 — Phase 3 진입의 선행 조건

> Phase 1·1.5·2(로컬 부분)는 G0 없이 진행할 수 있다. **G0는 Phase 3(AWS 과금 시작) 진입 전까지 통과하면 된다.**

```bash
# 1. 자격증명 — Account/Arn 출력, Arn이 :root가 아닐 것
aws sts get-caller-identity

# 2. 리전 — 기대: ap-northeast-2
aws configure get region

# 3. Terraform 설치 확인
terraform version

# 4. vCPU 쿼터 조회 (L-1216C47A = Running On-Demand Standard instances)
aws service-quotas get-service-quota \
  --service-code ec2 --quota-code L-1216C47A \
  --region ap-northeast-2 --query 'Quota.Value'

# 5. 현재 사용 중인 인스턴스 — 4번 쿼터에서 이만큼 빼고 6 이상 남아야 한다
aws ec2 describe-instances --filters Name=instance-state-name,Values=running \
  --query 'Reservations[].Instances[].[InstanceId,InstanceType]' --output table

# 6. VPC 개수 — 기대: 4 이하 (기본 한도 5, 새로 1개 필요)
aws ec2 describe-vpcs --query 'length(Vpcs)'

# 7. 비용 알림 등록 확인 — 빈 출력이면 실패
aws budgets describe-budgets --account-id $(aws sts get-caller-identity --query Account --output text) \
  --query 'Budgets[].BudgetName'

# 8. ECR 이름 충돌 확인 — logistics/* 가 이미 있으면 prefix 조정
aws ecr describe-repositories --query 'repositories[].repositoryName'
```

**4·5번이 핵심이다.** `쿼터 값 - 현재 사용 중 vCPU >= 6`이 성립하지 않으면 Phase 3의 `terraform apply`가 세 번째 인스턴스에서 실패한다.

### G0 실행 결과 (2026-08-09)

| 검증 | 결과 | 판정 |
|---|---|---|
| 자격증명 | IAM `user/Admin` (루트 아님) | ✅ |
| 리전 | `ap-northeast-2` | ✅ |
| Terraform | v1.15.8 | ✅ |
| AWS CLI | v2.36.17 | ✅ |
| **vCPU 쿼터** | **32** (필요 6, 실행 중 0) | ✅ 여유 26 — **증설 신청 불필요** |
| VPC 개수 | 1 (한도 5) | ✅ 여유 4 |
| ECR 리포지토리 | 없음 | ✅ `logistics/*` 이름 충돌 없음 |
| 비용 알림 | 미등록 | ❌ **미충족 — Phase 3 전까지 등록 필요** |

기존 계정이라 vCPU 쿼터가 기본값 5가 아닌 32로 상향돼 있었다. 리드타임 리스크가 해소되어 Phase 3을 대기 없이 진행할 수 있다.

> **주의**: 이 세션의 셸은 도구 설치 이전에 시작되어 PATH가 낡았다. PowerShell 호출마다 다음을 먼저 실행해야 `aws`·`terraform`이 인식된다. 새 터미널을 열면 불필요하다.
> ```powershell
> $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
> ```

> **프리티어 오해 주의**: `m5.large`·`t3.small`은 프리티어 대상이 **아니다.** 프리티어는 `t2.micro`/`t3.micro` 750시간뿐이므로 이 구성은 전액 유료다. 비용표($28.7/주)는 이미 이 기준이다.

---

## Phase 1 — 로컬에서 prod 프로파일 전체 기동

> **이 계획의 핵심 게이트다.** AWS 리소스를 단 하나도 만들기 전에, 컨테이너 11개가 prod 프로파일로 서로를 찾아 정상 동작하는 것을 로컬에서 증명한다. 클라우드에서 애플리케이션 설정 문제를 디버깅하면 시간과 비용이 몇 배로 든다.

### 접근 방식 — 기존 파일을 최소한만 건드린다

**Spring Boot는 OS 환경변수가 `application.yml`·`application-{profile}.yml`보다 우선순위가 높다.** 따라서 B-1(Eureka·Zipkin 주소)과 B-2(프로파일)는 **YAML을 한 줄도 고치지 않고** compose의 환경변수만으로 해결한다.

| YAML 프로퍼티 | 대응 환경변수 |
|---|---|
| `eureka.client.service-url.defaultZone` | `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` |
| `management.zipkin.tracing.endpoint` | `MANAGEMENT_ZIPKIN_TRACING_ENDPOINT` |
| `spring.profiles.active` | `SPRING_PROFILES_ACTIVE` |
| `jwt.secret` | `JWT_SECRET` |
| `spring.datasource.hikari.maximum-pool-size` | `SPRING_DATASOURCE_HIKARI_MAXIMUMPOOLSIZE` |

**이유는 팀 프로젝트이기 때문이다.** 여러 팀원이 각자 브랜치에서 작업 중이므로, 11개 서비스의 `application.yml`을 건드리면 병합할 때마다 충돌이 난다. 인프라 작업이 남의 작업을 방해하지 않아야 한다.

단점은 값의 소재가 yml에서 안 보인다는 것이다. `docker-compose.app.yml`에 주석을 달고 README에 정리해 보완한다.

> **케밥케이스 주의**: `app.master.slack-id`의 환경변수 형태는 대시가 제거된 `APP_MASTER_SLACKID`다. `APP_MASTER_SLACK_ID`로 착각하기 쉬우므로, 이런 항목은 yml에 `${MASTER_SLACK_ID}` 플레이스홀더로 명시해 둔다.

### 원칙 — 각 서비스 내부 코드 수정은 최소화한다

**배포에 필요하지 않은 수정은 하지 않는다.** 발견한 문제라도 배포와 무관하면 담당자에게 알리고 넘어간다. 남의 브랜치와 충돌할 파일을 취향으로 건드리지 않는다.

이 기준으로 걸러낸 항목:

| 후보 | 판정 | 사유 |
|---|---|---|
| `company`·`product`의 `active: prod` → `local` | ❌ 제외 | 도커는 `SPRING_PROFILES_ACTIVE=prod`로 덮으므로 **배포 결과가 동일**하다. IDE 개발이 깨진 건 별개 문제이므로 **담당자에게 전달만** 한다 |
| `hubRoute-service` Redis TTL 30초 → 1시간 | ❌ 제외 | 30초여도 캐시는 정상 동작한다. 오히려 검증이 쉽다 |
| `user-service/application-prod.yml`에 `app.master.*` 추가 | ⚠️ 보류 | 환경변수로 먼저 시도하고, G1에서 실패하면 그때 수정 |

### TODO

**서비스 내부 코드 수정 (1개, 1줄)**

1. **`MockGatewayAuthenticationFilter.java:37`** (B-6) — `@Profile("local")` 삭제 + 주석 갱신
   - **이것만은 대안이 없다.** 삭제하지 않으면 `SecurityConfig`가 빈을 찾지 못해 `user-service`가 prod에서 기동 실패한다
   - 이 시점에는 아직 Gateway가 헤더를 주입하지 않아 보호 경로가 전부 401이 되는데, **이는 정상이며 G1은 401을 통과로 인정한다**(404/503만 실패). 실제 인증은 Phase 1.5에서 완성된다
   - Phase 1.5를 하면 **어차피 지워야 하는 줄**이다. Gateway가 헤더를 주입하면 이 필터는 prod에서 필요한 진짜 필터가 된다

   > **검토한 우회안과 기각 사유**
   > - `SPRING_PROFILES_ACTIVE=local,prod` — 두 프로파일 동시 활성화. 파일 수정 0개이고 `jwt.secret`·`app.master.*`도 덤으로 해결된다
   > - `SPRING_PROFILES_ACTIVE=local` + datasource만 환경변수 오버라이드
   >
   > 둘 다 동작하지만 **프로파일 로드 순서에 의존하고, `application-local.yml`의 다른 설정(`show-sql: true` 등)이 prod에 섞인다.** 나중에 원인 추적이 어렵고 "왜 prod 배포에 local 프로파일이 켜져 있나"에 답하기 곤란하다. 한 줄 삭제가 더 안전하다.

**루트 파일 (1개)**

2. **`.env.example`** — 환경변수 목록 보강 (서비스 코드 아님)

**파일 신규 (2개 — 실제 작업량의 대부분)**

6. **`docker-compose.app.yml`** — 앱 11개 + Zipkin. App EC2용이자 로컬 검증용
   - 공통 블록(YAML 앵커)으로 환경변수를 한 번만 정의하고 11개 서비스가 참조
   - `SPRING_PROFILES_ACTIVE=prod`
   - `JAVA_TOOL_OPTIONS=-Xmx256m -XX:MaxMetaspaceSize=128m`, Zipkin에 `JAVA_OPTS=-Xmx256m`
   - `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE`, `MANAGEMENT_ZIPKIN_TRACING_ENDPOINT`
   - `SPRING_DATASOURCE_HIKARI_MAXIMUMPOOLSIZE=3` (기본 10 × 9서비스 = 90 → 27)
   - `DB_HOST`, `DB_USERNAME`, `DB_PASSWORD`, `REDIS_HOST`, `RABBITMQ_HOST`, `SLACK_WEBHOOK_URL`, `JWT_SECRET`, `MASTER_*`를 `.env`에서 주입
   - `depends_on` + `healthcheck`로 순차 기동: eureka → gateway → 나머지 9개
   - 각 서비스 `mem_limit: 600m` (초과 시 조기 발견)
7. **`docker-compose.data.yml`** — PostgreSQL + Redis + RabbitMQ. Data EC2용

**기존 `docker-compose.yml`은 손대지 않는다.** 팀원들의 현재 워크플로(미들웨어만 도커, 앱은 IDE)가 그대로 유지된다.

**판단이 필요한 작업 (별도)**

8. **B-3** — `gateway-service` 라우트 **9개** 주석 해제, 각 컨트롤러 `@RequestMapping` 경로와 대조
9. **B-4** — `infra/postgres/hubroute.sql` 작성. `ai-service` 보류로 `ai.sql`은 불필요
10. `infra/postgres/init.sql`의 `p_company_service` 중복 여부 확인 후 정리
11. `hubRoute-service` Redis TTL 조정 (30초 → 1시간, `RedisConfig.java:22-23`)

### 실행 방법

```bash
# 팀원 로컬 개발 — 지금과 동일, 아무것도 바뀌지 않음
docker compose up

# Phase 1 전체 검증 — 배포와 동일한 두 파일을 병합 (데이터 3 + 앱 11 + Zipkin = 15개)
docker compose -f docker-compose.data.yml -f docker-compose.app.yml up

# Data EC2
docker compose -f docker-compose.data.yml up -d      # 3개

# App EC2
docker compose -f docker-compose.app.yml up -d       # 12개
```

**로컬 검증에 기존 `docker-compose.yml`을 쓰지 않는 이유**: 처음에는 `docker-compose.yml`(미들웨어 4개) + `app.yml` 조합을 계획했으나, Zipkin이 양쪽에 정의되면 `ports` 리스트가 병합되며 중복 바인딩이 발생할 수 있다. `data.yml` + `app.yml`로 검증하면 **배포에 쓰는 파일과 완전히 동일한 조합**을 검증하게 되어 "로컬에서만 되는 구성"이 생기지 않는다. 기존 `docker-compose.yml`은 팀원 워크플로용으로 그대로 남긴다.

### 검증 기준

- 15개 컨테이너 전부 `Up` 상태, 재시작 루프 없음
- Eureka에 **10개** 등록 (비즈니스 9 + gateway, eureka-server 자신 제외)
- Gateway 경유로 **9개** 비즈니스 서비스 전부 응답
- DB 커넥션 총합 40 미만
- 컨테이너 총 메모리 6GB 미만

### 🚦 게이트 G1 — 전부 통과해야 Phase 1.5 진입

```bash
C="-f docker-compose.yml -f docker-compose.app.yml"

# 1. 컨테이너 상태 — restarting/exited 0개
docker compose $C ps --format '{{.Name}} {{.State}}'

# 2. Eureka 등록 서비스 수 — 기대값: 10
curl -s -H 'Accept: application/json' http://localhost:19090/eureka/apps \
  | grep -o '"name"' | wc -l

# 3. Gateway 라우트 수 — 기대값: 9
curl -s http://localhost:19091/actuator/gateway/routes | grep -o '"route_id"' | wc -l

# 4. Gateway 경유 실제 호출 — 각 서비스 200 또는 401 (404/503이면 실패)
#    라우트 경로별로 반복 확인

# 5. DB 커넥션 수 — 기대값: 40 미만
docker exec logistics-postgres psql -U "$POSTGRES_USER" -d logistics \
  -tAc "select count(*) from pg_stat_activity where datname='logistics'"

# 6. 메모리 총합 — 기대값: 6000 MiB 미만
docker stats --no-stream --format '{{.MemUsage}}'

# 7. Redis 캐시 실동작 — hubRoute 조회 2회 후 키 존재 확인
docker exec logistics-redis redis-cli KEYS 'hubRouteCache*'

# 8. user-service가 prod 프로파일로 실제로 떴는가 (B-6 검증)
#    컨테이너가 Up이어도 기동 실패 후 재시작 중일 수 있으므로 로그를 직접 확인
docker compose $C logs user-service | grep -i 'UnsatisfiedDependency\|Started UserServiceApplication'

# 9. 환경변수 오버라이드가 실제로 먹었는가 (이 접근 방식의 핵심 검증)
#    yml에는 localhost가 그대로 있으므로, 실제 사용 값을 확인해야 한다
docker compose $C exec user-service env | grep -E 'EUREKA_CLIENT_SERVICEURL|SPRING_PROFILES_ACTIVE'

# 10. 로그인 동작 (B-7 검증) — 토큰이 실제로 발급되는가
curl -s -X POST http://localhost:19091/api/v1/auth/login \
  -H 'Content-Type: application/json' -d '{"username":"...","password":"..."}'

# 11. 최초 MASTER 계정 생성 확인 (B-7 검증) — 기대: 1
docker exec logistics-postgres psql -U "$POSTGRES_USER" -d logistics \
  -tAc "select count(*) from user_service.p_user where role='MASTER'"
```

**9번이 이 접근 방식 특유의 검증이다.** yml에는 여전히 `localhost`가 적혀 있으므로, 컨테이너가 실제로 어떤 값을 쓰는지 눈으로 확인하지 않으면 "고쳤다고 착각"할 수 있다.

**게이트 실패 시**: Phase 1.5로 넘어가지 않는다. AWS에서 고치려 들지 말 것.

---

## Phase 1.5 — Gateway JWT 인증 구현 (S-1)

> Phase 1과 분리하는 이유는 **검증 성격이 다르기 때문**이다. G1은 "11개가 뜨고 서로를 찾는가"(기동), G1.5는 "인증이 실제로 막는가"(보안)를 증명한다. G1이 통과해야 G1.5를 시도할 수 있고, G1.5가 실패해도 G1 결과는 유효하다.

### 확인된 제약

| 항목 | 값 | 영향 |
|---|---|---|
| Gateway 스택 | `spring-cloud-starter-gateway-server-webflux` | **WebFlux**. 서블릿 `Filter`가 아니라 `GlobalFilter`를 써야 함 |
| JWT 라이브러리 | `jjwt 0.12.6` (user-service) | Gateway에도 동일 버전 추가 |
| 서명 방식 | `Keys.hmacShaKeyFor` = HMAC 대칭키 | **같은 `JWT_SECRET`을 두 서비스가 공유**. 공개키 배포 불필요 |
| Claim 구조 | `sub`(userId), `username`, `role`, `hubId`, `companyId`, `tokenType` | 그대로 헤더에 매핑 가능 |

### TODO

1. `gateway-service/build.gradle`에 jjwt 3줄 추가
   ```gradle
   implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
   runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'
   runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'
   ```
   `spring-boot-starter-security`는 **넣지 않는다.** `GlobalFilter` 하나로 충분하고, WebFlux Security를 붙이면 설정이 두 배가 된다
2. `JwtProperties` 신규 — `jwt.secret` 바인딩
3. **`JwtAuthenticationGlobalFilter` 신규** — `GlobalFilter` + `Ordered`(최우선). 처리 순서:
   ```
   1. 들어온 X-User-Id / X-User-Role / X-Hub-Id / X-Company-Id 헤더를
      무조건 제거            ← ★ 화이트리스트 여부와 무관하게 가장 먼저
   2. 화이트리스트 경로면 그대로 통과
      (/api/v1/auth/login, /api/v1/auth/signup, /actuator/health)
   3. Authorization: Bearer <token> 파싱 → 없으면 401
   4. 서명·만료 검증 (jjwt) → 실패하면 401
   5. tokenType == "ACCESS" 확인 → REFRESH 토큰으로 API 호출 차단
   6. claim을 X-User-* 헤더로 주입해서 다음 필터로 전달
   ```
   **1번이 이 작업의 전부다.** 3~6번만 구현하고 1번을 빠뜨리면, 공격자가 `Authorization` 없이 `X-User-Role: MASTER`만 보낼 때 2번에서 걸러지지 않고 그대로 통과한다 — 지금과 똑같이 뚫린다
4. `gateway-service/application.yml`에 `jwt.secret: ${JWT_SECRET}`, 화이트리스트 경로 설정
5. `gateway-service/application-prod.yml` 신규 (Phase 1 TODO 7번과 통합)
6. **`user-service`의 `MockGatewayAuthenticationFilter`에서 `@Profile("local")` 삭제** — B-6이 여기서 해결된다. 주석을 "Gateway가 주입한 헤더를 읽어 Authentication을 생성한다"로 갱신, 클래스명은 유지

### 🚦 게이트 G1.5

```bash
# 1. 토큰 없이 보호 경로 → 기대: 401
curl -s -o /dev/null -w '%{http_code}' http://localhost:19091/api/v1/users/me

# 2. ★ 헤더만 위조 → 기대: 401  (S-1의 핵심 회귀 테스트)
curl -s -o /dev/null -w '%{http_code}' \
  -H 'X-User-Id: 1' -H 'X-User-Role: MASTER' \
  http://localhost:19091/api/v1/users/me

# 3. 정상 로그인 → 토큰 → 기대: 200
TOKEN=$(curl -s -X POST http://localhost:19091/api/v1/auth/login \
  -H 'Content-Type: application/json' -d '{"username":"...","password":"..."}' | jq -r .accessToken)
curl -s -o /dev/null -w '%{http_code}' -H "Authorization: Bearer $TOKEN" \
  http://localhost:19091/api/v1/users/me

# 4. 변조된 토큰 → 기대: 401
curl -s -o /dev/null -w '%{http_code}' -H "Authorization: Bearer ${TOKEN}x" \
  http://localhost:19091/api/v1/users/me

# 5. REFRESH 토큰으로 API 호출 → 기대: 401

# 6. 화이트리스트는 토큰 없이 접근 → 기대: 200/400 (401이 아닐 것)
curl -s -o /dev/null -w '%{http_code}' -X POST http://localhost:19091/api/v1/auth/login -d '{}'

# 7. G1 전 항목 재통과 (필터 추가가 기동을 깨지 않았는지)
```

**2번이 200이면 나머지가 모두 통과해도 실패로 처리한다.** 이 작업의 목적 자체가 2번이다.

### 이 작업으로도 남는 한계 (문서화 대상)

**1. Gateway 우회 방지는 네트워크에 의존한다.**
서비스들은 여전히 헤더를 무조건 신뢰한다. 누군가 `19092`(user-service)에 직접 접근하면 헤더 위조가 그대로 통한다. 이번 배포는 SG에서 19091만 열고 나머지는 docker 내부 네트워크에만 두므로 안전하지만, **"서비스가 검증해서" 안전한 것이 아니라 "네트워크가 막아서" 안전한 것**이다.

**2. 이번 범위는 "인증"까지다.**
"이 사람이 누구인가"는 해결되지만 "이 API를 호출해도 되는가"(role별 접근 제어)는 여전히 `user`·`company`·`product` 3개 서비스에만 있다. 나머지 7개는 로그인만 하면 무엇이든 호출할 수 있다. 인가까지 넣으려면 7개 서비스에 `SecurityConfig` + `@PreAuthorize`를 추가하는 별도 작업이 필요하다.

---

## Phase 2 — 이미지 빌드 파이프라인 및 ECR 푸시

### TODO

1. **Dockerfile 재작성** — 배포 대상 11개. 이미지 내부 Gradle 빌드를 제거하고 CI에서 만든 jar를 복사하는 구조로 변경
   ```dockerfile
   FROM eclipse-temurin:21-jre
   WORKDIR /app
   COPY build/libs/*.jar app.jar
   ENTRYPOINT ["java", "-jar", "app.jar"]
   ```
   이유: 현재 구조는 각 이미지가 전체 소스를 복사해 Gradle 빌드를 수행 → 의존성을 11번 내려받아 EC2에서 40분 이상 소요, CPU 크레딧 고갈
2. `./gradlew build` **한 번**으로 jar 생성 → 각 Dockerfile은 자기 모듈 jar만 복사. 빌드는 `ai-service`를 포함해 12개가 생성되지만 **이미지는 11개만 만든다**
3. 로컬에서 11개 이미지 빌드 후 compose가 이미지를 참조하도록 전환 (`build:` → `image:`)
4. **G1·G1.5를 이미지 기반으로 재실행** — 빌드 방식 변경이 동작을 깨지 않았는지 확인
5. Terraform으로 ECR 리포지토리 **11개** 생성 (`for_each`, 이미지 3개 유지 lifecycle policy)
6. 수동으로 1회 푸시하여 ECR 연동 확인 (자동화는 Phase 7)

### 🚦 게이트 G2

```bash
# 1. jar — 기대: 12 (ai-service 포함. 빌드는 전체, 이미지만 11개)
find . -path '*/build/libs/*.jar' -not -name '*-plain.jar' | wc -l

# 2. 이미지 11개, 각 460MB 이하 (alpine 베이스 실측 기준. 최초 목표였던 350MB는
#    eclipse-temurin:21-jre-alpine 자체가 이미 300MB 안팎이라 비현실적이라고 판단해 수정함)
docker images --format '{{.Repository}} {{.Size}}' | grep logistics

# 3. 이미지 기반으로 G1·G1.5 전 항목 재통과 (필수)

# 4. ECR 리포지토리 11개, 각 이미지 1개 이상
aws ecr describe-repositories --query 'length(repositories)'   # 기대: 11
```

---

## Phase 3 — Terraform 네트워크 계층

> 여기서부터 AWS 과금이 시작된다. 착수 시각을 기록하고 destroy 예정일을 캘린더에 등록할 것.

### TODO

1. Terraform 프로젝트 구조 생성
   ```
   infra/terraform/
     ├── main.tf  providers.tf  variables.tf  outputs.tf  terraform.tfvars
     ├── network.tf   # VPC, 서브넷, IGW, 라우팅, SG
     ├── compute.tf   # EC2 3대, IAM, 키페어  (Phase 4)
     └── ecr.tf       # (Phase 2에서 작성)
   ```
2. `.gitignore`에 `*.tfstate*`, `.terraform/`, `terraform.tfvars` 추가 — **state에 DB 비밀번호가 평문으로 남는다**
3. VPC `10.0.0.0/16`, 퍼블릭 `10.0.1.0/24`, 프라이빗 `10.0.2.0/24` (단일 AZ)
4. IGW + 퍼블릭 라우팅 테이블 (`0.0.0.0/0` → IGW)
5. **NAT Instance** — t3.nano, Amazon Linux 2023
   - `source_dest_check = false` (누락 시 NAT가 조용히 동작 안 함)
   - user-data: `net.ipv4.ip_forward=1` + `iptables -t nat -A POSTROUTING -o ens5 -j MASQUERADE`
   - 대안: `fck-nat` AMI 사용 시 user-data 불필요 (권장)
6. 프라이빗 라우팅 테이블 (`0.0.0.0/0` → NAT Instance의 ENI)
7. 보안 그룹 3개
   | SG | 인바운드 포트 | 소스 |
   |---|---|---|
   | `app-sg` | 22, 19091 | 내 IP만 (`var.my_ip`) |
   | | 9411 (Zipkin UI) | 내 IP만 |
   | `data-sg` | 5432, 6379, 5672, 22 | `app-sg`만 |
   | | 15672 (RabbitMQ UI) | `app-sg`만 (SSH 터널로 접근) |
   | `nat-sg` | all | `10.0.0.0/16` |
8. SSH 키페어 등록 (`aws_key_pair`, 공개키는 로컬에서 생성)

### 🚦 게이트 G3

```bash
# 1. plan이 깨끗한가 (apply 직후 실행 시 "No changes")
terraform plan -detailed-exitcode      # 기대: exit code 0

# 2. 0.0.0.0/0 인바운드 없음 — 기대: 빈 출력
aws ec2 describe-security-groups --filters Name=vpc-id,Values=$(terraform output -raw vpc_id) \
  --query "SecurityGroups[].IpPermissions[?contains(IpRanges[].CidrIp, '0.0.0.0/0')]" --output text

# 3. NAT source/dest check 비활성 — 기대: False
aws ec2 describe-instances --instance-ids $(terraform output -raw nat_instance_id) \
  --query 'Reservations[].Instances[].SourceDestCheck' --output text

# 4. 프라이빗 라우팅이 NAT ENI를 향하는지 확인
terraform output private_route_table_id
```

> NAT 실동작 검증은 프라이빗 서브넷에 인스턴스가 생긴 뒤(G4)에 가능하다.

---

## Phase 4 — Terraform 컴퓨트 계층

### TODO

1. **Data EC2** — t3.small, 프라이빗 서브넷, EBS gp3 20GB, 퍼블릭 IP 없음
2. **App EC2** — m5.large, 퍼블릭 서브넷, EBS gp3 30GB, 퍼블릭 IP 할당
3. IAM 역할 + 인스턴스 프로파일 — App EC2에 `AmazonEC2ContainerRegistryReadOnly` (ECR pull용)
4. 양쪽 user-data 공통: Docker + docker compose plugin 설치, **swap 2GB 생성** (`/swapfile`, `fstab` 등록)
5. `outputs.tf` — `app_public_ip`, `data_private_ip`, `nat_public_ip`

### 🚦 게이트 G4

```bash
APP=$(terraform output -raw app_public_ip)
DATA=$(terraform output -raw data_private_ip)

# 1. App EC2 접속
ssh -i ~/.ssh/logistics.pem ec2-user@$APP 'echo OK'

# 2. 배스천 경유 Data EC2 접속
ssh -i ~/.ssh/logistics.pem -J ec2-user@$APP ec2-user@$DATA 'echo OK'

# 3. NAT 실동작 — 프라이빗 인스턴스가 인터넷에 나가는가 (가장 중요)
ssh -i ~/.ssh/logistics.pem -J ec2-user@$APP ec2-user@$DATA \
  'curl -s -o /dev/null -w "%{http_code}" https://registry-1.docker.io/v2/'   # 기대: 401 (연결 성공을 의미)

# 4. Docker + swap
ssh -i ~/.ssh/logistics.pem ec2-user@$APP 'docker compose version && free -m | grep Swap'

# 5. App EC2의 ECR pull 권한
ssh -i ~/.ssh/logistics.pem ec2-user@$APP \
  'aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin <ACCOUNT>.dkr.ecr.ap-northeast-2.amazonaws.com'
```

**3번이 실패하면** `source_dest_check`, iptables MASQUERADE 규칙, 프라이빗 라우팅 테이블 순으로 점검한다. 여기서 막힌 채 진행하면 Phase 5에서 원인 불명으로 헤맨다.

---

## Phase 5 — Data EC2 부트스트랩

### TODO

1. Data EC2에 `docker-compose.data.yml` 배치 — `postgres:17`, `redis:7`, `rabbitmq:3-management`
2. Postgres 볼륨을 EBS 경로(`/data/pgdata`)에 바인드
3. `max_connections=100` 확인 (HikariCP 3 × 10서비스 = 30이므로 여유)
4. `infra/postgres/*.sql`을 `docker-entrypoint-initdb.d`로 주입. **B-4에서 추가한 `hubroute.sql`·`ai.sql` 포함 확인**. 각 파일이 자체 스키마를 생성하므로 알파벳 순서 의존성은 없음
5. 스키마 9개, 테이블 전체 생성 확인
6. App EC2에서 Data EC2로 5432·6379·5672 연결 확인
7. **컨테이너 재시작 후 Postgres 데이터 영속 확인** (Redis는 캐시라 유실 무방)

### 🚦 게이트 G5

```bash
# 1. 스키마 9개 — 기대: 9 (B-4 미해결 시 8이 나오며, 여기서 막아야 한다)
ssh -J ec2-user@$APP ec2-user@$DATA \
  "docker exec pg psql -U logistics -d logistics -tAc \
   \"select count(*) from information_schema.schemata where schema_name like '%_service'\""

# 2. 테이블 목록 — 엔티티와 대조 (ddl-auto: validate 통과 여부의 사전 확인)
ssh -J ec2-user@$APP ec2-user@$DATA \
  "docker exec pg psql -U logistics -d logistics -c '\\dt *_service.*'"

# 3. App EC2 → Data EC2 3개 포트 (SG 검증)
ssh ec2-user@$APP "nc -zv $DATA 5432 && nc -zv $DATA 6379 && nc -zv $DATA 5672"

# 4. 영속성 — 테스트 행 삽입 → 컨테이너 재시작 → 행 존재 확인
```

---

## Phase 6 — App EC2 배포 및 순차 기동

### TODO

1. `docker-compose.app.yml`을 App EC2로 전송, ECR 이미지 참조로 전환
2. `.env` 작성 — `DB_HOST`, `REDIS_HOST`, `RABBITMQ_HOST` 전부 Data EC2의 **프라이빗 IP**
3. **순차 기동**: Zipkin → eureka-server → gateway-service → 나머지 10개
   - 2 vCPU에서 11개 JVM을 동시 부팅하면 CPU 포화로 Eureka 등록이 서로 타임아웃된다
   - 2~3개씩 나눠 올리고 각 단계에서 헬스체크 통과를 확인
4. 각 단계마다 `free -m`으로 잔여 메모리 추적
5. `restart: unless-stopped` 또는 systemd 유닛으로 재부팅 시 자동 복구 구성

### 🚦 게이트 G6

```bash
# 1. 컨테이너 상태 — 12개 Up, restarting/exited 0개
ssh ec2-user@$APP 'docker compose ps --format "{{.Name}} {{.State}}"'

# 2. 메모리 — available 1500MB 이상
ssh ec2-user@$APP 'free -m'

# 3. Eureka 등록 — 기대: 10
curl -s -H 'Accept: application/json' http://$APP:19090/eureka/apps | grep -o '"name"' | wc -l

# 4. 외부에서 Gateway 경유 호출 (실사용 경로 증명) — 10개 라우트 반복
curl -i http://$APP:19091/api/v1/auth/...

# 5. 서비스 간 통신 — 멀티홉 시나리오 1건 실행 후 Zipkin에서 trace 확인
#    (예: hubRoute 멀티홉 경로 계산 — hub-service를 Feign으로 호출하는 경로)
curl -s "http://$APP:9411/api/v2/traces?limit=5"

# 6. 재부팅 복구
ssh ec2-user@$APP 'sudo reboot' && sleep 120 && ssh ec2-user@$APP 'docker compose ps'
```

**G6 통과 시점이 "배포 완료"다.** Phase 7은 여기에 자동화를 얹는 것이므로, 실패하면 되돌릴 수 있는 안전한 상태를 확보한 셈이다.

---

## Phase 7 — GitHub Actions CD

### TODO

1. **OIDC 인증 구성** — `aws_iam_openid_connect_provider` + GitHub Actions 전용 역할. 롱리브드 액세스 키를 리포지토리 시크릿에 넣지 않는다
2. 기존 `.github/workflows/ci.yml`은 그대로 두고 `deploy.yml` 신규 작성 (`main` 푸시 트리거)
3. 워크플로 단계: checkout → JDK 21 → `./gradlew build` → 11개 이미지 빌드 → ECR 푸시(`:latest` + `:${{ github.sha }}`) → App EC2에서 pull & 재기동
4. 배포 실행 방식: **SSM Send-Command** 권장 (SSH 키를 시크릿에 넣지 않아도 됨). App EC2에 SSM 에이전트 + `AmazonSSMManagedInstanceCore` 필요
5. 배포 후 헬스체크 단계 추가 — 실패 시 워크플로 실패 처리
6. 변경사항 1건을 실제로 푸시해 end-to-end 확인

### 🚦 게이트 G7

```bash
# 1. 커밋 1건 푸시 후 워크플로 성공
gh run watch

# 2. ECR 이미지 태그가 방금 커밋 SHA와 일치
aws ecr describe-images --repository-name logistics/gateway-service \
  --query 'sort_by(imageDetails,&imagePushedAt)[-1].imageTags'

# 3. 실행 중인 컨테이너 이미지 다이제스트가 ECR 최신과 일치
ssh ec2-user@$APP 'docker compose images'

# 4. 배포 후 G6의 3·4번 자동 통과

# 5. 리포지토리 시크릿에 AWS 액세스 키가 없는지 확인
gh secret list
```

---

## Phase 8 — 문서화 및 정리 검증

### TODO

1. `README.md` 갱신 — 현재 "작성 예정"으로 비어 있는 서비스 구성/실행 방법 섹션 채우기
2. 아키텍처 다이어그램 추가 (이 문서의 구조도 기반)
3. 이 문서에 실제 소요 비용과 계획 대비 차이 기록
4. **비용 알림 설정** — AWS Budgets로 $50 임계값 알림 (destroy를 잊었을 때의 안전장치)
5. `terraform destroy` 리허설 — 계획 확인 후 실제 destroy는 시연 종료 시점에
6. destroy 후 잔여 리소스 확인 (EBS 스냅샷, ECR 이미지, Elastic IP는 Terraform 밖에서 남을 수 있음)

### 🚦 게이트 G8

```bash
# 1. destroy 계획에 리소스 누락이 없는지 사전 확인
terraform plan -destroy

# 2. 시연 종료 후 실제 destroy
terraform destroy

# 3. 잔여 과금 리소스 확인 — 전부 빈 출력이어야 함
aws ec2 describe-instances --filters Name=instance-state-name,Values=running --query 'Reservations[].Instances[].InstanceId'
aws ec2 describe-volumes --query 'Volumes[?State==`available`].VolumeId'
aws ec2 describe-addresses --query 'Addresses[].AllocationId'
aws ecr describe-repositories --query 'repositories[].repositoryName'
```

---

## 게이트 요약

| 게이트 | 통과 조건 | 실패 시 |
|---|---|---|
| **G0** | 자격증명·리전·Terraform 확인, **여유 vCPU 6 이상**, VPC 4개 이하, 비용 알림 등록 | **Phase 3 진입 금지** (Phase 1·1.5·2는 진행 가능) |
| **G1** | 로컬 prod로 15개 컨테이너 기동, **Eureka 10개** 등록, Gateway 9개 라우트, 커넥션 40 미만, **환경변수 오버라이드 실측 확인** | Phase 1.5 진입 금지 |
| **G1.5** | **위조 헤더 요청이 401** (핵심), 토큰 없이 401, 정상 토큰 200, 화이트리스트 통과 | Phase 2 진입 금지 |
| **G2** | 단일 빌드로 jar 12개(ai 포함), 이미지 11개(각 460MB 이하), G1·G1.5 재통과, ECR 11개 리포 | Terraform 착수 금지 |
| **G3** | `plan` 무변경, `0.0.0.0/0` 인바운드 0개, NAT source/dest check 비활성 | EC2 생성 금지 |
| **G4** | 3대 running, 배스천 경유 접속, **프라이빗에서 인터넷 아웃바운드 성공** | Phase 5 진입 금지 |
| **G5** | **스키마 9개**, App→Data 3개 포트 연결, 재시작 후 영속 | 앱 배포 금지 |
| **G6** | 외부에서 Gateway 경유 9개 서비스 응답, 여유 메모리 1.5GB+, 재부팅 복구 | **여기까지가 배포 완료** |
| **G7** | 푸시 → 자동 배포 → 헬스체크 통과, AWS 키 시크릿 없음 | 수동 배포 유지 |
| **G8** | destroy 후 잔여 과금 리소스 0 | 콘솔에서 수동 정리 |

---

## 주요 파일

**Eureka·Zipkin 주소(B-1)와 프로파일(B-2)은 환경변수로 덮으므로 11개 `application.yml`이 목록에 없다.** HikariCP도 `SPRING_DATASOURCE_HIKARI_MAXIMUMPOOLSIZE`로 처리한다.

### Phase 1 (기존 파일 수정 2개 / 신규 3개)

| 경로 | 작업 |
|---|---|
| `user-service/.../MockGatewayAuthenticationFilter.java:37` | **`@Profile("local")` 삭제 (B-6)** — 서비스 내부 코드 수정은 이것 하나뿐 |
| `gateway-service/src/main/resources/application.yml` | 라우트 **8개 신규 작성** (B-3). 주석은 예시 템플릿일 뿐 |
| `.env.example` | 환경변수 목록 보강 (루트 파일) |
| `docker-compose.app.yml` | **신규** — 앱 11 + Zipkin. 로컬 검증 시 기존 `docker-compose.yml`과 `-f`로 병합 |
| `docker-compose.data.yml` | **신규** — PG + Redis + RabbitMQ (Data EC2용) |
| `infra/postgres/hub.sql` | **B-4: `hub_route_service` 스키마 추가** — 별도 파일(`hubroute.sql`)로 만들었다가 이후 `hub.sql`에 합침 (`order-inventory-slack.sql`처럼 여러 스키마를 한 파일에 두는 기존 관례와 통일) |
| `infra/postgres/init.sql` | **삭제** — 오래된 중복 파일. `user-slack.sql`의 `p_user` 최신 정의를 무력화시키고 있었음 |
| `*/Dockerfile` (11개) | Phase 2에서 선당김 — 이미지 내 Gradle 빌드 제거, jar 복사 방식으로 |

### Phase 1.5 (S-1)

| 경로 | 작업 |
|---|---|
| `gateway-service/.../filter/JwtAuthenticationGlobalFilter.java` | **신규 — JWT 검증 + 헤더 스트리핑/주입** |
| `gateway-service/.../config/JwtProperties.java` | 신규 — `jwt.secret`, `jwt.whitelist` 바인딩 |
| `gateway-service/build.gradle` | jjwt 3줄 추가 |
| `gateway-service/application.yml` | `jwt.secret`, `jwt.whitelist` 설정 추가 |

### Phase 2

| 경로 | 작업 |
|---|---|
| `*/Dockerfile` (11개) | `eclipse-temurin:21-jre` → `eclipse-temurin:21-jre-alpine` + 선택 스테이지 구조로 추가 전환 (이미지 크기) |
| `docker-compose.app.yml` | healthcheck 11개 `bash /dev/tcp` → `nc -z` (alpine엔 bash 없음) |
| `infra/terraform/providers.tf` | 신규 — AWS provider, `ap-northeast-2` |
| `infra/terraform/variables.tf` | 신규 — 리전, prefix, 서비스명 11개 |
| `infra/terraform/ecr.tf` | 신규 — ECR 리포지토리 11개(`for_each`) + 라이프사이클 정책(이미지 3개 유지) |
| `infra/terraform/outputs.tf` | 신규 — 리포지토리 URL 맵 |
| `.gitignore` | `*.tfstate*`, `.terraform/`, `terraform.tfvars` 추가 |

### Phase 3

| 경로 | 작업 |
|---|---|
| `infra/terraform/network.tf` | 신규 — VPC, 서브넷 2개, IGW, 라우팅 테이블 2개, SG 3개, NAT Instance, SSH 키페어 |
| `infra/terraform/variables.tf` | `my_ip`, `ssh_public_key` 추가 |
| `infra/terraform/outputs.tf` | 네트워크 관련 output 8개 추가 |
| `infra/terraform/terraform.tfvars` | 신규(gitignore 대상) — `my_ip`, `ssh_public_key` |

### Phase 4

| 경로 | 작업 |
|---|---|
| `infra/terraform/compute.tf` | 신규 — App/Data EC2, IAM 역할+인스턴스 프로파일, user-data(Docker+swap) |
| `infra/terraform/network.tf` | NAT user-data에 `iptables-services` 설치·재부팅 복원 추가 (라이브 패치 후 반영) |
| `infra/terraform/outputs.tf` | `app_public_ip`, `data_private_ip` 추가 |

### Phase 5 이후

| 경로 | 작업 |
|---|---|
| `.github/workflows/deploy.yml` | 신규 — OIDC + ECR + SSM 배포 |
| `README.md` | 빈 섹션 채우기 + 인증/인가 한계 명시 |

### 보류 (범위 외)

| 경로 | 사유 |
|---|---|
| `ai-service/**` | B-5로 보류. 모듈은 유지하고 compose·ECR에서만 제외 |
| `infra/postgres/ai.sql` | `ai-service` 포함 시 작성 |

## 재사용하는 기존 자산

- **`docker-compose.yml`** — **수정하지 않는다.** 팀원의 현재 워크플로(미들웨어만 도커, 앱은 IDE)를 유지하고, 로컬 전체 검증 시 `-f`로 병합해 재사용
- `infra/postgres/*.sql` — 기존 도메인별 스크립트 5개는 그대로 사용, `hub.sql`에 `hub_route_service` 스키마 추가 (오래된 중복본 `init.sql`은 삭제, 총 5개 파일)
- `*/src/main/resources/application-prod.yml` — 이미 `${DB_HOST}` 등 환경변수 주입 구조가 준비되어 있음
- `hubRoute-service/application.yml` — Redis는 이미 `${REDIS_HOST:localhost}` 패턴이라 B-1 대상 아님
- `ai-service/application-prod.yml` — RabbitMQ도 이미 `${RABBITMQ_HOST:rabbitmq}` 패턴
- `.github/workflows/ci.yml` — 빌드/테스트 단계를 `deploy.yml`에서 재활용
- `*/Dockerfile` — 멀티스테이지 구조는 유지하고 빌드 스테이지만 제거

## 참고: Eureka·Zipkin의 역할

| | Eureka | Zipkin |
|---|---|---|
| 역할 | 서비스 레지스트리 — 각 서비스가 이름으로 등록하고, 호출 측은 IP 대신 이름으로 조회 | 분산 추적 — 요청에 trace ID를 붙여 서비스 경계를 넘어 전파, 구간별 소요 시간 수집 |
| 사용처 | Gateway의 `lb://user-service` 라우팅, FeignClient 14개의 이름 기반 호출 | 루트 `build.gradle`의 `subprojects`에 적용되어 12개 모듈 전부 자동 추적 |
| 장애 시 | **전체 API 503** — Gateway 라우팅·Feign 호출 전부 실패 | 추적만 불가, 서비스는 정상 동작 |
| 게이트 우선순위 | 최우선 (G1-2, G6-3) | 부차 (G6-5) |

가장 많이 호출되는 서비스는 `hub-service`로, `product`·`hubRoute`·`inventory`·`user`·`company`·`delivery`·`ai` 7개가 Feign으로 의존한다. Eureka 장애 시 영향 범위가 가장 큰 지점이다.

> **참고**: 앱을 EC2 1대에 docker-compose로 띄우는 현 구성에서는 Docker 내장 DNS가 서비스명을 해석하므로 Eureka가 기술적으로 필요하지 않다(서비스당 인스턴스 1개, 단일 호스트). 코드가 이미 Eureka 전제(`lb://`, FeignClient 14개)이고 MSA 학습이 목적이므로 유지하되, 이것이 "인스턴스가 여러 개라서" 도입한 것은 아님을 인지하고 있어야 한다. Phase 6의 순차 기동 요구사항도 사실상 Eureka 등록 타임아웃 때문에 생긴 제약이다.

## 참고: 현재 RBAC 구조

**role 부여 방식은 "권한 부여"가 아니라 "권한 신청 승인" 모델이다.**

1. 회원가입 시 **신청자가 role을 직접 선택**한다 (`SignupCommandDto.role()`). `MASTER`만 차단된다 (`SignupService.java:71`)
2. 관리자는 `PENDING → APPROVED/REJECTED` 상태만 바꾸고 **role은 변경하지 않는다** (`UserApprovalService`)
3. 최초 `MASTER`는 환경변수(`MASTER_USERNAME`/`PASSWORD`/`SLACK_ID`)로 부트스트랩된다

**승인 권한** (`UserApprovalService.java:127-172`)

| 처리자 role | 승인 가능 범위 |
|---|---|
| `MASTER` | 전체 |
| `HUB_MANAGER` | 같은 `hubId` 사용자만. MASTER 신청은 불가 |
| `HUB_DELIVERY_MANAGER`, `COMPANY_MANAGER`, `COMPANY_DELIVERY_MANAGER` | 없음 |

**인가 집행 위치**

| 서비스 | 필터 | 권한 문자열 |
|---|---|---|
| `user` | `MockGatewayAuthenticationFilter` (`@Profile("local")`) | `ROLE_` 접두사 있음 |
| `company`, `product` | `HeaderAuthenticationFilter` | 접두사 없음 |
| 나머지 7개 | 없음 | — |

배포 관점에서 이 구조가 만드는 문제는 B-6(prod 기동 실패)·B-7(시크릿 누락)·S-1(인증 우회)로 위에 정리했다.

## 리스크

| 리스크 | 영향 | 대응 |
|---|---|---|
| 앱 서버 메모리 부족 (12 JVM) | 컨테이너 OOM Kill | `-Xmx256m` + `mem_limit` + swap 2GB. G1에서 조기 발견 |
| **스키마 누락 (B-4)** | `hubRoute` 기동 불가 | Phase 1에서 `hubroute.sql` 작성, G5에서 스키마 9개 확인 |
| **`SecurityConfig` 프로파일 불일치 (B-6)** | `user-service` 기동 불가 → 로그인·인증 전체 마비 | Phase 1.5에서 `@Profile("local")` 삭제, G1-8에서 로그로 확인 |
| **JWT·MASTER 시크릿 누락 (B-7)** | 로그인 불가, MASTER 계정 없음 | `application-prod.yml` + `.env` 보강, G1-9·10에서 확인 |
| **Gateway 인증 미구현 (S-1)** | 헤더 위조로 누구나 MASTER 사칭 | Phase 1.5에서 해결. G1.5-2가 핵심 검증. 완료 전까지 19091을 본인 IP로만 개방 |
| 헤더 스트리핑 누락 | 필터를 만들어도 우회 가능 (S-1이 그대로 남음) | 필터 로직 1번을 화이트리스트 판정보다 **먼저** 수행. G1.5-2로 검증 |
| Gateway 우회 시 서비스 무방비 | 서비스에 직접 접근하면 헤더 위조 가능 | 네트워크로만 방어 — SG에서 19091만 개방, 서비스는 docker 내부 네트워크에만 노출 |
| 인가(role별 접근 제어) 미구현 | 로그인한 사용자가 7개 서비스의 모든 API 호출 가능 | 이번 범위 밖. README에 명시하고 별도 이슈로 |
| `ai-service` 미완성 (B-5) | 시연 중 오류 | 범위 포함 여부를 Phase 1에서 먼저 결정 |
| `ddl-auto: validate` 불일치 | 서비스 기동 불가 | G5에서 테이블을 엔티티와 대조 |
| NAT Instance 오구성 | Data EC2가 이미지 pull 불가 | G4-3에서 명시 검증. `fck-nat` AMI로 회피 가능 |
| 11개 JVM 동시 부팅 | Eureka 등록 타임아웃 | 순차 기동 (Phase 6) |
| **환경변수 오버라이드 미적용** | yml에 `localhost`가 남아 있어 "고쳤다고 착각"하기 쉬움 | G1-9에서 컨테이너의 실제 환경변수를 눈으로 확인 |
| 케밥케이스 환경변수 오인 | `app.master.slack-id` → `APP_MASTER_SLACKID`(대시 제거) | 해당 항목만 yml에 플레이스홀더로 명시 |
| **vCPU 쿼터 부족** | Phase 3 `terraform apply`가 3번째 인스턴스에서 실패. 증설 승인에 수 시간~며칠 대기 | **Phase 0에서 최우선 확인.** 기존 계정이므로 사용 중 vCPU와 합산 판단, 부족하면 즉시 신청 후 Phase 1 병행 |
| VPC 한도 초과 | 새 VPC 생성 불가 | Phase 0에서 기존 VPC 개수 확인 (기본 한도 5) |
| 기존 리소스와 이름 충돌 | ECR·SG 생성 실패 | `logistics-` prefix 일관 적용, G0-8에서 사전 확인 |
| destroy 누락 | 과금 지속 | AWS Budgets $50 알림 + 캘린더 등록. **알림은 리소스 생성 전에 설정**(Phase 0) |
| Terraform state에 평문 비밀번호 | 자격증명 유출 | `.gitignore` 등록. 커밋 이력 확인 |
