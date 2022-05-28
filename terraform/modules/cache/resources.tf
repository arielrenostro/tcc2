resource "aws_elasticache_subnet_group" "middleware" {
  name       = "subnet-group-middleware"
  subnet_ids = var.subnets
}

resource "aws_elasticache_replication_group" "middleware" {
  automatic_failover_enabled = false
  description                = "cluster-middleware"
  replication_group_id       = "tf-rep-middleware-1"
  node_type                  = "cache.t3.micro"
  num_cache_clusters         = length(var.subnets)
  port                       = 6379
  subnet_group_name          = aws_elasticache_subnet_group.middleware.name
  security_group_ids         = var.security_groups

  lifecycle {
    ignore_changes = [
      num_cache_clusters
    ]
  }
}

resource "aws_elasticache_cluster" "middleware" {
  cluster_id           = "cluster-middleware"
  replication_group_id = aws_elasticache_replication_group.middleware.id
}
