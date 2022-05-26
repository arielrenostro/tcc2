output "middleware_lb" {
  value = aws_security_group.middleware_lb.id
}

output "middleware_ecs" {
  value = aws_security_group.middleware_ecs.id
}
