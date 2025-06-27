# 📋 Plan de Refactorización de Commands

## 🎯 Objetivo

Refactorizar todos los commands para seguir el patrón asíncrono obligatorio y eliminar código duplicado.

## 📊 Estado Actual

- **3/7 commands completos** (BuyCryptoCommand, CryptoPaymentCommand, GetBalancesCommand)
- **4/7 commands incompletos** (UserCreationCommand, WalletCreationCommand, FiatDepositCommand, WalletDeletionCommand)

---

## 🔧 TAREAS DE REFACTORIZACIÓN

### **TAREA 1: Crear ResponseBuilder Utility**

**Archivo:** `src/main/java/com/deliverar/pagos/adapters/rest/messaging/commands/utils/ResponseBuilder.java`

**Contexto:**

- Extraer patrón repetido de construcción de respuestas
- Manejar automáticamente traceData
- Métodos estáticos para diferentes tipos de respuesta

**Métodos a implementar:**

```java
public static Map<String, Object> buildResponse(Map<String, Object> originalData, String... fields);
public static void addTraceData(Map<String, Object> response, Map<String, Object> originalData);
public static Map<String, Object> buildErrorResponse(String errorMessage, Map<String, Object> originalData);
```

---

### **TAREA 2: Crear ValidationUtils Utility**

**Archivo:** `src/main/java/com/deliverar/pagos/adapters/rest/messaging/commands/utils/ValidationUtils.java`

**Contexto:**

- Extraer validaciones comunes de owner
- Validación de campos requeridos
- Manejo consistente de errores

**Métodos a implementar:**

```java
public static Owner validateOwnerExists(GetOwnerByEmail useCase, String email);
public static void validateRequiredFields(Map<String, Object> data, String... fields);
public static BigDecimal parseBigDecimal(Map<String, Object> data, String field, BigDecimal defaultValue);
```

---

### **TAREA 3: Crear AsyncBaseCommand**

**Archivo:** `src/main/java/com/deliverar/pagos/adapters/rest/messaging/commands/AsyncBaseCommand.java`

**Contexto:**

- Clase base para commands asíncronos
- Métodos comunes para publicación de eventos
- Manejo estándar de errores

**Métodos a implementar:**

```java
protected void publishSuccessResponse(IncomingEvent originalEvent, EventType responseType, Map<String, Object> data);
protected void publishErrorResponse(IncomingEvent originalEvent, String errorMessage);
protected void processAsync(Runnable asyncTask);
```

---

### **TAREA 4: Refactorizar UserCreationCommand**

**Archivo:** `src/main/java/com/deliverar/pagos/adapters/rest/messaging/commands/strategies/UserCreationCommand.java`

**Contexto:**

- Convertir a patrón asíncrono
- Usar ResponseBuilder y ValidationUtils
- Publicar wallet.creation.response al Hub

**Cambios específicos:**

- Retornar `CommandResult.buildSuccess(null, ...)`
- Usar `CompletableFuture.runAsync()`
- Implementar `processUserCreation()` asíncrono
- Publicar evento usando `EventPublisher`

**Documentación de eventos:**

- Input: `user.creation.request` (name, email, initialFiatBalance, initialCryptoBalance)
- Output: `wallet.creation.response` (name, email, createdAt)

---

### **TAREA 5: Refactorizar WalletCreationCommand**

**Archivo:** `src/main/java/com/deliverar/pagos/adapters/rest/messaging/commands/strategies/WalletCreationCommand.java`

**Contexto:**

- Convertir a patrón asíncrono
- Usar ResponseBuilder y ValidationUtils
- Publicar wallet.creation.response al Hub

**Cambios específicos:**

- Retornar `CommandResult.buildSuccess(null, ...)`
- Usar `CompletableFuture.runAsync()`
- Implementar `processWalletCreation()` asíncrono
- Publicar evento usando `EventPublisher`

**Documentación de eventos:**

- Input: `wallet.creation.request` (name, email, initialFiatBalance, initialCryptoBalance)
- Output: `wallet.creation.response` (name, email, createdAt)

---

### **TAREA 6: Refactorizar FiatDepositCommand**

**Archivo:** `src/main/java/com/deliverar/pagos/adapters/rest/messaging/commands/strategies/FiatDepositCommand.java`

**Contexto:**

- Convertir a patrón asíncrono
- Corregir campos según documentación
- Usar ResponseBuilder y ValidationUtils

**Cambios específicos:**

- Retornar `CommandResult.buildSuccess(null, ...)`
- Usar `CompletableFuture.runAsync()`
- Corregir campos de respuesta: `currentFiatBalance`, `currentCryptoBalance`, `transactionDate`
- Remover campos incorrectos: `new_balance`, `currency`, `concept`, `description`

**Documentación de eventos:**

- Input: `fiat.deposit.request` (email, amount, concept)
- Output: `fiat.deposit.response` (email, amount, concept, status, transactionDate, currentFiatBalance, currentCryptoBalance)

---

### **TAREA 7: Refactorizar WalletDeletionCommand**

**Archivo:** `src/main/java/com/deliverar/pagos/adapters/rest/messaging/commands/strategies/WalletDeletionCommand.java`

**Contexto:**

- Convertir a patrón asíncrono
- Usar use case en lugar de repository
- Corregir campos según documentación
- Usar ResponseBuilder y ValidationUtils

**Cambios específicos:**

- Reemplazar `OwnerRepository` por `GetOwnerByEmail` use case
- Retornar `CommandResult.buildSuccess(null, ...)`
- Usar `CompletableFuture.runAsync()`
- Corregir campos de respuesta según documentación

**Documentación de eventos:**

- Input: `wallet.deletion.request` (email)
- Output: `wallet.deletion.response` (email, deletedAt)

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

### **Para utilities:**

- ✅ Métodos estáticos y reutilizables
- ✅ Manejo consistente de traceData
- ✅ Validaciones robustas
- ✅ Documentación clara

---

## 🚀 ORDEN DE IMPLEMENTACIÓN

1. **TAREA 1** - ResponseBuilder (fundación)
2. **TAREA 2** - ValidationUtils (fundación)
3. **TAREA 3** - AsyncBaseCommand (fundación)
4. **TAREA 4** - UserCreationCommand (más simple)
5. **TAREA 5** - WalletCreationCommand (similar a 4)
6. **TAREA 6** - FiatDepositCommand (campos a corregir)
7. **TAREA 7** - WalletDeletionCommand (más complejo)

---

## ✅ CRITERIOS DE ACEPTACIÓN

- [ ] Todos los commands siguen patrón asíncrono
- [ ] No hay código duplicado entre commands
- [ ] Todos usan utilities compartidas
- [ ] Todos respetan documentación de eventos
- [ ] Todos publican al Hub correctamente
- [ ] Compilación exitosa sin errores
- [ ] Tests pasan (si existen)
