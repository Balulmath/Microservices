output "sqs_queue_url" {
  value = aws_sqs_queue.workflow_events.url
}

output "sns_topic_arn" {
  value = aws_sns_topic.workflow_events.arn
}

output "audit_bucket" {
  value = aws_s3_bucket.audit.bucket
}

output "dynamodb_table" {
  value = aws_dynamodb_table.audit.name
}

output "eks_cluster_name" {
  value = aws_eks_cluster.this.name
}
