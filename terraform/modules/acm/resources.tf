resource "aws_acm_certificate" "middleware_domain" {
  domain_name               = var.middleware_domain
  validation_method         = "DNS"
  subject_alternative_names = var.middleware_alternatives

  tags = {
    Name = "acm-${var.middleware_domain}"
  }
}

resource "aws_route53_record" "middleware_domain_acm" {
  for_each = {

  for dvo in aws_acm_certificate.middleware_domain.domain_validation_options : dvo.domain_name => {
    name   = dvo.resource_record_name
    record = dvo.resource_record_value
    type   = dvo.resource_record_type
  }

  }

  allow_overwrite = true
  name            = each.value.name
  records         = [each.value.record]
  ttl             = 60
  type            = each.value.type
  zone_id         = var.route53.zone_id
}
