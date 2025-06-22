# Documentación del Proyecto Deliverar Pagos

Este directorio contiene toda la documentación técnica del proyecto, incluyendo arquitectura, diagramas y guías de implementación.

## Archivos de Documentación

### 📋 Arquitectura y Diseño

- **[event-driven-architecture.md](./event-driven-architecture.md)** - Documentación completa de la arquitectura basada en eventos
- **[event-flow-diagram.puml](./event-flow-diagram.puml)** - Diagrama de componentes de la arquitectura (PlantUML)
- **[event-sequence-diagram.puml](./event-sequence-diagram.puml)** - Diagrama de secuencia del flujo de eventos (PlantUML)
- **[event-class-diagram.puml](./event-class-diagram.puml)** - Diagrama de clases de la implementación (PlantUML)
- **[implementation-example.md](./implementation-example.md)** - Ejemplo de implementación con código Java

### 🎯 Patrones de Diseño Implementados

1. **Event-Driven Architecture (EDA)**

   - Event Handler Pattern
   - Event Publisher Pattern
   - Event Router

2. **Command Pattern**

   - Encapsula acciones de negocio
   - Desacopla lógica de eventos

3. **Strategy Pattern**

   - Maneja diferentes tipos de eventos
   - Permite extensibilidad

4. **Observer Pattern**
   - Notifica múltiples componentes
   - Mantiene desacoplamiento

## Cómo Visualizar los Diagramas

### PlantUML

Los archivos `.puml` pueden ser visualizados usando:

1. **PlantUML Online**: https://www.plantuml.com/plantuml/uml/
2. **VS Code Extension**: PlantUML
3. **IntelliJ IDEA Plugin**: PlantUML integration

### Mermaid

Los diagramas en formato Mermaid pueden ser visualizados en:

- GitHub (se renderizan automáticamente)
- Mermaid Live Editor: https://mermaid.live/

## Estructura de Eventos

### Eventos de Entrada (13 eventos)

1. `USER_CREATION_REQUEST` - Creación de usuario
2. `USER_DELETION_REQUEST` - Eliminación de usuario
3. `GET_BALANCES_REQUEST` - Obtener saldos
4. `GET_USER_FIAT_TRANSACTIONS_REQUEST` - Obtener transacciones fiat de usuario
5. `GET_USER_CRYPTO_TRANSACTIONS_REQUEST` - Obtener transacciones crypto de usuario
6. `FIAT_DEPOSIT_REQUEST` - Ingreso de fiat
7. `FIAT_WITHDRAWAL_REQUEST` - Extracción de fiat
8. `FIAT_PAYMENT_REQUEST` - Pago con fiat
9. `CRYPTO_PAYMENT_REQUEST` - Pago con crypto
10. `BUY_CRYPTO_REQUEST` - Compra de crypto
11. `SELL_CRYPTO_REQUEST` - Venta de crypto
12. `GET_ALL_FIAT_TRANSACTIONS_REQUEST` - Obtener transacciones fiat (sin discriminar por usuario)
13. `GET_ALL_CRYPTO_TRANSACTIONS_REQUEST` - Obtener transacciones crypto (sin discriminar por usuario)

### Eventos de Salida (14 eventos)

- `USER_CREATION_RESPONSE`
- `USER_DELETION_RESPONSE`
- `GET_BALANCES_RESPONSE`
- `GET_USER_FIAT_TRANSACTIONS_RESPONSE`
- `GET_USER_CRYPTO_TRANSACTIONS_RESPONSE`
- `FIAT_DEPOSIT_RESPONSE`
- `FIAT_WITHDRAWAL_RESPONSE`
- `FIAT_PAYMENT_RESPONSE`
- `CRYPTO_PAYMENT_RESPONSE`
- `BUY_CRYPTO_RESPONSE`
- `SELL_CRYPTO_RESPONSE`
- `GET_ALL_FIAT_TRANSACTIONS_RESPONSE`
- `GET_ALL_CRYPTO_TRANSACTIONS_RESPONSE`
- `ERROR_RESPONSE`

## Ventajas de la Arquitectura

1. **Desacoplamiento**: Los componentes no dependen directamente entre sí
2. **Escalabilidad**: Fácil agregar nuevos tipos de eventos
3. **Mantenibilidad**: Lógica de negocio separada de infraestructura
4. **Testabilidad**: Cada componente puede ser testeado independientemente
5. **Extensibilidad**: Nuevos handlers pueden ser agregados sin modificar código existente

## Flujo de Procesamiento

1. **Recepción**: El `CallbackController` recibe eventos del Hub externo
2. **Enrutamiento**: El `EventRouter` dirige el evento al handler apropiado
3. **Procesamiento**: El `EventHandler` ejecuta el comando correspondiente
4. **Lógica de Negocio**: El `Command` ejecuta la lógica específica usando los Use Cases existentes
5. **Respuesta**: El `EventPublisher` publica el evento de respuesta al Hub

## Próximos Pasos

1. ✅ Crear documentación de arquitectura
2. ✅ Definir eventos específicos del dominio
3. 🔄 Implementar estructura base de eventos
4. ⏳ Crear handlers para eventos existentes
5. ⏳ Migrar lógica de negocio a comandos
6. ⏳ Implementar sistema de métricas
7. ⏳ Agregar tests unitarios y de integración

## Contribución

Al agregar nueva documentación:

1. Mantén la consistencia en el formato
2. Incluye diagramas cuando sea apropiado
3. Actualiza este README si agregas nuevos archivos
4. Usa PlantUML para diagramas técnicos complejos
5. Usa Mermaid para diagramas simples en markdown
