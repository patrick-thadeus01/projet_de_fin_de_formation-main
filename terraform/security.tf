# ============================================
# SECURITY GROUP - EC2 (API Spring Boot)
# ============================================
resource "aws_security_group" "ec2" {
  name        = "${var.project_name}-ec2-sg"
  description = "Security group pour EC2 (API Spring Boot)"
  vpc_id      = aws_vpc.main.id

  # HTTP depuis Internet (l'API est publique)
  ingress {
    description = "API HTTP depuis Internet"
    from_port   = 8080
    to_port     = 8080
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # SSH restreint a une IP unique
  ingress {
    description = "SSH depuis mon IP uniquement"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.allowed_ssh_cidr]
  }

  # Sortie : tout autorise
  egress {
    description = "Tout le trafic sortant"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name    = "${var.project_name}-ec2-sg"
    Project = var.project_name
  }
}

# ============================================
# SECURITY GROUP - RDS (MySQL)
# ============================================
resource "aws_security_group" "rds" {
  name        = "${var.project_name}-rds-sg"
  description = "Security group pour RDS MySQL"
  vpc_id      = aws_vpc.main.id

  # MySQL accessible uniquement depuis le SG de EC2
  ingress {
    description     = "MySQL depuis EC2 uniquement"
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.ec2.id]
  }

  # Sortie : tout autorise
  egress {
    description = "Tout le trafic sortant"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name    = "${var.project_name}-rds-sg"
    Project = var.project_name
  }
}