# API business-transaction-anti-fraud

## Input Payload

```json
{
  "id": "1",
  "amount": "100",
  "currency": "USD"
}
```

## Output Payload

```json
{
  "transactionId": "1",
  "isFraudulent": false,
  "reason": ""
}
```


# ingress-nginx

**ingress-nginx** es un controlador de Ingress para Kubernetes basado en NGINX. Se utiliza para gestionar el acceso externo a los servicios en un clúster de Kubernetes, proporcionando balanceo de carga, terminación SSL y enrutamiento basado en host y ruta.

## Características principales

- **Balanceo de carga**: Distribuye el tráfico entrante entre múltiples réplicas de un servicio.
- **Terminación SSL**: Maneja la terminación SSL/TLS para asegurar las conexiones.
- **Enrutamiento basado en host y ruta**: Permite definir reglas para enrutar el tráfico a diferentes servicios basados en el host y la ruta de la solicitud.

## Instalación

Para instalar **ingress-nginx** en un clúster de Kubernetes, puedes usar Helm:

```bash
helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update
helm install ingress-nginx ingress-nginx/ingress-nginx --namespace ingress-nginx --create-namespace

