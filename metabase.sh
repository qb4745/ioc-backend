#!/bin/bash
#
# Script de gestión para el entorno de Metabase en Docker.
#
# Uso:
#   ./metabase.sh up     - Inicia los contenedores en segundo plano.
#   ./metabase.sh down   - Detiene y elimina los contenedores (mantiene datos).
#   ./metabase.sh stop   - Detiene los contenedores sin eliminarlos.
#   ./metabase.sh start  - Inicia los contenedores previamente detenidos.
#   ./metabase.sh logs   - Muestra los logs de todos los servicios.
#   ./metabase.sh logs [servicio] - Muestra y sigue los logs de un servicio (ej. metabase).
#   ./metabase.sh ps     - Muestra el estado de los contenedores.
#   ./metabase.sh clean  - Detiene y elimina TODO (contenedores y volúmenes de datos).
#

# --- Carga las variables de entorno desde el archivo .env ---
# Esto asegura que las variables estén disponibles para Docker Compose.
if [ -f .env ]; then
  export $(grep -v '^#' .env | xargs)
fi

# --- Comando principal ---
# Pasa todos los argumentos recibidos por el script a 'docker compose'.
# El comando 'sudo -E' es la clave: preserva las variables de entorno que acabamos de exportar.
sudo -E docker compose "$@"
