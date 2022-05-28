output "write_host" {
  value = aws_elasticache_replication_group.middleware.primary_endpoint_address
}

output "read_host" {
  value = aws_elasticache_replication_group.middleware.reader_endpoint_address
}
