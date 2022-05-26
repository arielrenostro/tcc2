output "id" {
  value = aws_vpc.middleware.id
}

output "subnet_a" {
  value = {
    id                = aws_subnet.subnet_middleware_a.id
    availability_zone = aws_subnet.subnet_middleware_a.availability_zone
  }
}

output "subnet_a_public" {
  value = {
    id                = aws_subnet.subnet_middleware_a_public.id
    availability_zone = aws_subnet.subnet_middleware_a_public.availability_zone
  }
}
