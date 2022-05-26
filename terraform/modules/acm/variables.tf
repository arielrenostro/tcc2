variable "middleware_domain" {
  description = "Domínio do middleware"
  type        = string
}

variable "middleware_alternatives" {
  description = "Domínios alternativos para o middleware"
  type        = set(string)
}

variable "route53" {
  description = "Route 53"
  type        = object({
    domain  = string
    zone_id = string
  })
}
