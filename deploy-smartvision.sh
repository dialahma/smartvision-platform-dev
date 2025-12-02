#!/usr/bin/env bash
set -euo pipefail

CMD="${1:-}"; [[ -n "${CMD}" ]] && shift || true

CONFIG_REPO_DIR_DEFAULT="${HOME}/smartvision-config-repo-dev"
CONFIG_REPO_BRANCH_DEFAULT="main"

CONFIG_REPO_DIR="${CONFIG_REPO_DIR_DEFAULT}"
CONFIG_REPO_BRANCH="${CONFIG_REPO_BRANCH_DEFAULT}"
FORCE_REBUILD=false

usage() {
  cat <<EOF
 Usage: $(basename "$0") <command> [options]

 Commands:
   build|up|down|logs|restart|status|monitor|update

 Options:
   --config-repo <path>   Path du dépôt Git de configuration (défaut: ${CONFIG_REPO_DIR_DEFAULT})
   --config-branch <name> Branche à utiliser (défaut: ${CONFIG_REPO_BRANCH_DEFAULT})
   --force-rebuild        Force docker compose build
   -h|--help              Aide
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    "--config-repo")
      CONFIG_REPO_DIR="${2:?--config-repo requiert un chemin}"; shift 2 ;;
    "--config-branch")
      CONFIG_REPO_BRANCH="${2:?--config-branch requiert un nom}"; shift 2 ;;
    "--force-rebuild")
      FORCE_REBUILD=true; shift ;;
    "-h|--help")
      usage; exit 0 ;;
    *)
      # passe les options restantes aux sous-commandes (ex: logs <svc>)
      break ;;
  esac
done

# Expo pour docker compose (variables interpolées dans docker-compose.yml)
export CONFIG_REPO_DIR
export CONFIG_REPO_BRANCH

log(){ printf '%s\n' "$*"; }

# Déploiement Docker
deploy_platform() {
  local compose_args=("-d")
  $FORCE_REBUILD && compose_args+=("--build")
  MAX_WAIT=120
  SECONDS_WAITED=0

  log "🚀 Déploiement avec ordre contrôlé..."
  
  # Démarrer d'abord les services essentiels
  log "⏳ Démarrage de MongoDB..."
  docker-compose up -d mongodb
  
  log "⏳ Attente du démarrage de MongoDB..."
  while ! docker-compose exec -T mongodb mongo --eval "db.adminCommand('ping')" >/dev/null 2>&1; do
    sleep 5
  done
  
  if ! git -C "$CONFIG_REPO_DIR" show-ref --verify --quiet refs/heads/main; then
    log "❌ La branche 'main' est absente dans le dépôt $CONFIG_REPO_DIR"
    log "ℹ️ Branche disponible : $(git -C "$CONFIG_REPO_DIR" branch --show-current)"
    exit 1
  fi
  
  log "⏳ Démarrage du Config Server..."
  docker-compose up -d config-server
  
  log "⏳ Attente du Config Server (max ${MAX_WAIT}s)..."
  
  if [ ! -f "$CONFIG_REPO_DIR/api-gateway.yml" ]; then
    echo "❌ Le fichier api-gateway.yml est manquant dans smartvision-config-repo"
    exit 1
  fi

  if ! git -C "$CONFIG_REPO_DIR" rev-parse --verify main > /dev/null 2>&1; then
    echo "❌ La branche 'main' est absente ou incorrecte dans $CONFIG_REPO_DIR"
    exit 1
  fi

  log "⏳ Attente du Config Server..."
  while ! curl -s http://localhost:8888/actuator/health | grep -q '"status":"UP"' || [ $SECONDS_WAITED -ge $MAX_WAIT ]; do
    sleep 5
    SECONDS_WAITED=$((SECONDS_WAITED + 5))
  done
  if [ $SECONDS_WAITED -ge $MAX_WAIT ]; then
    log "⚠️ Timeout atteint. Le Config Server est lent ou instable (status = $(curl -s http://localhost:8888/actuator/health))."
  else
    log "✔️ Config Server opérationnel après ${SECONDS_WAITED}s"
  fi
  
  log "⏳ Démarrage d'Eureka Server..."
  docker-compose up -d eureka-server
  
  log "⏳ Attente d'Eureka Server (max ${MAX_WAIT}s)..."
  while ! curl -s http://localhost:8761/actuator/health | grep -q '"status":"UP"'; do
    sleep 5
  done 
  
  # Démarrer les autres services
  log "⏳ Démarrage des autres services..."
  docker-compose up -d api-gateway video-core video-analyzer video-storage
  
  log "✅ Tous les services ont été démarrés"
}

case "$CMD" in
  build)
    echo "🔨 Building jars and Docker images..."
    for d in config-server eureka-server api-gateway video-core video-analyzer video-storage; do
      echo "➡️  Building $d..."
      (cd "$d" && mvn -q -DskipTests=${SKIP_TESTS:-0} clean package)
    done
    log "🐳 Construction des images Docker..."
    docker-compose build --no-cache --pull || {
    log "❌ Échec de la construction des images Docker"
    exit 1
   }
    # docker compose build --pull
    log "✅ Build completed successfully"
    ;;
  up)
    echo "🚀 Starting services..."
    # docker-compose up -d --remove-orphans
    if ! deploy_platform $CONFIG_REPO_DIR; then
      exit 1
    fi
    echo "🚀 Starting services..."
    docker-compose up -d
    echo "✅ Services started"
    echo "📊 Monitoring: http://localhost:3000"
    echo "🔐 Keycloak: http://localhost:8080"
    echo "📈 Prometheus: http://localhost:9090"
    echo "🔍 Zipkin: http://localhost:9411"
    echo "🌐 API Gateway: http://localhost:8084"
    ;;
  down)
    echo "🛑 Stopping services..."
    docker compose down -v --remove-orphans
    echo "✅ Services stopped"
    ;;
  logs)
    docker compose logs -f --tail=200 "$@"
    ;;
  restart)
    docker compose restart "$@"
    ;;
  status)
    docker compose ps
    ;;
  monitor)
    watch -n 5 'docker compose ps | grep -E "(Up|Exit)"'
    ;;
  update)
    git pull origin main
    ./deploy-smartvision.sh build
    ./deploy-smartvision.sh up
    ;;
  *)
    cat <<EOF
Usage: ./deploy-smartvision.sh [command]

Commands:
  build     - Build all services and Docker images
  up        - Start all services
  down      - Stop and remove all services
  logs      - Show service logs
  restart   - Restart specific services
  status    - Show service status
  monitor   - Monitor services in real-time
  update    - Update and redeploy platform

Environment variables:
  SKIP_TESTS - Set to 1 to skip tests during build
EOF
    ;;
esac
