resource "aws_ecs_cluster" "middleware" {
  name = "middleware-${var.env}"
}
