package com.logistics.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinition;

/**
 * application.yml의 routes: 설정이 실제로 반영되는지 확인한다.
 * 라우트 블록이 통째로 대체돼 업무 경로가 사라진 사고(#244)가 CI를 그대로
 * 통과했던 이유는, 기동 성공 여부만 볼 뿐 라우팅 내용은 아무도 검증하지
 * 않았기 때문이다.
 *
 * compiled Route(RouteLocator)가 아니라 raw 설정(RouteDefinitionLocator)을
 * 본다 — predicate를 실행할 필요 없이 문자열로 비교할 수 있어 더 간단하고,
 * 실제로 터졌던 두 사고(경로 누락, id 중복으로 인한 라우트 소실)를
 * 그대로 재현해서 잡는다.
 */
/**
 * webEnvironment를 지정하지 않는다 (기본값 MOCK).
 * NONE으로 두면 spring.main.web-application-type=none이 강제돼, 리액티브 웹
 * 자동 설정에서 나오는 ServerProperties 빈이 아예 안 만들어진다. 게이트웨이의
 * NettyConfiguration(gatewayHttpClientFactory)이 그 빈을 요구해 기동이 실패한다.
 * MOCK은 실제 포트를 열지 않으면서도 리액티브 웹 컨텍스트를 구성해 이 문제가 없다.
 */
@SpringBootTest(properties = {
        // eureka.client.enabled=false로 완전히 끄면 게이트웨이 자동 설정이 요구하는
        // DiscoveryClient 빈 자체가 안 만들어져 NoSuchBeanDefinitionException이 난다.
        // 등록·조회만 끄고 클라이언트 빈은 살려서 실제 Eureka 서버 없이도 뜨게 한다.
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        // JwtAuthenticationGlobalFilter가 기동 시 HMAC 키를 만든다.
        // HS256은 256비트(32바이트) 이상이 필요해 짧은 값은 WeakKeyException으로 거부된다.
        "jwt.secret=test-only-secret-key-please-make-it-32-bytes-min"
})
class GatewayRouteTest {

    @Autowired
    private RouteDefinitionLocator routeDefinitionLocator;

    private List<RouteDefinition> routes;

    @BeforeEach
    void setUp() {
        routes = routeDefinitionLocator.getRouteDefinitions().collectList().block();
    }

    static Stream<Arguments> businessRoutes() {
        return Stream.of(
                Arguments.of("user-service", "/api/v1/users/**"),
                Arguments.of("user-service", "/api/v1/auth/**"),
                Arguments.of("hub-service", "/api/v1/hubs/**"),
                Arguments.of("hubRoute-service", "/api/v1/hubRoute/**"),
                Arguments.of("company-service", "/api/v1/companies/**"),
                Arguments.of("product-service", "/api/v1/products/**"),
                Arguments.of("order-service", "/api/v1/orders/**"),
                Arguments.of("inventory-service", "/api/v1/inventories/**"),
                Arguments.of("delivery-service", "/api/v1/deliveries/**"),
                Arguments.of("delivery-service", "/api/v1/delivery-managers/**"),
                Arguments.of("slack-service", "/api/v1/slack/**"),
                Arguments.of("ai-service", "/api/v1/ai/**")
        );
    }

    @ParameterizedTest(name = "{1} -> lb://{0}")
    @MethodSource("businessRoutes")
    void 업무_경로가_올바른_서비스로_라우팅된다(String expectedService, String pathPattern) {
        boolean found = routes.stream().anyMatch(route ->
                route.getUri().toString().equals("lb://" + expectedService)
                        && route.getPredicates().stream().anyMatch(predicate ->
                                "Path".equals(predicate.getName())
                                        && predicate.getArgs().values().stream()
                                                .anyMatch(v -> v.contains(pathPattern))));

        assertThat(found)
                .as("%s 요청이 lb://%s 로 라우팅되는 설정을 찾을 수 없다", pathPattern, expectedService)
                .isTrue();
    }

    @Test
    void 라우트_id가_전역에서_유일하다() {
        // Spring Cloud Gateway는 id가 겹치면 나중 정의가 앞선 정의를 조용히 덮는다.
        // 업무 라우트와 문서(swagger) 라우트의 id가 우연히 같아지면 이 테스트가 잡는다.
        List<String> ids = routes.stream().map(RouteDefinition::getId).toList();
        assertThat(ids).doesNotHaveDuplicates();
    }

    @Test
    void 업무_라우트와_문서_라우트가_서비스_수만큼_모두_존재한다() {
        long businessCount = routes.stream()
                .filter(route -> hasPredicateNotContaining(route, "/v3/api-docs"))
                .count();
        long docsCount = routes.stream()
                .filter(route -> hasPredicateContaining(route, "/v3/api-docs"))
                .count();

        assertThat(businessCount).as("업무 라우트 10개가 모두 있어야 한다").isEqualTo(10);
        assertThat(docsCount).as("문서 라우트 10개가 모두 있어야 한다").isEqualTo(10);
    }

    private boolean hasPredicateContaining(RouteDefinition route, String needle) {
        return route.getPredicates().stream()
                .anyMatch(p -> p.getArgs().values().stream().anyMatch(v -> v.contains(needle)));
    }

    private boolean hasPredicateNotContaining(RouteDefinition route, String needle) {
        return route.getPredicates().stream()
                .anyMatch(p -> p.getArgs().values().stream().noneMatch(v -> v.contains(needle)));
    }
}
