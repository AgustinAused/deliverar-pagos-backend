# Event test

1. [x] Delivery user created

````bash
curl --location 'http://localhost:8080/callback' \
--header 'x-topic: delivery.nuevoRepartidor' \
--header 'Content-Type: application/json' \
--data '{
    "name": "Fernanda Salinas",
    "email": "fernanda@example.com"
  }'
````

2. [x]    Delivery wallet deletion

````bash
curl --location 'http://localhost:8080/callback' \
--header 'x-topic: wallet.deletion.request' \
--header 'Content-Type: application/json' \
--data '{
    "email": "fernanda@example.com"
}'
````

3. [x] Tenant created (con saldo inicial de fiat)

````bash
curl --location 'http://localhost:8080/callback' \
--header 'x-topic: tenant.created' \
--header 'Content-Type: application/json' \
--data '{
    "traceData" : {
      "originModule" : "marketplace-service",
      "traceId" : "123123"
    },
    "name": "María Gómez",
    "email": "maria.gomez@example.com",
    "initialFiatBalance": 10000
  }'
````

4. [x] Wallet creation (con saldo inicial de fiat)

````bash
curl --location 'http://localhost:8080/callback' \
--header 'x-topic: wallet.creation.request' \
--header 'Content-Type: application/json' \
--data '{
    "traceData" : {
      "originModule" : "module-name",
      "traceId" : "xxx"
    },
    "name": "Juan Pérez",
    "email": "juan.perez@example.com",
    "initialFiatBalance": 5000
  }'
````

5. [x]    Fiat deposit

```bash
curl --location 'http://localhost:8080/callback' \
--header 'x-topic: fiat.deposit.request' \
--header 'Content-Type: application/json' \
--data '{
    "traceData" : {
      "originModule" : "module-name",
      "traceId" : "xxx"
    },
    "email": "juan.perez@example.com",
    "amount": 500,
    "concept": "Depósito bancario"
}'
```

6. [x]    Fiat Payment

```bash
curl --location 'http://localhost:8080/callback' \
--header 'x-topic: fiat.payment.request' \
--header 'Content-Type: application/json' \
--data '{
    "traceData" : {
      "originModule" : "module-name",
      "traceId" : "xxx"
    },
    "fromEmail": "juan.perez@example.com",
    "toEmail": "maria.gomez@example.com",
    "amount": 250.00,
    "concept": "Pago de factura"
}'
```

7. [x]    Fiat Withdrawal

```bash
curl --location 'http://localhost:8080/callback' \
--header 'x-topic: fiat.withdrawal.request' \
--header 'Content-Type: application/json' \
--data '{
    "traceData" : {
      "originModule" : "module-name",
      "traceId" : "xxx"
    },
    "email": "juan.perez@example.com",
    "amount": 100.00,
    "concept": "Retiro a cuenta bancaria"        
}'
```

8. [x]    Buy Crypto

```bash
curl --location 'http://localhost:8080/callback' \
--header 'x-topic: buy.crypto.request' \
--header 'Content-Type: application/json' \
--data '{
    "traceData" : {
      "originModule" : "module-name",
      "traceId" : "xxx"
    },
    "email": "juan.perez@example.com",
    "amount": 50.00
}'
```

9. [x]    Crypto Payment

```bash
curl --location 'http://localhost:8080/callback' \
--header 'x-topic: crypto.payment.request' \
--header 'Content-Type: application/json' \
--data '{
    "traceData" : {
      "originModule" : "module-name",
      "traceId" : "xxx"
    },
    "fromEmail": "juan.perez@example.com",
    "toEmail": "maria.gomez@example.com",
    "amount": 100.50,
    "concept": "Pago por servicios"    
}'
```

10. [x]    Sell Crypto

```bash
curl --location 'http://localhost:8080/callback' \
--header 'x-topic: sell.crypto.request' \
--header 'Content-Type: application/json' \
--data '{
    "traceData" : {
      "originModule" : "module-name",
      "traceId" : "xxx"
    },
    "email": "juan.perez@example.com",
    "amount": 25.00
}'
```

11. [x]    Get Balances

```bash
curl --location 'http://localhost:8080/callback' \
--header 'x-topic: get.balances.request' \
--header 'Content-Type: application/json' \
--data '{
    "traceData" : {
      "originModule" : "module-name",
      "traceId" : "xxx"
    },
    "email": "juan.perez@example.com"
}'
```

12. [x]    Get User Crypto Transactions

```bash
curl --location 'http://localhost:8080/callback' \
--header 'x-topic: get.user.crypto.transactions.request' \
--header 'Content-Type: application/json' \
--data '{
    "traceData" : {
      "originModule" : "module-name",
      "traceId" : "xxx"
    },
    "email": "juan.perez@example.com",
    "transactionDateSince": "2024-01-01T09:15:00Z"         
}'
```

13. [x]    Get User Fiat Transactions

```bash
curl --location 'http://localhost:8080/callback' \
--header 'x-topic: get.user.fiat.transactions.request' \
--header 'Content-Type: application/json' \
--data '{
    "traceData" : {
      "originModule" : "module-name",
      "traceId" : "xxx"
    },
    "email": "juan.perez@example.com",
    "transactionDateSince": "2024-01-01T09:15:00Z"        
}'
```

14. [x] GetAllCryptoTransactions

```bash
curl --location 'http://localhost:8080/callback' \
--header 'x-topic: get.all.crypto.transactions.request' \
--header 'Content-Type: application/json' \
--data '{
    "traceData" : {
      "originModule" : "module-name",
      "traceId" : "xxx"
    },
    "transactionDateSince": "2024-01-01T09:15:00Z"
  }
}'
```

15. [x] GetAllFiatTransactions

```bash
curl --location 'http://localhost:8080/callback' \
--header 'x-topic: get.all.fiat.transactions.request' \
--header 'Content-Type: application/json' \
--data '{
    "traceData" : {
      "originModule" : "module-name",
      "traceId" : "xxx"
    },
    "transactionDateSince": "2025-06-26T02:58:20.105769Z"      
}'
```

## Commands

1. WalletCreationCommand (client, tenant and marketplace)
2. WalletDeletionCommand
3. BuyCryptoCommand
4. SellCryptoCommand
5. CryptoPaymentCommand
6. FiatDepositCommand
7. FiatPaymentCommand
8. FiatWithdrawalCommand
9. GetBalancesCommand
10. GetUserCryptoTransactionsCommand
11. GetUserFiatTransactionsCommand
12. GetAllCryptoTransactionsCommand
13. GetAllFiatTransactionsCommand