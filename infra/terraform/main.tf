resource "aws_sqs_queue" "workflow_events" {
  name                       = "${var.project_name}-events"
  visibility_timeout_seconds = 60
}

resource "aws_sns_topic" "workflow_events" {
  name = "${var.project_name}-events"
}

resource "aws_s3_bucket" "audit" {
  bucket = "${var.project_name}-audit-${data.aws_caller_identity.current.account_id}"
}

resource "aws_dynamodb_table" "audit" {
  name         = "${var.project_name}_audit"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "request_id"

  attribute {
    name = "request_id"
    type = "S"
  }
}

resource "aws_cloudwatch_log_group" "workflow" {
  name              = "/aws/treasury/${var.project_name}"
  retention_in_days = 30
}

resource "aws_iam_role" "eks_cluster" {
  name = "${var.project_name}-eks-cluster"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "eks.amazonaws.com"
      }
    }]
  })
}

resource "aws_eks_cluster" "this" {
  name     = var.project_name
  role_arn = aws_iam_role.eks_cluster.arn

  vpc_config {
    subnet_ids = var.private_subnet_ids
  }
}

data "aws_caller_identity" "current" {}
