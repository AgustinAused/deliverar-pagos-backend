package com.deliverar.pagos.domain.entities;

public enum Permission {
    // recurso:operación:scope
    TRANSACTION_READ_OWN,    // consultar propias transacciones
    BALANCE_READ_OWN,        // consultar propio balance
    BALANCE_MODIFY_OWN,     // modificar propio saldo fiat/crypto

    TRANSACTION_READ_ALL,    // consultar todas las transacciones
    BALANCE_READ_ALL,        // consultar todos los balances

    CRYPTO_MINT,             // emitir criptos
    CRYPTO_BURN              // quemar criptos
}
