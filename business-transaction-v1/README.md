# API Business Transaction V1

# Pasos para instalar postgresql en docker

### 1. Ejecutar el siguiente comando para descargar la imagen de postgresql
```bash
docker-compose up -d
```
# Pasos para instalar kafka en docker

### 1. Ejecutar el siguiente comando para descargar la imagen de kafka
```bash
docker-compose up -d
```
### 2. Limpia los volúmenes de datos persistentes: 
Elimina los datos persistidos de Kafka. Dependiendo de cómo configuraste tus volúmenes en docker-compose.yml, esto puede implicar borrar la carpeta que contiene los datos de Kafka.

```bash 
docker-compose down
```
### 3. Esto eliminará los datos persistidos por Kafka, incluidos los archivos de meta.properties.

```bash 
sudo rm -rf ./kafka-data/*
```

# Pasos para Crear base datos en postgresql

### 1. Crear la base de datos
```sql
CREATE SCHEMA IF NOT EXISTS trx;


DROP TABLE IF EXISTS trx.transaction;


CREATE TABLE trx.transaction(id varchar(36) PRIMARY KEY,
                             accountExternalIdDebit varchar(40),
                             accountExternalIdCredit varchar(40),
                             transactionTypeCode varchar(20),
                             amount numeric,
                             status varchar(10),
                             createdAt timestamp,
                             updatedAt timestamp);

GRANT ALL PRIVILEGES ON SCHEMA trx TO postgresuser;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA trx TO postgresuser;
``` 

# Pasos para Crear y Subir una Imagen a Docker Hub

### 1. Crear un `Dockerfile`
Define cómo se debe construir tu imagen. Ejemplo básico para una aplicación Node.js:

```dockerfile
# Usar la imagen base oficial de OpenJDK con Java 17
FROM openjdk:17-jdk-slim

# Establecer el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiar el archivo JAR de la aplicación al directorio de trabajo
COPY target/myapp.jar /app/myapp.jar

# Exponer el puerto en el que la aplicación se ejecutará (cambiar si es necesario)
EXPOSE 8080

# Comando para ejecutar la aplicación Java
ENTRYPOINT ["java", "-jar", "myapp.jar"]

```

### 2. Construir la Imagen
Usa el siguiente comando para construir la imagen:

```bash
docker build -t <tu-usuario>/<nombre-imagen>:<etiqueta> .
```

### 3. Iniciar Sesión en Docker Hub
Inicia sesión en Docker Hub desde la terminal usando el siguiente comando:

```bash
docker login
```
### 4. Subir la Imagen a Docker Hub
Una vez que hayas iniciado sesión, sube la imagen a Docker Hub usando el siguiente comando:

```bash
docker push <tu-usuario>/<nombre-imagen>:<etiqueta>
```
### 5. Verificar la Imagen
Verifica que la imagen se haya subido correctamente iniciando sesión en [Docker Hub](https://hub.docker.com/).

1. Inicia sesión en tu cuenta de Docker Hub.
2. Dirígete a tu perfil o al repositorio donde subiste la imagen.
3. La imagen debería aparecer con el nombre y la etiqueta que especificaste.

### 6. Descargar y Ejecutar la Imagen

Para descargar y ejecutar la imagen en otro entorno, usa los siguientes comandos:

```bash
docker pull <tu-usuario>/<nombre-imagen>:<etiqueta>
docker run -p 4000:8080 -d <tu-usuario>/<nombre-imagen>:<etiqueta>
```

# Pasos para Ejecutar los Planes de `resources_plan` y `replicas_plan` en Kubernetes

### 1. Crear el Archivo `deployment.yaml`
Define el archivo `deployment.yaml` con los recursos de CPU, memoria y configuraciones de JVM:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-application
spec:
  replicas: 1  # Número inicial de réplicas
  selector:
    matchLabels:
      app: my-application
  template:
    metadata:
      labels:
        app: my-application
    spec:
      containers:
        - name: my-container
          image: my-image:latest
          resources:
            limits:
              cpu: "2"                   # cpu_limits
              memory: "1512Mi"            # memory_limits
            requests:
              cpu: "40m"                  # cpu_requests
              memory: "256Mi"             # memory_requests
          env:
            - name: JAVA_OPTS
              value: "-Xms32m -Xmx1024m"  # jvm_xms y jvm_xmx
```

### 2. Crear el Archivo `hpa.yaml`
Define el escalado automático en un archivo `hpa.yaml`:

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: my-application-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: my-application
  minReplicas: 1
  maxReplicas: 4
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 900
    - type: Resource
      resource:
        name: memory
        target:
          type: Utilization
          averageUtilization: 350
```

### 3. Aplicar los Manifiestos YAML
Aplica los archivos `deployment.yaml` y `hpa.yaml` al clúster de Kubernetes con los siguientes comandos:

```bash
kubectl apply -f deployment.yaml
kubectl apply -f hpa.yaml
```
### 4. Verificar el Estado de los Recursos
Comprueba el estado del Deployment y del Horizontal Pod Autoscaler (HPA) para asegurarte de que todo está funcionando correctamente:

```bash
kubectl get deployment my-application
kubectl get hpa my-application-hpa
```

### 5. Monitorear el Escalado
Monitorea cómo el Horizontal Pod Autoscaler (HPA) ajusta las réplicas automáticamente basándose en las métricas de CPU y memoria:

```bash
kubectl describe hpa my-application-hpa
```