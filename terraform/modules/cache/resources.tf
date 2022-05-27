resource "aws_elasticache_subnet_group" "middleware" {
  name       = "subnet-group-middleware"
  subnet_ids = var.subnets
}

resource "aws_elasticache_replication_group" "middleware" {
  automatic_failover_enabled = true
  description                = "cluster-middleware"
  replication_group_id       = "tf-rep-middleware-1"
  node_type                  = "cache.t3.micro"
  num_cache_clusters         = 1
  port                       = 6379
  subnet_group_name          = aws_elasticache_subnet_group.middleware.name
  security_group_ids         = var.security_groups
}

resource "aws_elasticache_cluster" "middleware" {
  cluster_id           = "cluster-middleware"
  replication_group_id = aws_elasticache_replication_group.middleware.id
}

resource "aws_route53_record" "mq" {
  zone_id = var.route53.zone_id
  name    = "cache.${var.env}.${var.route53.domain}"
  type    = "CNAME"
  ttl     = "300"
  records = [aws_elasticache_cluster.middleware.cluster_address]
}
