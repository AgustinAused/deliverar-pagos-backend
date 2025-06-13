package com.deliverar.pagos.domain.usecases.owner.impl;

import com.deliverar.pagos.domain.dtos.SendFiatRequest;
import com.deliverar.pagos.domain.dtos.SendFiatResponse;
import com.deliverar.pagos.domain.usecases.owner.SendFiat;
import com.deliverar.pagos.domain.entities.Owner;
import com.deliverar.pagos.domain.entities.Wallet;
import com.deliverar.pagos.domain.entities.FiatTransaction;
import com.deliverar.pagos.domain.entities.FiatTransactionConcept;
import com.deliverar.pagos.domain.entities.CurrencyType;
import com.deliverar.pagos.domain.entities.TransactionStatus;
import com.deliverar.pagos.domain.repositories.OwnerRepository;
import com.deliverar.pagos.domain.repositories.FiatTransactionRepository;
import com.deliverar.pagos.domain.exceptions.BadRequestException;
import com.deliverar.pagos.domain.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class DefaultSendFiat implements SendFiat {
    private final OwnerRepository ownerRepository;
    private final FiatTransactionRepository fiatTransactionRepository;

    @Override
    @Transactional
    public SendFiatResponse sendFiat(SendFiatRequest request) {
        if (request.getSenderEmail() == null || request.getRecipientEmail() == null || request.getAmount() == null) {
            throw new BadRequestException("Sender, recipient, and amount are required");
        }
        if (request.getSenderEmail().equalsIgnoreCase(request.getRecipientEmail())) {
            throw new BadRequestException("Sender and recipient must be different");
        }
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be positive");
        }
        Owner sender = ownerRepository.findByEmail(request.getSenderEmail())
                .orElseThrow(() -> new NotFoundException("Sender not found: " + request.getSenderEmail()));
        Owner recipient = ownerRepository.findByEmail(request.getRecipientEmail())
                .orElseThrow(() -> new NotFoundException("Recipient not found: " + request.getRecipientEmail()));
        Wallet senderWallet = sender.getWallet();
        Wallet recipientWallet = recipient.getWallet();
        if (senderWallet.getFiatBalance().compareTo(request.getAmount()) < 0) {
            throw new BadRequestException("Sender has insufficient fiat balance");
        }
        senderWallet.setFiatBalance(senderWallet.getFiatBalance().subtract(request.getAmount()));
        recipientWallet.setFiatBalance(recipientWallet.getFiatBalance().add(request.getAmount()));
        ownerRepository.save(sender);
        ownerRepository.save(recipient);
        FiatTransaction senderTx = FiatTransaction.builder()
                .owner(sender)
                .amount(request.getAmount().negate())
                .currency(CurrencyType.FIAT)
                .concept(FiatTransactionConcept.TRANSFER)
                .transactionDate(Instant.now())
                .status(TransactionStatus.SUCCESS)
                .build();
        FiatTransaction recipientTx = FiatTransaction.builder()
                .owner(recipient)
                .amount(request.getAmount())
                .currency(CurrencyType.FIAT)
                .concept(FiatTransactionConcept.TRANSFER)
                .transactionDate(Instant.now())
                .status(TransactionStatus.SUCCESS)
                .build();
        fiatTransactionRepository.save(senderTx);
        fiatTransactionRepository.save(recipientTx);
        return SendFiatResponse.builder()
                .status("success")
                .message("Fiat sent successfully")
                .build();
    }
}
