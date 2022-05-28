data "aws_region" "current" {}
data "aws_caller_identity" "current" {}

# Policy

resource "aws_iam_policy" "get_ssm_tpa_web" {
  name        = "get-ssm-middleware-${var.env}"
  description = "Permissions to get and describe SSM variables of Middleware ${upper(var.env)}"

  policy = jsonencode({
    Version   = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "ssm:GetParameterHistory",
          "ssm:GetParametersByPath",
          "ssm:GetParameters",
          "ssm:GetParameter"
        ],
        Resource = "arn:aws:ssm:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:parameter/middleware/${var.env}/*"
      },
      {
        Effect   = "Allow",
        Action   = "ssm:DescribeParameters",
        Resource = "*"
      }
    ]
  })
}


# Roles
resource "aws_iam_role" "middleware_ecs" {
  name               = "middleware-ecs-${var.env}"
  assume_role_policy = jsonencode({
    Version   = "2012-10-17",
    Statement = [
      {
        Effect    = "Allow",
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        },
        Action = "sts:AssumeRole"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "ecs" {
  role       = aws_iam_role.middleware_ecs.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role_policy_attachment" "ssm" {
  role       = aws_iam_role.middleware_ecs.name
  policy_arn = aws_iam_policy.get_ssm_tpa_web.arn
}
