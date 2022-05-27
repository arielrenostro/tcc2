resource "aws_security_group" "middleware_lb" {
  name        = "middleware-loadbalancer-${var.env}"
  description = "middleware-loadbalancer-${var.env}"
  vpc_id      = var.vpc_id

  tags = {
    Name = "middleware-loadbalancer-${var.env}"
  }
}

resource "aws_security_group_rule" "middleware_lb_in_https" {
  type              = "ingress"
  description       = "HTTPS - Public"
  protocol          = "tcp"
  from_port         = 443
  to_port           = 443
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.middleware_lb.id
}

resource "aws_security_group_rule" "middleware_lb_out" {
  type              = "egress"
  description       = "Output default"
  protocol          = "-1"
  from_port         = 0
  to_port           = 0
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.middleware_lb.id
}



resource "aws_security_group" "middleware_ecs" {
  name        = "middleware-ecs-${var.env}"
  description = "middleware-ecs-${var.env}"
  vpc_id      = var.vpc_id

  tags = {
    Name = "middleware-ecs-${var.env}"
  }
}

resource "aws_security_group_rule" "middleware_ecs_in_https" {
  type                     = "ingress"
  description              = "ALL HTTP - LB"
  protocol                 = "tcp"
  from_port                = 0
  to_port                  = 65535
  source_security_group_id = aws_security_group.middleware_lb.id
  security_group_id        = aws_security_group.middleware_ecs.id
}

resource "aws_security_group_rule" "middleware_ecs_out" {
  type              = "egress"
  description       = "Output default"
  protocol          = "-1"
  from_port         = 0
  to_port           = 0
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.middleware_ecs.id
}


resource "aws_security_group" "middleware_cache" {
  name        = "middleware-cache-${var.env}"
  description = "middleware-cache-${var.env}"
  vpc_id      = var.vpc_id

  tags = {
    Name = "middleware-cache-${var.env}"
  }
}

resource "aws_security_group_rule" "middleware_cache_in_ecs" {
  type                     = "ingress"
  description              = "Redis - ECS"
  protocol                 = "tcp"
  from_port                = 6379
  to_port                  = 6379
  source_security_group_id = aws_security_group.middleware_ecs.id
  security_group_id        = aws_security_group.middleware_cache.id
}


resource "aws_security_group" "middleware_mq" {
  name        = "middleware-mq-${var.env}"
  description = "middleware-mq-${var.env}"
  vpc_id      = var.vpc_id

  tags = {
    Name = "middleware-mq-${var.env}"
  }
}

resource "aws_security_group_rule" "middleware_mq_in_ecs" {
  type                     = "ingress"
  description              = "RabbitMQ - ECS"
  protocol                 = "tcp"
  from_port                = 5672
  to_port                  = 5672
  source_security_group_id = aws_security_group.middleware_ecs.id
  security_group_id        = aws_security_group.middleware_mq.id
}
