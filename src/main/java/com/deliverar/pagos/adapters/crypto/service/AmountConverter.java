package com.deliverar.pagos.adapters.crypto.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class AmountConverter {
    private static final int DECIMALS = 2;
    private static final BigDecimal SCALE_FACTOR = BigDecimal.TEN.pow(DECIMALS);

    /**
     * Convierte un BigDecimal (p. ej. 12.34) a BigInteger (ej. 1234),
     * asegurando que no haya decimales sobrantes.
     */
    public static BigInteger toInteger(BigDecimal amount) {
        BigDecimal scaled = amount.multiply(SCALE_FACTOR)
                .setScale(0, RoundingMode.UNNECESSARY);
        return scaled.toBigIntegerExact();
    }

    /**
     * Convierte un BigInteger (p. ej. 1234) a BigDecimal (12.34),
     * aplicando la escala inversa y definiendo precisión y redondeo.
     */
    public static BigDecimal toDecimal(BigInteger amount) {
        return new BigDecimal(amount)
                .divide(SCALE_FACTOR, DECIMALS, RoundingMode.UNNECESSARY);
    }
}
