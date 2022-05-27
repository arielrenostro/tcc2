data "aws_region" "current" {}

resource "aws_mq_broker" "mq" {
  broker_name                = "middleware-${var.env}"
  engine_type                = "RabbitMQ"
  engine_version             = "3.9.13"
  host_instance_type         = var.instance_type
  auto_minor_version_upgrade = false
  deployment_mode            = "SINGLE_INSTANCE"
  publicly_accessible        = false
  security_groups            = var.security_groups
  subnet_ids                 = var.subnets

  user {
    password = var.password
    username = var.username
  }

  tags = {
    Name = "middleware-${var.env}"
  }
}

resource "aws_route53_record" "mq" {
  zone_id = var.route53.zone_id
  name    = "mq.${var.env}.${var.route53.domain}"
  type    = "CNAME"
  ttl     = "300"
  records = ["${aws_mq_broker.mq.id}.mq.${data.aws_region.current.name}.amazonaws.com"]
}
