data "aws_region" "current" {}
data "aws_caller_identity" "current" {}

resource "aws_cloudwatch_log_group" "middleware" {
  name = "/ecs/middleware-${var.env}"

  retention_in_days = var.logs_retention
}

resource "aws_ecs_task_definition" "middleware" {
  family                   = "middleware-${var.env}"
  network_mode             = "awsvpc"
  cpu                      = 1024
  memory                   = 2048
  execution_role_arn       = var.role_arn
  task_role_arn            = var.role_arn
  requires_compatibilities = ["FARGATE"]

  container_definitions = jsonencode([
    {
      name      = "middleware-${var.env}"
      image     = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${data.aws_region.current.name}.amazonaws.com/middleware:latest"
      cpu       = 1024
      memory    = 2048
      essential = true

      portMappings = [
        {
          protocol      = "tcp"
          hostPort      = 8080
          containerPort = 8080
        }
      ]

      environment = [
        {
          name  = "RABBITMQ_PORT"
          value = "5671"
        },
        {
          name  = "RABBITMQ_VHOST"
          value = "/"
        },
        {
          name  = "RABBITMQ_SSL"
          value = "true"
        },
        {
          name  = "RABBITMQ_TIMEOUT_PUBLISH"
          value = "2000"
        },
        {
          name  = "CONSUMERS_TO_SEND"
          value = "10"
        },
        {
          name  = "CACHE_CLIENT_REGISTER_TIMEOUT"
          value = "30000"
        },
        {
          name  = "QUARKUS_MONGODB_DATABASE"
          value = "middleware"
        },
      ]

      secrets = [
        {
          name      = "RABBITMQ_HOST"
          valueFrom = "arn:aws:ssm:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:parameter/middleware/${var.env}/MQ_HOST"
        },
        {
          name      = "RABBITMQ_USERNAME"
          valueFrom = "arn:aws:ssm:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:parameter/middleware/${var.env}/MQ_USERNAME"
        },
        {
          name      = "RABBITMQ_PASSWORD"
          valueFrom = "arn:aws:ssm:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:parameter/middleware/${var.env}/MQ_PASSWORD"
        },
        {
          name      = "QUARKUS_REDIS_HOST"
          valueFrom = "arn:aws:ssm:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:parameter/middleware/${var.env}/CACHE_WRITE_HOST"
        },
        {
          name      = "QUARKUS_MONGODB_CONNECTION_STRING"
          valueFrom = "arn:aws:ssm:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:parameter/middleware/${var.env}/DB_CONNECTION_STRING"
        }
      ]

      logConfiguration = {
        logDriver = "awslogs",
        options   = {
          awslogs-group = "/ecs/middleware-${var.env}",
          "awslogs-region" : data.aws_region.current.name,
          "awslogs-stream-prefix" : "ecs"
        }
      }
    }
  ])
}

resource "aws_ecs_service" "middleware" {
  name            = "middleware-${var.env}"
  cluster         = var.cluster.id
  task_definition = aws_ecs_task_definition.middleware.arn
  desired_count   = 2
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = var.subnets_ids
    security_groups  = var.security_groups
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.middleware.arn
    container_name   = "middleware-${var.env}"
    container_port   = 8080
  }
}

resource "aws_lb_target_group" "middleware" {
  name        = "middleware-${var.env}"
  target_type = "ip"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = var.vpc_id

  health_check {
    enabled             = true
    healthy_threshold   = 2
    unhealthy_threshold = 3
    interval            = 10
    matcher             = "200"
    path                = "/health-check"
    timeout             = 5
  }
}
