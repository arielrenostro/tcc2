resource "aws_ssm_parameter" "mq_address" {
  name  = "/middleware/${var.env}/MQ_HOST"
  type  = "String"
  value = var.mq_host
}

resource "aws_ssm_parameter" "mq_username" {
  name  = "/middleware/${var.env}/MQ_USERNAME"
  type  = "String"
  value = var.mq_username
}

resource "aws_ssm_parameter" "mq_password" {
  name  = "/middleware/${var.env}/MQ_PASSWORD"
  type  = "String"
  value = var.mq_password
}

resource "aws_ssm_parameter" "cache_write_host" {
  name  = "/middleware/${var.env}/CACHE_WRITE_HOST"
  type  = "String"
  value = "redis://${var.cache_write_host}:6379"
}

resource "aws_ssm_parameter" "cache_read_host" {
  name  = "/middleware/${var.env}/CACHE_READ_HOST"
  type  = "String"
  value = "redis://${var.cache_read_host}:6379"
}

resource "aws_ssm_parameter" "mongo_host" {
  name  = "/middleware/${var.env}/DB_CONNECTION_STRING"
  type  = "String"
  value = "mongodb://${var.mongodb_host}:27017"
}

resource "aws_ssm_parameter" "influxdb_host" {
  name  = "/middleware/${var.env}/INFLUXDB_CONNECTION_STRING"
  type  = "String"
  value = "http://${var.influxdb_host}:8086"
}
