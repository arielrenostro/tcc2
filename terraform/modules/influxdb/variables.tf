variable "env" {
  description = "Environment"
  type        = string
}

variable "ami" {
  type = string
}

variable "instance_type" {
  type = string
}

variable "security_groups" {
  type = set(string)
}

variable "subnet" {
  type = object({
    id                = string
    availability_zone = string
  })
}

variable "public_key" {
  type = string
}

variable "route53" {
  description = "Route 53"
  type        = object({
    domain  = string
    zone_id = string
  })
}
