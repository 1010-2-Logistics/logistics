#!/bin/bash
# App EC2에서 SSM Send-Command로 실행되는 배포 스크립트.
#
# 11개 컨테이너를 한 번에 up -d 하지 않는다. m5.large가 2vCPU라 JVM 11개가
# 동시에 뜨면 CPU 포화로 Eureka 등록이 서로 타임아웃난다(Phase 6에서 확인된
# 문제). 최초 배포 때와 동일하게 eureka → gateway → 3개씩 3배치로 순차 기동한다.
set -euo pipefail

cd /home/ec2-user/logistics

aws ecr get-login-password --region ap-northeast-2 \
  | docker login --username AWS --password-stdin 116971679055.dkr.ecr.ap-northeast-2.amazonaws.com

# --quiet가 없으면 레이어 추출 진행률이 수천 줄 쏟아진다. SSM은 명령 출력을
# 24,000자에서 자르므로 진행률이 그 한도를 다 먹고 정작 뒤에서 난 실패 메시지가
# 잘려 나간다. 실제로 배포 실패 원인을 못 찾아 두 번 헤맸다.
docker compose -f docker-compose.app.yml pull --quiet

wait_healthy() {
  for name in "$@"; do
    n=0
    until [ "$(docker inspect -f '{{.State.Health.Status}}' "$name" 2>/dev/null)" = "healthy" ] || [ "$n" -ge 40 ]; do
      sleep 3
      n=$((n + 1))
    done
    if [ "$(docker inspect -f '{{.State.Health.Status}}' "$name" 2>/dev/null)" != "healthy" ]; then
      echo "$name did not become healthy in time"
      exit 1
    fi
  done
}

docker compose -f docker-compose.app.yml up -d zipkin eureka-server
wait_healthy logistics-eureka-server

docker compose -f docker-compose.app.yml up -d gateway-service
wait_healthy logistics-gateway-service

docker compose -f docker-compose.app.yml up -d user-service order-service product-service
wait_healthy logistics-user-service logistics-order-service logistics-product-service

docker compose -f docker-compose.app.yml up -d delivery-service hub-service inventory-service
wait_healthy logistics-delivery-service logistics-hub-service logistics-inventory-service

docker compose -f docker-compose.app.yml up -d company-service hubroute-service slack-service
wait_healthy logistics-company-service logistics-hubroute-service logistics-slack-service

docker compose -f docker-compose.app.yml up -d ai-service
wait_healthy logistics-ai-service

docker compose -f docker-compose.app.yml ps --format '{{.Name}} {{.State}} {{.Status}}'

for i in $(seq 1 20); do
  code=$(curl -s -o /dev/null -w '%{http_code}' http://localhost:19091/actuator/health || true)
  [ "$code" = "200" ] && exit 0
  sleep 15
done

echo "gateway health check failed"
exit 1
