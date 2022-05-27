output "middleware_lb" {
  value = aws_security_group.middleware_lb.id
}

output "middleware_ecs" {
  value = aws_security_group.middleware_ecs.id
}

output "middleware_cache" {
  value = aws_security_group.middleware_cache.id
}

output "middleware_mq" {
  value = aws_security_group.middleware_mq.id
}
