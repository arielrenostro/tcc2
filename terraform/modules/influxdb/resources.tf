resource "aws_key_pair" "influxdb" {
  key_name = "influxdb-${var.env}"
  public_key = var.public_key
}

resource "aws_instance" "influxdb" {
  ami                         = var.ami
  instance_type               = var.instance_type
  vpc_security_group_ids      = var.security_groups
  subnet_id                   = var.subnet.id
  availability_zone           = var.subnet.availability_zone
  key_name                    = aws_key_pair.influxdb.key_name

  root_block_device {
    delete_on_termination = true
    volume_size           = 20
    volume_type           = "gp2"

    tags = {
      Name = "influxdb-root-${var.env}"
    }
  }

  user_data = <<-EEOOFF
    #!/bin/bash

  EEOOFF

  tags = {
    Name = "influxdb-${var.env}"
  }

  depends_on = [
    aws_key_pair.influxdb
  ]
}

resource "aws_route53_record" "influxdb" {
  zone_id = var.route53.zone_id
  name    = "influxdb.internal.${var.env}.${var.route53.domain}"
  type    = "A"
  ttl     = "300"
  records = [aws_instance.influxdb.private_ip]

  depends_on = [
    aws_instance.influxdb
  ]
}
