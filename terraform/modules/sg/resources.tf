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
