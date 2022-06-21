variable "env" {
  description = "Ambiente da aplicação"
  type        = string
  default     = "stg"
}

# São duas formas de gerenciar o DNS (Route53): pelo Terraform, criado manualmente;

# Este método é gerenciado pelo Terraform.
# Quando utilizado este método, deve ser criado o módulo "dns"
#variable "domain" {
#  description = "Nome do domínio que será criado no Route53"
#  type        = string
#  default     = "ariel-middleware.site"
#}

# Este método é o manual.
# Quando utilizado este método, deve ser removido o módulo "dns".
variable "dns" {
  default = {
    route53 = {
      zone_id = "Z036681222L117T3KDZ5N"
      domain  = "ariel-middleware.site"
    }
  }
}

variable "mq_username" {
  description = "Usuário do RabbitMQ"
  type        = string
  default     = "admin"
}

variable "mq_password" {
  description = "Senha do RabbitMQ"
  type        = string
  default     = "@Batata-1234"
}

variable "bastion_public_key" {
  description = "Bastion SSH Public Key"
  type        = string
  default     = "ssh-rsa xxxx"
}

variable "mongodb_public_key" {
  description = "MongoDB SSH Public Key"
  type        = string
  default     = "ssh-rsa xxxx"
}

variable "influxdb_public_key" {
  description = "InfluxDB SSH Public Key"
  type        = string
  default     = "ssh-rsa xxxx"
}
