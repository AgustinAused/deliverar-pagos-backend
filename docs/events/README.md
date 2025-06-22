# Documentación de Eventos - Deliverar Pagos

Este directorio contiene la especificación completa de todos los eventos de entrada y salida del sistema Deliverar Pagos, organizados en formato JSON con ejemplos detallados.

## Estructura de Directorios

```
events/
├── input/          # Eventos de entrada (13 eventos)
├── output/           # Eventos de salida (14 eventos)
└── README.md         # Esta documentación
```

## Convenciones de Nomenclatura

### Formato de Topics

Los topics siguen el formato **dot notation lowercase**:

- ✅ `user.creation.request`
- ✅ `get.balances.response`
- ✅ `crypto.payment.request`
- ❌ `USER_CREATION_REQUEST`
- ❌ `get_balances_response`

### Estructura de Eventos

Todos los eventos siguen esta estructura base:

```json
{
  "topic": "tipo.accion.request|response",
  "data": {
    // Datos específicos del evento
  }
}
```

## Eventos de Entrada (13 eventos)

### 1. Creación de Usuario

- **Archivo**: `01-user-creation-request.json`
- **Topic**: `user.creation.request`
- **Descripción**: Solicita la creación de un nuevo usuario en el sistema

### 2. Consulta de Saldos

- **Archivo**: `02-get-balances-request.json`
- **Topic**: `get.balances.request`
- **Descripción**: Solicita los saldos fiat y crypto de un usuario

### 3. Pago con Crypto

- **Archivo**: `03-crypto-payment-request.json`
- **Topic**: `crypto.payment.request`
- **Descripción**: Solicita realizar un pago usando crypto entre usuarios

### 4. Compra de Crypto

- **Archivo**: `04-buy-crypto-request.json`
- **Topic**: `buy.crypto.request`
- **Descripción**: Solicita comprar crypto con fiat

### 5. Depósito Fiat

- **Archivo**: `05-fiat-deposit-request.json`
- **Topic**: `fiat.deposit.request`
- **Descripción**: Solicita realizar un depósito de dinero fiat

### 6. Pago con Fiat

- **Archivo**: `06-fiat-payment-request.json`
- **Topic**: `fiat.payment.request`
- **Descripción**: Solicita realizar un pago usando fiat entre usuarios

### 7. Consulta de Transacciones Fiat (Todos)

- **Archivo**: `07-get-all-fiat-transactions-request.json`
- **Topic**: `get.all.fiat.transactions.request`
- **Descripción**: Solicita todas las transacciones fiat del sistema

### 8. Consulta de Transacciones Crypto (Todos)

- **Archivo**: `08-get-all-crypto-transactions-request.json`
- **Topic**: `get.all.crypto.transactions.request`
- **Descripción**: Solicita todas las transacciones crypto del sistema

### 9. Venta de Crypto

- **Archivo**: `09-sell-crypto-request.json`
- **Topic**: `sell.crypto.request`
- **Descripción**: Solicita vender crypto por fiat

### 10. Consulta de Transacciones Fiat de Usuario

- **Archivo**: `10-get-user-fiat-transactions-request.json`
- **Topic**: `get.user.fiat.transactions.request`
- **Descripción**: Solicita las transacciones fiat de un usuario específico

### 11. Consulta de Transacciones Crypto de Usuario

- **Archivo**: `11-get-user-crypto-transactions-request.json`
- **Topic**: `get.user.crypto.transactions.request`
- **Descripción**: Solicita las transacciones crypto de un usuario específico

### 12. Retiro Fiat

- **Archivo**: `12-fiat-withdrawal-request.json`
- **Topic**: `fiat.withdrawal.request`
- **Descripción**: Solicita realizar un retiro de dinero fiat

### 13. Eliminación de Usuario

- **Archivo**: `13-user-deletion-request.json`
- **Topic**: `user.deletion.request`
- **Descripción**: Solicita eliminar un usuario del sistema

## Eventos de Salida (14 eventos)

### 1. Respuesta de Creación de Usuario

- **Archivo**: `01-user-creation-response.json`
- **Topic**: `user.creation.response`
- **Descripción**: Confirma la creación exitosa de un usuario

### 2. Respuesta de Consulta de Saldos

- **Archivo**: `02-get-balances-response.json`
- **Topic**: `get.balances.response`
- **Descripción**: Devuelve los saldos fiat y crypto del usuario

### 3. Respuesta de Pago con Crypto

- **Archivo**: `03-crypto-payment-response.json`
- **Topic**: `crypto.payment.response`
- **Descripción**: Confirma el pago crypto realizado

### 4. Respuesta de Compra de Crypto

- **Archivo**: `04-buy-crypto-response.json`
- **Topic**: `buy.crypto.response`
- **Descripción**: Confirma la compra de crypto realizada

### 5. Respuesta de Depósito Fiat

- **Archivo**: `05-fiat-deposit-response.json`
- **Topic**: `fiat.deposit.response`
- **Descripción**: Confirma el depósito fiat realizado

### 6. Respuesta de Pago con Fiat

- **Archivo**: `06-fiat-payment-response.json`
- **Topic**: `fiat.payment.response`
- **Descripción**: Confirma el pago fiat realizado

### 7. Respuesta de Consulta de Transacciones Fiat (Todos)

- **Archivo**: `07-get-all-fiat-transactions-response.json`
- **Topic**: `get.all.fiat.transactions.response`
- **Descripción**: Devuelve todas las transacciones fiat del sistema

### 8. Respuesta de Consulta de Transacciones Crypto (Todos)

- **Archivo**: `08-get-all-crypto-transactions-response.json`
- **Topic**: `get.all.crypto.transactions.response`
- **Descripción**: Devuelve todas las transacciones crypto del sistema

### 9. Respuesta de Venta de Crypto

- **Archivo**: `09-sell-crypto-response.json`
- **Topic**: `sell.crypto.response`
- **Descripción**: Confirma la venta de crypto realizada

### 10. Respuesta de Consulta de Transacciones Fiat de Usuario

- **Archivo**: `10-get-user-fiat-transactions-response.json`
- **Topic**: `get.user.fiat.transactions.response`
- **Descripción**: Devuelve las transacciones fiat de un usuario específico

### 11. Respuesta de Consulta de Transacciones Crypto de Usuario

- **Archivo**: `11-get-user-crypto-transactions-response.json`
- **Topic**: `get.user.crypto.transactions.response`
- **Descripción**: Devuelve las transacciones crypto de un usuario específico

### 12. Respuesta de Retiro Fiat

- **Archivo**: `12-fiat-withdrawal-response.json`
- **Topic**: `fiat.withdrawal.response`
- **Descripción**: Confirma el retiro fiat realizado

### 13. Respuesta de Eliminación de Usuario

- **Archivo**: `13-user-deletion-response.json`
- **Topic**: `user.deletion.response`
- **Descripción**: Confirma la eliminación exitosa de un usuario

### 14. Respuesta de Error

- **Archivo**: `14-error-response.json`
- **Topic**: `error.response`
- **Descripción**: Notifica errores en el procesamiento de eventos

## Campos Comunes

### Campos de Transacciones

- `transactionId`: Identificador único de la transacción
- `amount`: Monto de la transacción
- `concept`: Concepto o descripción de la transacción
- `status`: Estado de la transacción (SUCCESS, PENDING, FAILURE)
- `transactionDate`: Fecha y hora de la transacción
- `blockchainTxHash`: Hash de la transacción blockchain (solo para crypto)

### Campos de Usuario

- `id`: Identificador único del usuario
- `name`: Nombre completo del usuario
- `email`: Email del usuario (identificador único)
- `role`: Rol del usuario (CORE, ADMIN)
- `createdAt`: Fecha de creación del usuario

### Campos de Error

- `errorCode`: Código de error específico
- `message`: Mensaje descriptivo del error
- `details`: Lista de detalles adicionales del error
- `timestamp`: Fecha y hora del error

## Estados de Transacciones

- **SUCCESS**: Transacción completada exitosamente
- **PENDING**: Transacción en proceso (especialmente para crypto)
- **FAILURE**: Transacción fallida

## Tipos de Transacciones

- **DEPOSIT**: Depósito de dinero
- **WITHDRAWAL**: Retiro de dinero
- **PAYMENT**: Pago entre usuarios
- **BUY**: Compra de crypto
- **SELL**: Venta de crypto

## Uso de los Eventos

1. **Entrada**: Los eventos de entrada son enviados al sistema para solicitar operaciones
2. **Procesamiento**: El sistema procesa la solicitud usando la lógica de negocio existente
3. **Salida**: Los eventos de salida confirman el resultado de la operación
4. **Error**: En caso de error, se envía un evento de error con detalles específicos

## Validaciones

Cada evento incluye validaciones específicas:

- Campos requeridos
- Formatos de datos (email, montos, fechas)
- Reglas de negocio (saldo suficiente, usuario existente, etc.)
- Permisos y roles de usuario
