variable "env" {
  description = "Ambiente da aplicação"
  type        = string
  default     = "stg"
}

# São duas formas de gerenciar o DNS: pelo Terraform, criado manualmente;

# Este método é gerenciado pelo Terraform.
# Quando utilizado este método, deve ser criado o módulo "dns"
variable "domain" {
  description = "Nome do domínio que será criado no Route53"
  type        = string
  default     = "ariel-middleware.site"
}

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
  default     = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQDWlJgCHZ9DuZQD92i391zN7bTKquOVTzP+frQSSXkfkPdrruY9ggbxnmFCkzwgDbsoAy5e1Wd68lSh752h8l1AODQOOhVFW0sT3iz9TXIu5Q300/iwlsIpJ1tLtlQNR8i/YX5zij9C9ZzFKUXqC2zckVxNHHWDcv6pieKx5EUZ4ba1EINqk3htnBVaQh1jlJem+zt5oVsXb2ylNQ3mBY6uxiU5P3tpv6FDigS25BROwxbMd6WL7bwDusmrTgH7jyCljjmpEpSLko2nEPETWwCC6wu4YGgnsCBgY5RQxSOJhhxnCRNaVWL9jzgIDAJ2peVb+gpgWRt8MMoEwRSKeAvuAvrDC3CFMcHVZvEFtppW6t4pFrtrParH22f4frqHaZMX+7LFlUMuMA9LP5OlB4buAX+QUU1RTQXp0TlVRpMKXGEGEXoKTJZ8ge2JCKTbsK3RIErJVqMYk2nJ/BdWwnNgFrGNgJ59wdG2DzthOQElI2xG41P+aYbisl29sr56GGzOunToM36+SajsdL3mC59MyFWR5hnIRO0i/zeKblNszjt3ndR7v/2evyGZHjY7p6pvCdr3AD7Izy7wfu7oVtVYJaEV5bW+MD6qdDDspHOk5ktt4o+WYWb2bhhwPFj7BRYzaOz8RenwYIPSjq9StCuE1rZ8SQ1oM9JGqT4xShZjsQ== ariel@ariel"
}

variable "mongodb_public_key" {
  description = "MongoDB SSH Public Key"
  type        = string
  default     = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQDgHVZh1q7zrrTyPbxvQ+6S40cWso6iVAflxjxNmH8tHx9VJa01cVxQnW8OhA8NOUWj6IN/mOsDS2FrEnmxf32u0lb0C416Fu+PwJHhU8qfFTMtnpGani2vnSjdXztBEUXdNPFWJXwo9/5JNYjZwDQrxWUdMhRaTQ078QsbTfksBNAx2vNpgQ2Tma/P130XNXOgsS+EXK+TXSN5oifQsB4SB5wFQ+/aQH9QWCaVnopZXpJl+rJUf4kMrxRqDEJzhzPQO3jtR511WCeYC95LCrLkuWSu4PwV5Vah5Qc8ckz2yZ8QzsE+JSz/bSplW9pxKny0i5t/1DlNVrQ+vMLLmyD/swtG8NmwOKFvdLIQC3DL+XY5dmt/o1kwy5bn4SzigZloMCp3Z08hdUcLQxjM42NifCFlxZ6Rf9stZN116wWqhP5xLP9v3TOum6ydm9CjTaDxRySH95hDftoX+ltpKKgbgvIhmrs/h8vWpQ1j/+zwFSnlEQVi1NxKYf+pwQ8/qBH6xS7DObROaQKT5tSVqDV4uwrqXnl6yQkub7ptLqV4W7OFeyILAmrVXy81jC3TTCHcBVQgaAcUOuHjLzDMJPQxpjPxkmHwcvvV62/D5cmeLTrwpmXMOhr/2FQQS+B5EOH73LufMwtVKhqOQy6MLbWvRLt4arN5G2AtOtxJc7I8yw== ariel@ariel"
}

variable "influxdb_public_key" {
  description = "InfluxDB SSH Public Key"
  type        = string
  default     = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQCvhNwDsJMaeJrW/d3I2RH+dQuYo8MUGGqlZ/wTLHW8BsD/Tt7kekRKn+CI3hx9ub3QTw+Rakzx84wGNtkwYzczApScPm1AxCoGgkusJNwbqywNnliQ1kRTpNLZjeoBp0YfDVcIbgKYRgM7njBF05kwdjV+OyXqBEFy+dvTlNNl5UHAcLwKocI2BqZpIxbwFt6Wk3cb4TmG+xKd0R433Ts7hLNej3PB+mUkWr5HyDJ6RlDVU+UPJLjn6mHWChAKFXG87g9ccM0L4/pGT++cU2Me6GOWTZ1ScpQKdB8cTbJqVHXDoBtV4S3S4RxTaQrI6P6yZcKCYwxNIj6v1qLrN6WqOlgLnPQYdr7kCVkVcmgRmic47gtEv2y38tmQ6OuIftDoTbeXsrXB+pD6HtkFivQX+yvLFqrAaFPjQTg9v6KLSX2K23jIPdX0C6sigp7P9skRegc1EzaHd3tk1vXhsp0vApcGGhbUqAzkyo6OZEta4adWsFCAzOjQ7rMUr23hhXO/XuNrQvQMylaL+efk074vhSKTL0bBHKLcwWEwLBn7J3TXfvPc6UPuH54RfOIQyWWP14aCihb6HMfoNJjkGDskRgo8PySnnbCjMuHKDIVUYOmLQkN+xs1UEuXU8Cjfkx2PaxDfWt9blBJkmHyrPkEVedm8BE/Y2rdx2MlE8dazNw== ariel@ariel"
}
