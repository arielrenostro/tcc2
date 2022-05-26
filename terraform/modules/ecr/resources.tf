resource "aws_ecr_repository" "middleware" {
  name                 = "middleware"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = false
  }
}
