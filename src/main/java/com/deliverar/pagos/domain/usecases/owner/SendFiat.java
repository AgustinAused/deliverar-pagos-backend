package com.deliverar.pagos.domain.usecases.owner;

import com.deliverar.pagos.domain.dtos.SendFiatRequest;
import com.deliverar.pagos.domain.dtos.SendFiatResponse;

public interface SendFiat {
    SendFiatResponse sendFiat(SendFiatRequest request);
}
