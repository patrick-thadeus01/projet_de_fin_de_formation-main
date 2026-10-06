variable "aws_region" {
  description = "Région AWS où déployer l'infrastructure"
  type        = string
  default     = "eu-north-1"
}

variable "project_name" {
  description = "Nom du projet (utilisé comme préfixe pour toutes les ressources)"
  type        = string
  default     = "pharmacy"
}

variable "vpc_cidr" {
  description = "CIDR du VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidr" {
  description = "CIDR du sous-réseau public (EC2)"
  type        = string
  default     = "10.0.1.0/24"
}

variable "private_subnet_cidr" {
  description = "CIDR du sous-réseau privé (RDS)"
  type        = string
  default     = "10.0.2.0/24"
}

variable "rds_db_name" {
  description = "Nom de la base de données MySQL"
  type        = string
  default     = "pharmacydb"
}

variable "rds_username" {
  description = "Utilisateur MySQL RDS"
  type        = string
  default     = "admin"
}

variable "rds_password" {
  description = "Mot de passe MySQL RDS (à définir dans terraform.tfvars)"
  type        = string
  sensitive   = true
}

variable "ec2_instance_type" {
  description = "Type d'instance EC2 (Free Tier)"
  type        = string
  default     = "t3.micro"
}

variable "ecr_image_url" {
  description = "URL complète de l'image Docker sur ECR"
  type        = string
}

variable "allowed_ssh_cidr" {
  description = "CIDR autorisé pour SSH (ton IP publique)"
  type        = string
}

variable "jwt_secret" {
  description = "Secret JWT de l'application"
  type        = string
  sensitive   = true
}

variable "jwt_expiration" {
  description = "Durée d'expiration du token JWT en millisecondes"
  type        = string
  default     = "1200000"
}

variable "admin_username" {
  description = "Nom d'utilisateur de l'admin créé au démarrage"
  type        = string
  default     = "admin"
}

variable "admin_email" {
  description = "Email de l'admin"
  type        = string
  default     = "admin@pharmacy.local"
}

variable "admin_password" {
  description = "Mot de passe admin créé au démarrage"
  type        = string
  sensitive   = true
}