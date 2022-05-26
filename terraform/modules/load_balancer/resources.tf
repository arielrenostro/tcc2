resource "aws_lb" "middleware" {
  name            = "middleware-${var.env}"
  internal        = false
  security_groups = var.middleware_security_groups
  subnets         = var.middleware_subnets

  enable_deletion_protection = false
}

resource "aws_lb_listener" "middleware" {
  load_balancer_arn = aws_lb.middleware.arn
  port              = "443"
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-2016-08"
  certificate_arn   = var.middleware_acm_arn

  default_action {
    type             = "forward"
    target_group_arn = var.middleware_target_group_arn
  }
}

resource "aws_lb_listener" "middleware_http" {
  load_balancer_arn = aws_lb.middleware.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type = "redirect"

    redirect {
      port        = "443"
      protocol    = "HTTPS"
      status_code = "HTTP_301"
    }
  }
}

resource "aws_route53_record" "middleware" {
  zone_id = var.route53.zone_id
  name    = var.middleware_domain
  type    = "CNAME"
  ttl     = "300"
  records = [aws_lb.middleware.dns_name]
}
