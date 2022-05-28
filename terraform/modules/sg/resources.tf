resource "aws_security_group" "middleware_lb" {
  name        = "middleware-loadbalancer-${var.env}"
  description = "middleware-loadbalancer-${var.env}"
  vpc_id      = var.vpc_id

  tags = {
    Name = "middleware-loadbalancer-${var.env}"
  }
}

resource "aws_security_group_rule" "middleware_lb_in_http" {
  type              = "ingress"
  description       = "HTTP - Public"
  protocol          = "tcp"
  from_port         = 80
  to_port           = 80
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.middleware_lb.id
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
  description              = "HTTP - ALB"
  protocol                 = "tcp"
  from_port                = 8080
  to_port                  = 8080
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
  from_port                = 5671
  to_port                  = 5672
  source_security_group_id = aws_security_group.middleware_ecs.id
  security_group_id        = aws_security_group.middleware_mq.id
}

resource "aws_security_group_rule" "middleware_mq_in_bastion" {
  type                     = "ingress"
  description              = "RabbitMQ Manager - Bastion"
  protocol                 = "tcp"
  from_port                = 443
  to_port                  = 443
  source_security_group_id = aws_security_group.bastion.id
  security_group_id        = aws_security_group.middleware_mq.id
}


resource "aws_security_group" "bastion" {
  name        = "bastion-${var.env}"
  description = "bastion-${var.env}"
  vpc_id      = var.vpc_id

  tags = {
    Name = "bastion-${var.env}"
  }
}

resource "aws_security_group_rule" "bastion_in_ssh" {
  type              = "ingress"
  description       = "SSH - All"
  protocol          = "tcp"
  from_port         = 22
  to_port           = 22
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.bastion.id
}

resource "aws_security_group_rule" "bastion_out_all" {
  type              = "egress"
  description       = "Output default"
  protocol          = "-1"
  from_port         = 0
  to_port           = 0
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.bastion.id
}


resource "aws_security_group" "mongodb" {
  name        = "mongodb-${var.env}"
  description = "mongodb-${var.env}"
  vpc_id      = var.vpc_id

  tags = {
    Name = "mongodb-${var.env}"
  }
}

resource "aws_security_group_rule" "mongodb_in_ssh" {
  type                     = "ingress"
  description              = "SSH - All"
  protocol                 = "tcp"
  from_port                = 22
  to_port                  = 22
  source_security_group_id = aws_security_group.bastion.id
  security_group_id        = aws_security_group.mongodb.id
}

resource "aws_security_group_rule" "mongodb_in_ecs" {
  type                     = "ingress"
  description              = "MongoDB - ECS"
  protocol                 = "tcp"
  from_port                = 27017
  to_port                  = 27017
  source_security_group_id = aws_security_group.middleware_ecs.id
  security_group_id        = aws_security_group.mongodb.id
}

resource "aws_security_group_rule" "mongodb_out_all" {
  type              = "egress"
  description       = "Output default"
  protocol          = "-1"
  from_port         = 0
  to_port           = 0
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.mongodb.id
}
