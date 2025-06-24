# Documentación del Proyecto Deliverar Pagos

Este directorio contiene toda la documentación técnica del proyecto, incluyendo arquitectura, diagramas y guías de implementación.

## Archivos de Documentación

### 📋 Arquitectura y Diseño

- **[event-driven-architecture.md](./event-driven-architecture.md)** - Documentación completa de la arquitectura basada en eventosava

### 📝 Especificación de Eventos

- **[events/](./events/)** - Directorio completo con ejemplos JSON de todos los eventos
  - **[events/entrada/](events/input/)** - 13 eventos de entrada con ejemplos JSON
  - **[events/salida/](events/output/)** - 14 eventos de salida con ejemplos JSON

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
