# 📋 Plan de Implementación de Commands Faltantes

## 🎯 Objetivo

Implementar los 8 commands faltantes siguiendo el patrón asíncrono establecido y respetando la documentación de eventos.

## 📊 Estado Actual

- **7/14 commands implementados** (UserCreation, WalletCreation, GetBalances, CryptoPayment, BuyCrypto, FiatDeposit, WalletDeletion)
- **7/14 commands faltantes** (FiatPayment, GetAllFiatTransactions, GetAllCryptoTransactions, SellCrypto, GetUserFiatTransactions, GetUserCryptoTransactions, FiatWithdrawal, UserDeletion)

---

## 🔧 TAREAS DE IMPLEMENTACIÓN

### **TAREA 1: FiatPaymentCommand** ⏳ **EN PROGRESO**

**Archivo:** `src/main/java/com/deliverar/pagos/adapters/rest/messaging/commands/strategies/FiatPaymentCommand.java`

**Eventos:**

- Input: `fiat.payment.request` (fromEmail, toEmail, amount, concept)
- Output: `fiat.payment.response` (fromEmail, toEmail, amount, concept, status, transactionDate, currentFiatBalance, currentCryptoBalance)

**Use Cases Necesarios:**

- ✅ `GetOwnerByEmail` (para validar fromEmail y toEmail)
- ✅ `ExchangeFiat` (para procesar el pago)

**Características:**

- Patrón asíncrono obligatorio
- Validación de saldo suficiente
- Publicación de evento al Hub
- Respuesta inmediata 204

**Dependencias:**

- ResponseBuilder
- ValidationUtils
- AsyncBaseCommand

---

### **TAREA 2: GetAllFiatTransactionsCommand**

**Archivo:** `src/main/java/com/deliverar/pagos/adapters/rest/messaging/commands/strategies/GetAllFiatTransactionsCommand.java`

**Eventos:**

- Input: `get.all.fiat.transactions.request` (pageNumber, pageSize, sortDirection)
- Output: `get.all.fiat.transactions.response` (transactions[], totalElements, totalPages, currentPage)

**Use Cases Necesarios:**

- ❌ `GetFiatTransactions` (existe pero está paginado, crear nuevo use case)

**Características:**

- Patrón asíncrono obligatorio
- Paginación
- Publicación de evento al Hub
- Respuesta inmediata 204

**Dependencias:**

- ResponseBuilder
- ValidationUtils
- AsyncBaseCommand

---

### **TAREA 3: GetAllCryptoTransactionsCommand**

**Archivo:** `src/main/java/com/deliverar/pagos/adapters/rest/messaging/commands/strategies/GetAllCryptoTransactionsCommand.java`

**Eventos:**

- Input: `get.all.crypto.transactions.request` (pageNumber, pageSize, sortDirection)
- Output: `get.all.crypto.transactions.response` (transactions[], totalElements, totalPages, currentPage)

**Use Cases Necesarios:**

- ❌ `GetTransactions` (existe pero está paginado, crear nuevo use case)

**Características:**

- Patrón asíncrono obligatorio
- Paginación
- Publicación de evento al Hub
- Respuesta inmediata 204

**Dependencias:**

- ResponseBuilder
- ValidationUtils
- AsyncBaseCommand

---

### **TAREA 4: SellCryptoCommand**

**Archivo:** `src/main/java/com/deliverar/pagos/adapters/rest/messaging/commands/strategies/SellCryptoCommand.java`

**Eventos:**

- Input: `sell.crypto.request` (email, amount)
- Output: `sell.crypto.response` (transactionId, email, cryptoAmount, status, blockchainTxHash, transactionDate, currentFiatBalance, currentCryptoBalance)

**Use Cases Necesarios:**

- ✅ `GetOwnerByEmail` (para validar owner)
- ✅ `DeliverCoinService.sellCryptoForFiat` (para vender crypto)

**Características:**

- Patrón asíncrono obligatorio
- Polling hasta estado final (SUCCESS/FAILURE)
- Publicación única al Hub
- Respuesta inmediata 204

**Dependencias:**

- ResponseBuilder
- ValidationUtils
- AsyncBaseCommand

---

### **TAREA 5: GetUserFiatTransactionsCommand**

**Archivo:** `src/main/java/com/deliverar/pagos/adapters/rest/messaging/commands/strategies/GetUserFiatTransactionsCommand.java`

**Eventos:**

- Input: `get.user.fiat.transactions.request` (email, pageNumber, pageSize, sortDirection)
- Output: `get.user.fiat.transactions.response` (email, transactions[], totalElements, totalPages, currentPage)

**Use Cases Necesarios:**

- ✅ `GetOwnerByEmail` (para validar owner)
- ✅ `GetOwnerFiatTransactions` (para obtener transacciones del owner)

**Características:**

- Patrón asíncrono obligatorio
- Paginación
- Publicación de evento al Hub
- Respuesta inmediata 204

**Dependencias:**

- ResponseBuilder
- ValidationUtils
- AsyncBaseCommand

---

### **TAREA 6: GetUserCryptoTransactionsCommand**

**Archivo:** `src/main/java/com/deliverar/pagos/adapters/rest/messaging/commands/strategies/GetUserCryptoTransactionsCommand.java`

**Eventos:**

- Input: `get.user.crypto.transactions.request` (email, pageNumber, pageSize, sortDirection)
- Output: `get.user.crypto.transactions.response` (email, transactions[], totalElements, totalPages, currentPage)

**Use Cases Necesarios:**

- ✅ `GetOwnerByEmail` (para validar owner)
- ✅ `GetOwnerTransactions` (para obtener transacciones del owner)

**Características:**

- Patrón asíncrono obligatorio
- Paginación
- Publicación de evento al Hub
- Respuesta inmediata 204

**Dependencias:**

- ResponseBuilder
- ValidationUtils
- AsyncBaseCommand

---

### **TAREA 7: FiatWithdrawalCommand**

**Archivo:** `src/main/java/com/deliverar/pagos/adapters/rest/messaging/commands/strategies/FiatWithdrawalCommand.java`

**Eventos:**

- Input: `fiat.withdrawal.request` (email, amount, concept)
- Output: `fiat.withdrawal.response` (email, amount, concept, status, transactionDate, currentFiatBalance, currentCryptoBalance)

**Use Cases Necesarios:**

- ✅ `GetOwnerByEmail` (para validar owner)
- ✅ `ExchangeFiat` (para procesar el retiro)

**Características:**

- Patrón asíncrono obligatorio
- Validación de saldo suficiente
- Publicación de evento al Hub
- Respuesta inmediata 204

**Dependencias:**

- ResponseBuilder
- ValidationUtils
- AsyncBaseCommand

---

### **TAREA 8: UserDeletionCommand**

**Archivo:** `src/main/java/com/deliverar/pagos/adapters/rest/messaging/commands/strategies/UserDeletionCommand.java`

**Eventos:**

- Input: `user.deletion.request` (email)
- Output: `user.deletion.response` (email, deletedAt, message)

**Use Cases Necesarios:**

- ✅ `GetOwnerByEmail` (para validar owner)
- ❓ `OwnerRepository` (para eliminar owner)

**Características:**

- Patrón asíncrono obligatorio
- Publicación de evento al Hub
- Respuesta inmediata 204

**Dependencias:**

- ResponseBuilder
- ValidationUtils
- AsyncBaseCommand

---

## ❓ USE CASES FALTANTES IDENTIFICADOS

### **Pregunta 1: GetAllFiatTransactionsCommand**

- **Use Case Necesario:** `GetAllFiatTransactions` (para obtener todas las transacciones fiat sin filtrar por owner)
- **Respuesta:** ✅ Crear nuevo use case

### **Pregunta 2: GetAllCryptoTransactionsCommand**

- **Use Case Necesario:** `GetAllTransactions` (para obtener todas las transacciones crypto sin filtrar por owner)
- **Respuesta:** ✅ Crear nuevo use case

### **Pregunta 3: SellCryptoCommand**

- **Use Case Necesario:** Método en `DeliverCoinService` para vender crypto
- **Respuesta:** ✅ Usar `DeliverCoinService.sellCryptoForFiat`

### **Pregunta 4: UserDeletionCommand**

- **Use Case Necesario:** `DeleteOwner` use case
- **Respuesta:** ❓ Usar directamente `OwnerRepository`

---

## 📋 CRITERIOS DE COMPLETITUD

### **Para cada command:**

- ✅ Retorna `CommandResult.buildSuccess(null, ...)` (respuesta inmediata 204)
- ✅ Usa `CompletableFuture.runAsync()` para procesamiento asíncrono
- ✅ Publica evento al Hub usando `EventPublisher`
- ✅ Usa use cases en lugar de repositories
- ✅ Respeta campos de documentación de eventos
- ✅ Usa utilities (ResponseBuilder, ValidationUtils)
- ✅ Maneja errores asíncronamente
- ✅ Mantiene traceData en todas las respuestas

---

## 🚀 ORDEN DE IMPLEMENTACIÓN RECOMENDADO

1. **TAREA 1** - FiatPaymentCommand (más simple, use cases disponibles) ⏳ **EN PROGRESO**
2. **TAREA 7** - FiatWithdrawalCommand (similar a FiatPayment)
3. **TAREA 5** - GetUserFiatTransactionsCommand (use cases disponibles)
4. **TAREA 6** - GetUserCryptoTransactionsCommand (use cases disponibles)
5. **TAREA 8** - UserDeletionCommand (simple)
6. **TAREA 4** - SellCryptoCommand (use case disponible)
7. **TAREA 2** - GetAllFiatTransactionsCommand (crear use case)
8. **TAREA 3** - GetAllCryptoTransactionsCommand (crear use case)

---

## ✅ CRITERIOS DE ACEPTACIÓN

- [ ] Todos los commands siguen patrón asíncrono
- [ ] No hay código duplicado entre commands
- [ ] Todos usan utilities compartidas
- [ ] Todos respetan documentación de eventos
- [ ] Todos publican al Hub correctamente
- [ ] Compilación exitosa sin errores
- [ ] Tests pasan (si existen)
- [ ] Respuesta inmediata 204 en todos los casos
- [ ] Trazabilidad mantenida con traceData

---

## 🔄 PROCESO DE IMPLEMENTACIÓN

1. **Revisión de use cases** - Confirmar existencia o crear nuevos ✅
2. **Implementación command por command** - Uno a la vez ⏳
3. **Testing individual** - Probar cada command antes de continuar
4. **Refinamiento** - Extraer código común si es necesario
5. **Documentación** - Actualizar documentación si es necesario
