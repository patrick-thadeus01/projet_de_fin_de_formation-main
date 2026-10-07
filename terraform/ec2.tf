# ============================================
# RECHERCHE DYNAMIQUE DE L'AMI AMAZON LINUX 2023
# ============================================
data "aws_ami" "al2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-2023.*-x86_64"]
  }

  filter {
    name   = "state"
    values = ["available"]
  }
}

# ============================================
# IAM ROLE pour EC2 (permet de tirer l'image depuis ECR)
# ============================================
resource "aws_iam_role" "ec2_role" {
  name = "${var.project_name}-ec2-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = { Service = "ec2.amazonaws.com" }
    }]
  })

  tags = {
    Name    = "${var.project_name}-ec2-role"
    Project = var.project_name
  }
}

resource "aws_iam_role_policy_attachment" "ecr_read" {
  role       = aws_iam_role.ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

resource "aws_iam_instance_profile" "ec2_profile" {
  name = "${var.project_name}-ec2-profile"
  role = aws_iam_role.ec2_role.name
}

# ============================================
# EC2 INSTANCE
# ============================================
resource "aws_instance" "app" {
  ami                    = data.aws_ami.al2023.id
  instance_type          = var.ec2_instance_type
  subnet_id              = aws_subnet.public.id
  vpc_security_group_ids = [aws_security_group.ec2.id]
  iam_instance_profile   = aws_iam_instance_profile.ec2_profile.name
  key_name               = "pharmacy-key"

  # Script execute au premier demarrage
  user_data = <<-EOF
    #!/bin/bash
    set -e

    # Log de demarrage
    echo "=== Demarrage du script user_data ===" >> /var/log/user-data.log

    # 1. Installation de Docker
    dnf update -y
    dnf install -y docker
    systemctl start docker
    systemctl enable docker

    # 2. Authentification ECR
    aws ecr get-login-password --region ${var.aws_region} | \
      docker login --username AWS --password-stdin ${var.ecr_image_url}

    # 3. Recuperation de l'image
    docker pull ${var.ecr_image_url}

    # 4. Arret d'un eventuel conteneur existant
    docker rm -f pharmacy-app || true

    # 5. Lancement du conteneur
    docker run -d \
      --name pharmacy-app \
      --restart unless-stopped \
      -p 8080:8080 \
      -e SPRING_DATASOURCE_URL="jdbc:mysql://${aws_db_instance.mysql.address}:3306/${var.rds_db_name}?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC" \
      -e SPRING_DATASOURCE_USERNAME="${var.rds_username}" \
      -e SPRING_DATASOURCE_PASSWORD="${var.rds_password}" \
      -e SPRING_JPA_HIBERNATE_DDL_AUTO="update" \
      -e SPRING_JPA_DATABASE_PLATFORM="org.hibernate.dialect.MySQLDialect" \
      -e JWT_SECRET="${var.jwt_secret}" \
      -e JWT_EXPIRATION="${var.jwt_expiration}" \
      -e APP_ADMIN_USERNAME="${var.admin_username}" \
      -e APP_ADMIN_EMAIL="${var.admin_email}" \
      -e APP_ADMIN_PASSWORD="${var.admin_password}" \
      ${var.ecr_image_url}

    echo "=== Conteneur lance ===" >> /var/log/user-data.log
  EOF

  tags = {
    Name    = "${var.project_name}-ec2"
    Project = var.project_name
  }

  depends_on = [aws_db_instance.mysql]
}