provider "aws" {
  # se existe perfil aws, pode ser utilizado aqui:
  profile             = "ariel"

  # ID da conta AWS
  allowed_account_ids = [806721437369]

  # região da AWS
  region              = "us-east-2"

  default_tags {
    tags = {
      CreatedBy   = "Terraform"
      Application = "Middleware"
    }
  }
}
