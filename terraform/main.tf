terraform {
  backend "local" {
    path = "./state/terraform.tfstate"
  }

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.4"
    }
  }

  required_version = ">= 0.14.9"
}


## Modulos
#module "dns" {
#  source = "./modules/dns"
#
#  domain_name = "ariel-middleware.site"
#}

module "vpc" {
  source = "./modules/vpc"

  env     = var.env
  route53 = var.dns.route53

  vpc_cidr_block             = "172.17.0.0/16"
  subnet_a_cidr_block        = "172.17.0.0/20"
  subnet_a_public_cidr_block = "172.17.128.0/20"
}

module "ecr" {
  source = "./modules/ecr"
}

module "acm" {
  source = "./modules/acm"

  middleware_domain       = "middleware.${var.dns.route53.domain}"
  middleware_alternatives = []
  route53                 = var.dns.route53
}

module "sg" {
  source = "./modules/sg"

  env    = var.env
  vpc_id = module.vpc.id
}

module "ecs" {
  source = "./modules/ecs"

  env = var.env
}

module "role" {
  source = "./modules/role"

  env = var.env
}

module "middleware" {
  source = "./modules/middleware"

  env    = var.env
  vpc_id = module.vpc.id

  alb_id         = module.load_balancer.middleware_alb_id
  cluster        = module.ecs.middleware
  role_arn       = module.role.middleware_ecs.arn
  logs_retention = 5
}

module "load_balancer" {
  source = "./modules/load_balancer"

  env     = var.env
  route53 = var.dns.route53

  middleware_domain           = "middleware.${var.dns.route53.domain}"
  middleware_acm_arn          = module.acm.acm_middleware_arn
  middleware_security_groups  = [module.sg.middleware_lb]
  middleware_subnets          = [module.vpc.subnet_a_public.id]
  middleware_target_group_arn = module.middleware.lb_target
}

module "cache" {
  source = "./modules/cache"

  env             = var.env
  subnets         = [module.vpc.subnet_a.id]
  security_groups = [module.sg.middleware_cache]
}

module "mq" {
  source = "./modules/mq"

  env             = var.env
  instance_type   = "mq.t3.micro"
  username        = "admin"
  password        = "@Batata-1234"
  subnets         = [module.vpc.subnet_a.id]
  security_groups = [module.sg.middleware_mq]
}
