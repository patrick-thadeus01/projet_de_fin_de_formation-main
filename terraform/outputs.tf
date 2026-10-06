output "ec2_public_ip" {
  description = "IP publique de l'EC2 (API)"
  value       = aws_instance.app.public_ip
}

output "rds_endpoint" {
  description = "Endpoint du RDS MySQL"
  value       = aws_db_instance.mysql.address
  sensitive   = true
}

output "api_url" {
  description = "URL de l'API Swagger UI"
  value       = "http://${aws_instance.app.public_ip}:8080/swagger-ui/index.html"
}

output "ssh_command" {
  description = "Commande SSH pour se connecter a l'EC2"
  value       = "ssh -i <chemin-cle.pem> ec2-user@${aws_instance.app.public_ip}"
}