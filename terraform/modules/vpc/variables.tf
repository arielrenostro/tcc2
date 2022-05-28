variable "env" {
  description = "Environment"
  type        = string
}

variable "vpc_cidr_block" {
  description = "CIDR block of the VPC"
  type        = string
}

variable "subnet_a_private_cidr_block" {
  description = "CIDR block of the Subnet Private A"
  type        = string
}

variable "subnet_b_private_cidr_block" {
  description = "CIDR block of the Subnet Private B"
  type        = string
}

variable "subnet_a_public_cidr_block" {
  description = "CIDR block of the Subnet Public A"
  type        = string
}

variable "subnet_b_public_cidr_block" {
  description = "CIDR block of the Subnet Public B"
  type        = string
}

variable "route53" {
  description = "Route 53"
  type        = object({
    domain  = string
    zone_id = string
  })
}
