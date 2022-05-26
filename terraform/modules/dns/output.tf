output "route53" {
  value = {
    zone_id = aws_route53_zone.middleware.id
    domain  = aws_route53_zone.middleware.name
  }
}
