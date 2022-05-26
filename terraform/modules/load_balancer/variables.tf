variable "env" {
  description = "Environment"
  type        = string
}

variable "middleware_security_groups" {
  description = "Middleware Security Groups"
  type        = set(string)
}

variable "middleware_subnets" {
  description = "Middleware Subnets"
  type        = set(string)
}

variable "middleware_acm_arn" {
  description = "Middleware ACM ARN"
  type        = string
}

variable "middleware_target_group_arn" {
  description = "Middleware Target Group ARN"
  type        = string
}

variable "middleware_domain" {
  description = "Domínio do Middleware"
  type        = string
}

variable "route53" {
  description = "Route 53"
  type        = object({
    domain  = string
    zone_id = string
  })
}
