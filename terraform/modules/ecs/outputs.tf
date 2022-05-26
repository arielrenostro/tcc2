output "middleware" {
  value = {
    id   = aws_ecs_cluster.middleware.id
    arn  = aws_ecs_cluster.middleware.arn
    name = aws_ecs_cluster.middleware.name
  }
}
