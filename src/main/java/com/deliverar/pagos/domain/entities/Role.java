package com.deliverar.pagos.domain.entities;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum Role {
    CORE(EnumSet.of(
            Permission.TRANSACTION_READ_OWN,
            Permission.BALANCE_READ_OWN,
            Permission.BALANCE_MODIFY_OWN
    )),
    ADMIN(EnumSet.of(
            Permission.CRYPTO_MINT,
            Permission.CRYPTO_BURN,
            Permission.TRANSACTION_READ_ALL,
            Permission.BALANCE_READ_ALL
    )),
    AUDITOR(EnumSet.of(
            Permission.TRANSACTION_READ_ALL,
            Permission.BALANCE_READ_ALL
    )),
    TEST(Collections.unmodifiableSet(
            EnumSet.allOf(Permission.class)
    ));

    private final Set<Permission> UserPermisions;

    public Set<Permission> getPermissions() {
        return UserPermisions;
    }

    public boolean hasPermission(Permission p) {
        return UserPermisions.contains(p);
    }
}
