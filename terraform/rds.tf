# ============================================
# DB SUBNET GROUP (le RDS doit etre dans 2 AZs)
# ============================================
resource "aws_db_subnet_group" "rds" {
  name       = "${var.project_name}-rds-subnet-group"
  subnet_ids = [aws_subnet.private.id, aws_subnet.public.id]

  tags = {
    Name    = "${var.project_name}-rds-subnet-group"
    Project = var.project_name
  }
}

# ============================================
# RDS MySQL
# ============================================
resource "aws_db_instance" "mysql" {
  identifier = "${var.project_name}-mysql"

  # Moteur
  engine         = "mysql"
  engine_version = "8.4"
  instance_class = "db.t3.micro"

  # Base
  db_name  = var.rds_db_name
  username = var.rds_username
  password = var.rds_password

  # Stockage (20 Go = Free Tier)
  allocated_storage = 20
  storage_type      = "gp3"

  # Réseau
  db_subnet_group_name   = aws_db_subnet_group.rds.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  publicly_accessible    = false
  multi_az               = false

  # Backups (0 = pas de backup automatique, pour reduire les couts)
  backup_retention_period = 0
  skip_final_snapshot     = true

  # Pour eviter un echec lors de la suppression
  deletion_protection = false

  tags = {
    Name    = "${var.project_name}-mysql"
    Project = var.project_name
  }
}