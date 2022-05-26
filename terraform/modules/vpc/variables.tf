variable "env" {
  description = "Environment"
  type        = string
}

variable "vpc_cidr_block" {
  description = "CIDR block of the VPC"
  type        = string
}

variable "subnet_a_cidr_block" {
  description = "CIDR block of the Subnet A"
  type        = string
}

variable "subnet_a_public_cidr_block" {
  description = "CIDR block of the Subnet Public A"
  type        = string
}

variable "route53" {
  description = "Route 53"
  type        = object({
    domain  = string
    zone_id = string
  })
}
