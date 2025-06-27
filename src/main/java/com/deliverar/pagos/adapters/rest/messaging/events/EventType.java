package com.deliverar.pagos.adapters.rest.messaging.events;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum EventType {
    // Eventos de Entrada
    TENANT_CREATION_REQUEST("tenant.creado"),
    DELIVERY_USER_CREATED_REQUEST("delivery.nuevoRepartidor"),
    WALLET_CREATION_REQUEST("wallet.creation.request"),

    WALLET_DELETION_REQUEST("wallet.deletion.request"),
    GET_BALANCES_REQUEST("get.balances.request"),
    GET_USER_FIAT_TRANSACTIONS_REQUEST("get.user.fiat.transactions.request"),
    GET_USER_CRYPTO_TRANSACTIONS_REQUEST("get.user.crypto.transactions.request"),
    FIAT_DEPOSIT_REQUEST("fiat.deposit.request"),
    FIAT_WITHDRAWAL_REQUEST("fiat.withdrawal.request"),
    FIAT_PAYMENT_REQUEST("fiat.payment.request"),
    CRYPTO_PAYMENT_REQUEST("crypto.payment.request"),
    BUY_CRYPTO_REQUEST("buy.crypto.request"),
    SELL_CRYPTO_REQUEST("sell.crypto.request"),
    GET_ALL_FIAT_TRANSACTIONS_REQUEST("get.all.fiat.transactions.request"),
    GET_ALL_CRYPTO_TRANSACTIONS_REQUEST("get.all.crypto.transactions.request"),

    // Eventos de Salida
    WALLET_CREATION_RESPONSE("wallet.creation.response"),
    WALLET_DELETION_RESPONSE("wallet.deletion.response"),
    GET_BALANCES_RESPONSE("get.balances.response"),
    GET_USER_FIAT_TRANSACTIONS_RESPONSE("get.user.fiat.transactions.response"),
    GET_USER_CRYPTO_TRANSACTIONS_RESPONSE("get.user.crypto.transactions.response"),
    FIAT_DEPOSIT_RESPONSE("fiat.deposit.response"),
    FIAT_WITHDRAWAL_RESPONSE("fiat.withdrawal.response"),
    FIAT_PAYMENT_RESPONSE("fiat.payment.response"),
    CRYPTO_PAYMENT_RESPONSE("crypto.payment.response"),
    BUY_CRYPTO_RESPONSE("buy.crypto.response"),
    SELL_CRYPTO_RESPONSE("sell.crypto.response"),
    GET_ALL_FIAT_TRANSACTIONS_RESPONSE("get.all.fiat.transactions.response"),
    GET_ALL_CRYPTO_TRANSACTIONS_RESPONSE("get.all.crypto.transactions.response"),
    ERROR_RESPONSE("blockchain.error.response");

    private final String topic;

    public static EventType fromTopic(String topic) {
        for (EventType eventType : values()) {
            if (eventType.topic.equals(topic)) {
                return eventType;
            }
        }
        throw new IllegalArgumentException("Unknown topic: " + topic);
    }
} 