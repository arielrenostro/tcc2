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
