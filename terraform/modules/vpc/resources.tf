data "aws_region" "current" {}

resource "aws_vpc" "middleware" {
  cidr_block           = var.vpc_cidr_block
  enable_dns_hostnames = true

  tags = {
    Name = "vpc-middleware-${var.env}"
  }
}

resource "aws_internet_gateway" "middleware" {
  vpc_id = aws_vpc.middleware.id

  tags = {
    Name = "ig-middleware-${var.env}"
  }
}

resource "aws_subnet" "subnet_middleware_a" {
  vpc_id                          = aws_vpc.middleware.id
  availability_zone               = "${data.aws_region.current.name}a"
  cidr_block                      = var.subnet_a_private_cidr_block
  assign_ipv6_address_on_creation = false
  ipv6_native                     = false
  enable_dns64                    = false

  tags = {
    Name = "subnet-middleware-a-private-${var.env}"
  }

  depends_on = [
    aws_internet_gateway.middleware,
  ]
}

resource "aws_subnet" "subnet_middleware_b" {
  vpc_id                          = aws_vpc.middleware.id
  availability_zone               = "${data.aws_region.current.name}b"
  cidr_block                      = var.subnet_b_private_cidr_block
  assign_ipv6_address_on_creation = false
  ipv6_native                     = false
  enable_dns64                    = false

  tags = {
    Name = "subnet-middleware-b-private-${var.env}"
  }

  depends_on = [
    aws_internet_gateway.middleware,
  ]
}

resource "aws_subnet" "subnet_middleware_a_public" {
  vpc_id                          = aws_vpc.middleware.id
  availability_zone               = "${data.aws_region.current.name}a"
  cidr_block                      = var.subnet_a_public_cidr_block
  assign_ipv6_address_on_creation = false
  ipv6_native                     = false
  enable_dns64                    = false

  tags = {
    Name = "subnet-middleware-a-public-${var.env}"
  }

  depends_on = [
    aws_internet_gateway.middleware,
  ]
}

resource "aws_subnet" "subnet_middleware_b_public" {
  vpc_id                          = aws_vpc.middleware.id
  availability_zone               = "${data.aws_region.current.name}b"
  cidr_block                      = var.subnet_b_public_cidr_block
  assign_ipv6_address_on_creation = false
  ipv6_native                     = false
  enable_dns64                    = false

  tags = {
    Name = "subnet-middleware-b-public-${var.env}"
  }

  depends_on = [
    aws_internet_gateway.middleware,
  ]
}

resource "aws_eip" "nat" {
  vpc = true

  tags_all = {
    Name = "eip-middleware-nat-${var.env}"
  }
}

resource "aws_nat_gateway" "nat" {
  allocation_id = aws_eip.nat.id
  subnet_id     = aws_subnet.subnet_middleware_a.id

  tags          = {
    Name = "nat-middleware-${var.env}"
  }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.middleware.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.middleware.id
  }

  tags = {
    Name = "rt-middleware-public-${var.env}"
  }

  depends_on = [
    aws_internet_gateway.middleware,
  ]
}

resource "aws_route_table" "private" {
  vpc_id = aws_vpc.middleware.id

  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.nat.id
  }

  tags = {
    Name = "rt-middleware-private-${var.env}"
  }

  depends_on = [
    aws_nat_gateway.nat,
  ]
}

resource "aws_route_table_association" "public_a" {
  route_table_id = aws_route_table.public.id
  subnet_id      = aws_subnet.subnet_middleware_a_public.id
}

resource "aws_route_table_association" "private_a" {
  route_table_id = aws_route_table.private.id
  subnet_id      = aws_subnet.subnet_middleware_a.id
}
