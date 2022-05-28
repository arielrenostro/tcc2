output "id" {
  value = aws_vpc.middleware.id
}

output "subnet_a_private" {
  value = {
    id                = aws_subnet.subnet_middleware_a.id
    availability_zone = aws_subnet.subnet_middleware_a.availability_zone
  }
}

output "subnet_b_private" {
  value = {
    id                = aws_subnet.subnet_middleware_b.id
    availability_zone = aws_subnet.subnet_middleware_b.availability_zone
  }
}

output "subnet_a_public" {
  value = {
    id                = aws_subnet.subnet_middleware_a_public.id
    availability_zone = aws_subnet.subnet_middleware_a_public.availability_zone
  }
}

output "subnet_b_public" {
  value = {
    id                = aws_subnet.subnet_middleware_b_public.id
    availability_zone = aws_subnet.subnet_middleware_b_public.availability_zone
  }
}
