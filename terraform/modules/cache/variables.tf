variable "env" {
  description = "Environment"
  type        = string
}

variable "subnets" {
  type = set(string)
}

variable "security_groups" {
  type = set(string)
}

variable "route53" {
  description = "Route 53"
  type        = object({
    domain  = string
    zone_id = string
  })
}
