variable "aws_region" {
  type    = string
  default = "ap-northeast-2"
}

variable "project_prefix" {
  description = "기존 계정 리소스와 구분하기 위한 네이밍 prefix"
  type        = string
  default     = "logistics"
}

variable "my_ip" {
  description = "app-sg 인바운드(SSH/Gateway/Zipkin)를 제한할 본인 공인 IP. CIDR 형식 (예: 1.2.3.4/32)"
  type        = string
}

variable "ssh_public_key" {
  description = "aws_key_pair에 등록할 SSH 공개키 내용 (개인키는 다루지 않는다)"
  type        = string
}

# GitHub은 OIDC sub 클레임에 조직·리포지토리의 불변 ID를 포함한 형식을 사용한다.
#   repo:{owner}@{orgId}/{repo}@{repoId}
# owner/repo 문자열만으로 조건을 걸면 실제 토큰과 매칭되지 않아
# sts:AssumeRoleWithWebIdentity가 거부된다.
#
# 확인 방법:
#   gh api repos/{owner}/{repo}/actions/oidc/customization/sub --jq .sub_claim_prefix
#
# ID 기반이라 조직·리포지토리 이름이 바뀌어도 그대로 동작하고,
# 같은 이름의 다른 리포지토리에는 권한이 넘어가지 않아 더 안전하다.
variable "github_sub_prefix" {
  description = "GitHub Actions OIDC sub 클레임 접두사 (불변 ID 포함 형식)"
  type        = string
  default     = "repo:1010-2-Logistics@312060404/logistics@1320049011"
}

variable "service_names" {
  description = "ECR 리포지토리를 만들 배포 대상 11개 서비스"
  type        = list(string)
  default = [
    "eureka-server",
    "gateway-service",
    "user-service",
    "hub-service",
    "hubroute-service",
    "company-service",
    "product-service",
    "order-service",
    "inventory-service",
    "delivery-service",
    "slack-service",
  ]
}
