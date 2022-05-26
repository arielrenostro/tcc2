data "aws_region" "current" {}
data "aws_caller_identity" "current" {}

resource "aws_cloudwatch_log_group" "middleware" {
  name = "/ecs/middleware-${var.env}"

  retention_in_days = var.logs_retention
}

resource "aws_ecs_task_definition" "middleware" {
  family             = "middleware-${var.env}"
  network_mode       = "bridge"
  cpu                = 1024
  memory             = 512
  execution_role_arn = var.role_arn
  task_role_arn      = var.role_arn

  container_definitions = jsonencode([
    {
      name      = "middleware-${var.env}"
      image     = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${data.aws_region.current.name}.amazonaws.com/middleware:latest"
      cpu       = 1024
      memory    = 512
      essential = true

      portMappings = [
        {
          protocol      = "tcp"
          hostPort      = 0
          containerPort = 8080
        }
      ]

      environment = [
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

  lifecycle {
    ignore_changes = [
      cpu,
      memory,
      container_definitions
    ]
  }
}

resource "aws_lb_target_group" "middleware" {
  name        = "middleware-${var.env}"
  target_type = "instance"
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

resource "aws_ecs_service" "middleware" {
  name            = "middleware-${var.env}"
  cluster         = var.cluster.id
  task_definition = aws_ecs_task_definition.middleware.arn
  desired_count   = 2
  launch_type     = "FARGATE"

  load_balancer {
    target_group_arn = aws_lb_target_group.middleware.arn
    container_name   = "middleware-${var.env}"
    container_port   = 8080
  }

  lifecycle {
    ignore_changes = [
      desired_count,
      task_definition,
    ]
  }
}
