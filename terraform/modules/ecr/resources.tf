resource "aws_ecr_repository" "middleware" {
  name                 = "middleware"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = false
  }
}

resource "aws_ecr_repository" "service_chat" {
  name                 = "service-chat"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = false
  }
}

resource "aws_ecr_repository" "client_chat" {
  name                 = "client-chat"
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = false
  }
}
