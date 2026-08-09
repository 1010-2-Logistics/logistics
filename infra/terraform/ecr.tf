# Phase 2 — ECR 리포지토리 11개 (ai-service는 B-5로 보류, 제외)

resource "aws_ecr_repository" "service" {
  for_each = toset(var.service_names)

  name                 = "${var.project_prefix}/${each.value}"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }
}

# 이미지 3개만 유지 — 포트폴리오용 단기 배포라 스토리지 비용 관리 목적
resource "aws_ecr_lifecycle_policy" "service" {
  for_each = aws_ecr_repository.service

  repository = each.value.name

  policy = jsonencode({
    rules = [{
      rulePriority = 1
      description  = "keep last 3 images"
      selection = {
        tagStatus   = "any"
        countType   = "imageCountMoreThan"
        countNumber = 3
      }
      action = { type = "expire" }
    }]
  })
}
