# document-service - despliegue en Kubernetes

El renderer de Playwright/Chromium usa memoria fuera del heap de Java. Por eso el contenedor usa `-XX:MaxRAMPercentage=50` en lugar de 70%.

Para este microservicio se recomienda un límite de memoria de **1.5Gi** y una reserva de al menos **768Mi**. En el Deployment/Helm chart, configura:

```yaml
resources:
  requests:
    cpu: "250m"
    memory: "768Mi"
  limits:
    cpu: "1"
    memory: "1536Mi"
```

No se modifica ningún manifiesto Kubernetes en este ZIP porque el archivo recibido solo contiene `document-service` y `facturacion-service`; aplica estos recursos en el Deployment/values de `document-service` de tu chart.
