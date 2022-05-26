output "middleware_ecs" {
  value = {
    id               = aws_iam_role.middleware_ecs.id
    name             = aws_iam_role.middleware_ecs.name
    arn              = aws_iam_role.middleware_ecs.arn
  }
}
