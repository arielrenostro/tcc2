variable "env" {
  description = "Ambiente da aplicação"
  type        = string
  default     = "stg"
}

variable "dns" {
  default = {
    route53 = {
      zone_id = "Z036681222L117T3KDZ5N"
      domain = "ariel-middleware.site"
    }
  }
}
