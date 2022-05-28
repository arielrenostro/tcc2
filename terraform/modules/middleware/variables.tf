variable "env" {
  description = "Environment"
  type        = string
}

variable "cluster" {
  description = "Cluster information"

  type = object({
    id   = string
    arn  = string
    name = string
  })
}

variable "alb_id" {
  description = "ALB ID"
  type        = string
}

variable "vpc_id" {
  description = "VPC ID"
  type        = string
}

variable "role_arn" {
  description = "Role ARN ID"
  type        = string
}

variable "logs_retention" {
  description = "CloudWatch logs retention in days"
  type        = number
}

variable "subnets_ids" {
  description = "Subnet Ids"
  type        = set(string)
}

variable "security_groups" {
  description = "Security Groups IDs"
  type        = set(string)
}
