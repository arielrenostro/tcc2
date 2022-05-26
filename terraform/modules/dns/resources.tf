resource "aws_route53_zone" "middleware" {
  name = var.domain_name
}
