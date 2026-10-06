terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # Backend distant : le State est stocké dans S3 + verrou DynamoDB
  backend "s3" {
    bucket         = "formation-thadeus-terraform-tfstate-2026"
    key            = "projet-pharmacie/terraform.tfstate"
    region         = "eu-north-1"
    dynamodb_table = "terraform-locks"
    encrypt        = true
  }
}

provider "aws" {
  region = var.aws_region
}