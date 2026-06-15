variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "project_name" {
  type    = string
  default = "treasury-workflow"
}

variable "vpc_id" {
  type        = string
  description = "Existing VPC for EKS and service resources"
}

variable "private_subnet_ids" {
  type        = list(string)
  description = "Private subnets for EKS node groups"
}
