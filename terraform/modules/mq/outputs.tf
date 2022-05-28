output "host" {
  value = "${aws_mq_broker.mq.id}.mq.${data.aws_region.current.name}.amazonaws.com"
}

output "username" {
  value = var.username
}

output "password" {
  value = var.password
}
