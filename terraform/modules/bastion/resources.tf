resource "aws_key_pair" "bastion" {
  key_name = "bastion-${var.env}"
  public_key = var.public_key
}

resource "aws_instance" "bastion" {
  ami                         = var.ami
  instance_type               = var.instance_type
  vpc_security_group_ids      = var.security_groups
  associate_public_ip_address = true
  subnet_id                   = var.subnet.id
  availability_zone           = var.subnet.availability_zone
  key_name                    = aws_key_pair.bastion.key_name

  root_block_device {
    delete_on_termination = true
    volume_size           = 8
    volume_type           = "gp2"

    tags = {
      Name = "bastion-root-${var.env}"
    }
  }

  tags = {
    Name = "bastion-${var.env}"
  }

  depends_on = [
    aws_key_pair.bastion
  ]
}

resource "aws_route53_record" "bastion" {
  zone_id = var.route53.zone_id
  name    = "bastion.${var.env}.${var.route53.domain}"
  type    = "A"
  ttl     = "300"
  records = [aws_instance.bastion.public_ip]

  depends_on = [
    aws_instance.bastion
  ]
}
