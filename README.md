# CircleGuard — Sistema de Trazabilidad de Salud Universitaria

Sistema de control de acceso y trazabilidad de contactos para campus universitario, implementado como arquitectura de microservicios con pipelines CI/CD completos.

---

## Entrega Taller 2: Pruebas y Lanzamiento

| Recurso | Enlace |
|---|---|
| Reporte completo con evidencias | [TALLER2_REPORTE.md](TALLER2_REPORTE.md) |
| Video de presentación | https://youtu.be/ORIiPzjQvqI |

---

## Servicios

| Servicio | Puerto | Responsabilidad |
|---|---|---|
| auth-service | 8180 | Autenticación, emisión de JWT y generación de tokens QR |
| identity-service | 8085 | Bóveda criptográfica de identidades reales |
| form-service | 8086 | Recepción y validación de encuestas de síntomas |
| promotion-service | 8088 | Motor de estados de salud sobre grafo Neo4j |
| notification-service | 8082 | Despacho de alertas multi-canal |
| gateway-service | 8087 | Validación de QR en puntos de acceso al campus |

---

## Decisiones Arquitectónicas

### Monorepo Gradle multi-módulo

Todos los servicios viven en un único repositorio con un `build.gradle` raíz que coordina la compilación. Esto simplifica la gestión de versiones de dependencias compartidas y permite ejecutar todos los builds y tests desde un único comando de Gradle. La alternativa (repositorios separados) añadiría overhead de sincronización de versiones sin beneficio real en esta escala.

### Kubernetes local con Kind (Kubernetes in Docker)

Se eligió Kind en lugar de Minikube o k3s por dos razones:

1. **Compatibilidad con Docker Desktop en Windows**: Kind corre los nodos del cluster como contenedores Docker, lo que lo hace nativo al entorno de desarrollo sin requerir una VM adicional.
2. **Soporte multi-nodo liviano**: Permite simular un cluster real con múltiples namespaces sin el overhead de una VM completa.

El cluster es **monocluster** con tres namespaces (`dev`, `stage`, `master`) en lugar de tres clusters separados. Esta decisión reduce el consumo de recursos en el entorno local y es suficiente para demostrar el aislamiento entre ambientes mediante namespaces de Kubernetes.

### Registry Docker local (`localhost:5000`)

Se usa un registry Docker local en lugar de Docker Hub o un registry cloud para eliminar la dependencia de red externa durante el CI. Las imágenes se construyen, etiquetan y consumen dentro del mismo host, haciendo el pipeline reproducible sin credenciales externas. Kind está configurado para que sus nodos confíen en este registry inseguro (HTTP).

### Jenkins en contenedor Docker con socket compartido

Jenkins corre como contenedor Docker con el socket `/var/run/docker.sock` montado. Esto le permite construir imágenes Docker y ejecutar Testcontainers directamente desde el agente de Jenkins sin necesidad de Docker-in-Docker (DinD), que introduce complejidad adicional de red y privilegios. El tradeoff es que Jenkins tiene acceso al daemon Docker del host, lo cual es aceptable en un entorno de desarrollo local.

### Bare repo Git local como fuente de Jenkins

Jenkins lee el código desde un repositorio bare local en `/var/jenkins_home/repos/circleguard.git` en lugar de GitHub. Esto elimina la dependencia de conectividad externa para los pipelines y hace el entorno completamente autónomo. Los cambios se propagan al bare repo mediante `git format-patch` + `git am` desde el clon de trabajo.

### Tres pipelines con responsabilidades distintas

| Pipeline | Pruebas incluidas | Propósito |
|---|---|---|
| `Jenkinsfile.dev` | Unitarias (62) | Feedback rápido por commit |
| `Jenkinsfile.stage` | Unitarias + Integración (26) + E2E (16) | Validación pre-release |
| `Jenkinsfile.master` | Todo + Rendimiento (Locust) | Release versionado con SemVer |

Las pruebas de integración se excluyen de dev con `-PexcludeIntegration` para mantener el ciclo de feedback bajo (< 3 min). Stage y master las ejecutan porque tienen más tiempo disponible y el costo de un fallo allí es mayor.

### Testcontainers con IP bridge en lugar de host mapeado

Las pruebas de integración que usan Testcontainers (Redis, PostgreSQL, Neo4j) conectan al contenedor via su IP en la red bridge de Docker (`172.17.x.x`) en lugar de usar el puerto mapeado en el host. Esto resuelve un problema de compatibilidad con el proxy DNAT de Docker Desktop en Windows, donde `redis.getHost()` retorna null en la JVM forkeada de Gradle.

### Versionado semántico automático (SemVer) en master

El pipeline master calcula la versión analizando los mensajes de commit desde el último tag (`feat:` → minor, `fix:` → patch, `BREAKING CHANGE` → major) siguiendo la convención Conventional Commits. Esto elimina la decisión manual de versión y hace el historial del proyecto legible.

---

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Backend | Spring Boot 3.2 / Java 21 |
| Grafo | Neo4j 5 |
| Relacional | PostgreSQL 16 |
| Cache | Redis 7.2 |
| Mensajería | Apache Kafka |
| CI/CD | Jenkins (contenedor Docker) |
| Orquestación | Kubernetes (Kind) |
| Build | Gradle 8 multi-módulo |
| Tests de carga | Locust 2.28 |
| Tests E2E | Python / pytest |
| Tests de integración | Testcontainers + EmbeddedKafka |

---

## Ejecutar el entorno

Si el entorno se reinició (Docker Desktop reiniciado), restaurar la conectividad con:

```bash
bash scripts/restart-env.sh
```

Jenkins disponible en http://localhost:8090
