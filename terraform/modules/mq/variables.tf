variable "env" {
  description = "Environment"
  type        = string
}

variable "instance_type" {
  description = "Tipo da instância"
  type        = string
}

variable "username" {
  type = string
}

variable "password" {
  type = string
}

variable "security_groups" {
  type = set(string)
}

variable "subnets" {
  type = set(string)
}

