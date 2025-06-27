package com.deliverar.pagos.infrastructure.bootstrap;

import com.deliverar.pagos.adapters.crypto.service.DeliverCoinService;
import com.deliverar.pagos.domain.entities.*;
import com.deliverar.pagos.domain.repositories.OwnerRepository;
import com.deliverar.pagos.domain.usecases.owner.CreateOwner;
import com.deliverar.pagos.domain.usecases.owner.impl.DefaultCreateOwner;
import com.deliverar.pagos.domain.usecases.owner.impl.DefaultExchangeFiat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
@RequiredArgsConstructor
public class OwnerBootstrapRunner implements CommandLineRunner {

    private final CreateOwner ownerUseCase;
    private final DeliverCoinService deliverCoinService;
    private final OwnerRepository ownerRepo;
    @Value("${app.bootstrap.owner.email}")
    private String ownerEmail;
    @Value("${app.bootstrap.owner.name}")
    private String ownerName;

    @Override
    public void run(String... args) {
        if (ownerRepo.findByEmail(ownerEmail).isEmpty()) {
            BigDecimal cryptoBalance = BigDecimal.ZERO;
            try {
                cryptoBalance = deliverCoinService.balanceOf(ownerEmail);
            } catch (Exception e) {
                log.error("Error getting the crypto balance of owner. Error message: {}", e.getMessage());
            }
            Owner owner = ownerUseCase.create(ownerName, ownerEmail, OwnerType.LEGAL, BigDecimal.valueOf(1000000000), cryptoBalance);
            log.info("Owner created: {}", owner);
        }
    }
}
