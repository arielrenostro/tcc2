resource "aws_key_pair" "mongodb" {
  key_name = "mongodb-${var.env}"
  public_key = var.public_key
}

resource "aws_instance" "mongodb" {
  ami                         = var.ami
  instance_type               = var.instance_type
  vpc_security_group_ids      = var.security_groups
  subnet_id                   = var.subnet.id
  availability_zone           = var.subnet.availability_zone
  key_name                    = aws_key_pair.mongodb.key_name

  root_block_device {
    delete_on_termination = true
    volume_size           = 20
    volume_type           = "gp2"

    tags = {
      Name = "mongodb-root-${var.env}"
    }
  }

  user_data = <<-EEOOFF
    #!/bin/bash

    cat <<EOF > /etc/yum.repos.d/mongodb-database-tools-5.0.repo
    [mongodb-database-tools-5.0]
    name=MongoDB Repository
    baseurl=https://repo.mongodb.org/yum/amazon/2/mongodb-org/5.0/arm64/
    gpgcheck=1
    enabled=1
    gpgkey=https://www.mongodb.org/static/pgp/server-5.0.asc
    EOF

    cat <<EOF > /etc/yum.repos.d/mongodb-org-5.0.repo
    [mongodb-org-5.0]
    name=MongoDB Repository
    baseurl=https://repo.mongodb.org/yum/amazon/2/mongodb-org/5.0/aarch64/
    gpgcheck=1
    enabled=1
    gpgkey=https://www.mongodb.org/static/pgp/server-5.0.asc
    EOF

    yum install -y mongodb-org

    sed 's/bindIp: 127.0.0.1/bindIp: 0.0.0.0/g' /etc/mongod.conf > /etc/mongod.conf.new
    yes | mv /etc/mongod.conf.new /etc/mongod.conf

    systemctl enable mongod
    service mongod start
  EEOOFF

  tags = {
    Name = "mongodb-${var.env}"
  }

  depends_on = [
    aws_key_pair.mongodb
  ]
}

resource "aws_route53_record" "mongodb" {
  zone_id = var.route53.zone_id
  name    = "mongodb.internal.${var.env}.${var.route53.domain}"
  type    = "A"
  ttl     = "300"
  records = [aws_instance.mongodb.private_ip]

  depends_on = [
    aws_instance.mongodb
  ]
}
