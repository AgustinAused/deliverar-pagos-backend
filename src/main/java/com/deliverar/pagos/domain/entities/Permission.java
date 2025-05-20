package com.deliverar.pagos.domain.entities;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum Permission {
    TRANSACTION_READ_OWN("View own transactions"),
    BALANCE_READ_OWN("View own balance"),
    BALANCE_MODIFY_OWN("Modify fiat/crypto balance"),

    TRANSACTION_READ_ALL("View all transactions"),
    BALANCE_READ_ALL("View all balances"),

    CRYPTO_MINT("Issue crypto"),
    CRYPTO_BURN("Burn crypto"),
    USER_CREATION("Create users");

    private final String description;
}
