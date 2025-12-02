# smartvision-platform

Plateforme microservices SmartVision

## Services et Ports

| Service | Port |
|---------|------|
| config-server | 8888 |
| eureka-server | 8761 |
| api-gateway | 8084 |
| video-core | 8085 |
| video-analyzer | 8082 |
| video-storage | 8083 |
## Infrastructure Services
- Nginx Load Balancer: 80
- HAProxy: 81 (HTTP), 1936 (Stats)
- Keycloak (OAuth2): 8080
- Redis: 6379
- MongoDB: 27017
- Zipkin (Tracing): 9411
- Prometheus (Metrics): 9090
- Grafana (Dashboards): 3000

## Security Features
- OAuth2/JWT Authentication
- Role-Based Access Control (RBAC)
- TLS Encryption
- Rate Limiting
- Circuit Breakers
- Request Tracing

## Monitoring
- Prometheus for metrics collection
- Grafana for visualization
- Zipkin for distributed tracing
- Health checks and metrics endpoints

## Démarrage

1. Copier le fichier d'environnement: `cp .env.example .env`
2. Construire les images: `docker-compose build`
3. Démarrer les services: `docker-compose up -d`

# Access services
http://localhost:80          # Nginx Load Balancer
http://localhost:8080        # Keycloak Admin Console
http://localhost:3000        # Grafana Dashboards
http://localhost:9090        # Prometheus
http://localhost:9411        # Zipkin Tracing
```

## Default Credentials
- Keycloak Admin: admin/admin123
- Grafana Admin: admin/admin
- HAProxy Stats: admin/admin
## Tests

```bash
# Lancer les tests avec le profil test
mvn test -Dspring.profiles.active=test
```
