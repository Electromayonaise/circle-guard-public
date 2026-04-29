#!/bin/bash
# Deploys infrastructure (Postgres, Kafka, Redis, Neo4j) to all 3 namespaces.
# Run once before the Jenkins pipelines.
set -e

NAMESPACES="dev stage master"

for NS in $NAMESPACES; do
  echo "=== Deploying infra to namespace: $NS ==="
  kubectl apply -f k8s/infra/postgres.yaml -n "$NS"
  kubectl apply -f k8s/infra/redis.yaml    -n "$NS"
  kubectl apply -f k8s/infra/kafka.yaml    -n "$NS"
  kubectl apply -f k8s/infra/neo4j.yaml    -n "$NS"
done

echo ""
echo "=== Waiting for Postgres to be ready in dev ==="
kubectl rollout status deployment/circleguard-postgres -n dev --timeout=120s

echo "=== Waiting for Redis to be ready in dev ==="
kubectl rollout status deployment/circleguard-redis -n dev --timeout=60s

echo "=== Waiting for Kafka to be ready in dev ==="
kubectl rollout status deployment/circleguard-kafka -n dev --timeout=120s

echo "=== Waiting for Neo4j to be ready in dev ==="
kubectl rollout status deployment/circleguard-neo4j -n dev --timeout=120s

echo ""
echo "Infrastructure deployed to all namespaces."
echo "Run 'kubectl get pods -n dev' to verify."
