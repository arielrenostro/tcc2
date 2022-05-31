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
#  domain_name = var.domain
#}

module "vpc" {
  source = "./modules/vpc"

  env     = var.env
  route53 = var.dns.route53

  vpc_cidr_block              = "172.17.0.0/16"
  subnet_a_private_cidr_block = "172.17.0.0/20"
  subnet_b_private_cidr_block = "172.17.32.0/20"
  subnet_a_public_cidr_block  = "172.17.128.0/20"
  subnet_b_public_cidr_block  = "172.17.160.0/20"
}

module "sg" {
  source = "./modules/sg"

  env    = var.env
  vpc_id = module.vpc.id
}

module "bastion" {
  source = "./modules/bastion"

  env     = var.env
  route53 = var.dns.route53

  instance_type   = "t4g.nano"
  ami             = "ami-02cb75f995890cd96"
  subnet          = module.vpc.subnet_a_public
  security_groups = [module.sg.bastion]

  public_key = var.bastion_public_key
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

  subnets_ids = [
    module.vpc.subnet_a_private.id,
    module.vpc.subnet_b_private.id,
  ]
  security_groups = [module.sg.middleware_ecs]

  alb_id         = module.load_balancer.middleware_alb_id
  cluster        = module.ecs.middleware
  role_arn       = module.role.middleware_ecs.arn
  logs_retention = 5
}

module "load_balancer" {
  source = "./modules/load_balancer"

  env     = var.env
  route53 = var.dns.route53

  middleware_domain          = "middleware.${var.dns.route53.domain}"
  middleware_acm_arn         = module.acm.acm_middleware_arn
  middleware_security_groups = [module.sg.middleware_lb]
  middleware_subnets         = [
    module.vpc.subnet_a_public.id,
    module.vpc.subnet_b_public.id,
  ]
  middleware_target_group_arn = module.middleware.lb_target
}

module "cache" {
  source = "./modules/cache"

  env = var.env

  subnets = [
    module.vpc.subnet_a_private.id,
    module.vpc.subnet_b_private.id
  ]
  security_groups = [module.sg.middleware_cache]
}

module "mq" {
  source = "./modules/mq"

  env = var.env

  instance_type = "mq.t3.micro"
  username      = var.mq_username
  password      = var.mq_password
  subnets       = [
    module.vpc.subnet_a_private.id,
    #    module.vpc.subnet_b_private.id,
  ]
  security_groups = [module.sg.middleware_mq]
}

module "parameter_store" {
  source = "./modules/parameter_store"

  env = var.env

  mongodb_host = module.mongodb.host

  cache_write_host = module.cache.write_host
  cache_read_host  = module.cache.read_host

  mq_host     = module.mq.host
  mq_username = module.mq.username
  mq_password = module.mq.password
}

module "mongodb" {
  source = "./modules/mongodb"

  env     = var.env
  route53 = var.dns.route53

  instance_type   = "m6g.large"
  ami             = "ami-02cb75f995890cd96"
  subnet          = module.vpc.subnet_a_private
  security_groups = [module.sg.mongodb]

  public_key = var.mongodb_public_key
}

module "influxdb" {
  source = "./modules/influxdb"

  env     = var.env
  route53 = var.dns.route53

  instance_type   = "t4g.micro"
  ami             = "ami-02cb75f995890cd96"
  subnet          = module.vpc.subnet_a_private
  security_groups = [module.sg.influxdb]

  public_key = var.influxdb_public_key
}
