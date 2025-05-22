package com.deliverar.pagos.adapters.crypto.service;

import com.deliverar.pagos.domain.dtos.GetCryptoSummaryInfoResponse;
import com.deliverar.pagos.domain.dtos.TransferRequest;
import com.deliverar.pagos.domain.entities.*;
import com.deliverar.pagos.domain.exceptions.BadRequestException;
import com.deliverar.pagos.domain.repositories.OwnerRepository;
import com.deliverar.pagos.domain.repositories.TransactionRepository;
import com.deliverar.pagos.domain.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.deliverar.pagos.adapters.crypto.service.AmountConverter.toDecimal;
import static com.deliverar.pagos.adapters.crypto.service.AmountConverter.toInteger;


@Service
@RequiredArgsConstructor
public class DeliverCoinService {

    @Value("${wallet.private.key}")
    private String privateKey;

    @Value("${web3j.client-address}")
    private String rpcEndpoint;

    @Value("${contract.address}")
    private String contractAddress;

    private DeliverCoin deliverCoin;
    private final TransactionRepository transactionRepository;
    private final OwnerRepository ownerRepository;
    private final UserRepository userRepository;
    @Value("${app.bootstrap.owner.email}")
    private String ownerEmail;

    @PostConstruct
    public void init() {
        Web3j web3j = Web3j.build(new HttpService(rpcEndpoint));
        Credentials credentials = Credentials.create(privateKey);
        deliverCoin = DeliverCoin.load(
                contractAddress,
                web3j,
                credentials,
                new StaticGasProvider(BigInteger.valueOf(2_000_000_000L), BigInteger.valueOf(3_000_000))
        );
    }

    public UUID asyncTransfer(TransferRequest request) {
        Owner fromOwner = getOwnerByEmail(request.getFromEmail());
        Owner toOwner = getOwnerByEmail(request.getToEmail());

        BigDecimal fromOwnerBalance = fromOwner.getWallet().getCryptoBalance();
        if (fromOwnerBalance.compareTo(request.getAmount()) < 0) {
            throw new BadRequestException("Insufficient crypto balance");
        }

        fromOwner.getWallet().setCryptoBalance(fromOwnerBalance.subtract(request.getAmount()));
        toOwner.getWallet().setCryptoBalance(toOwner.getWallet().getCryptoBalance().add(request.getAmount()));

        Transaction tx = new Transaction();
        tx.setId(UUID.randomUUID());
        tx.setOriginOwner(fromOwner);
        tx.setDestinationOwner(toOwner);
        tx.setAmount(request.getAmount());
        tx.setCurrency(CurrencyType.CRYPTO);
        tx.setConversionRate(BigDecimal.ONE);
        tx.setConcept("TRANSFER");
        tx.setStatus(TransactionStatus.PENDING);
        tx.setTransactionDate(Instant.now());
        tx.setCreatedAt(Instant.now());

        transactionRepository.save(tx);

        CompletableFuture.runAsync(() -> {
            try {
                TransactionReceipt receipt = deliverCoin
                        .transfer(request.getFromEmail(), request.getToEmail(), toInteger(request.getAmount()))
                        .send();

                tx.setStatus(TransactionStatus.SUCCESS);
                tx.setBlockchainTxHash(receipt.getTransactionHash());
                transactionRepository.save(tx);
                ownerRepository.save(fromOwner);
                ownerRepository.save(toOwner);

                // <<< SINCRONIZAR DESPUÉS DE TRANSFERENCIA EXITOSA
                syncBalance(request.getFromEmail());
                syncBalance(request.getToEmail());

            } catch (Exception e) {
                tx.setStatus(TransactionStatus.FAILURE);
                transactionRepository.save(tx);
            }
        });

        return tx.getId();
    }

    public UUID buyCryptoWithFiat(String email, BigDecimal cryptoAmount) throws Exception {
        Owner buyer = getOwnerByEmail(email);
        Owner ownerAdmin = getOwnerByEmail(ownerEmail);
        // 1 crypto = 1 fiat

        // Verify balance de fiat del comprador
        if (buyer.getWallet().getFiatBalance().compareTo(cryptoAmount) < 0) {
            throw new BadRequestException("Insufficient fiat balance");
        }

        // Verify balance de crypto del owner
        if (balanceOf(ownerEmail).compareTo(cryptoAmount) < 0) {
            throw new BadRequestException("Insufficient crypto balance in owner wallet");
        }

        // Subtract fiat from buyer wallet's
        buyer.getWallet().setFiatBalance(buyer.getWallet().getFiatBalance().subtract(cryptoAmount));

        // Transfer crypto from owner to buyer
        ownerAdmin.getWallet().setCryptoBalance(ownerAdmin.getWallet().getCryptoBalance().subtract(cryptoAmount));
        buyer.getWallet().setCryptoBalance(buyer.getWallet().getCryptoBalance().add(cryptoAmount));

        // Create transaction records
        Transaction tx = new Transaction();
        tx.setId(UUID.randomUUID());
        tx.setOriginOwner(ownerAdmin);
        tx.setDestinationOwner(buyer);
        tx.setAmount(cryptoAmount);
        tx.setCurrency(CurrencyType.CRYPTO);
        tx.setConversionRate(BigDecimal.ONE);
        tx.setConcept("BUY_CRYPTO");
        tx.setStatus(TransactionStatus.PENDING);
        tx.setTransactionDate(Instant.now());
        tx.setCreatedAt(Instant.now());

        transactionRepository.save(tx);

        CompletableFuture.runAsync(() -> {
            try {
                TransactionReceipt receipt = deliverCoin.transfer(ownerEmail, email, toInteger(cryptoAmount)).send();
                tx.setStatus(TransactionStatus.SUCCESS);
                tx.setBlockchainTxHash(receipt.getTransactionHash());
                transactionRepository.save(tx);
                ownerRepository.save(ownerAdmin);
                ownerRepository.save(buyer);

                // <<< SINCRONIZAR DESPUÉS DE COMPRA EXITOSA
                syncBalance(email);
                syncBalance(ownerEmail);

            } catch (Exception e) {
                tx.setStatus(TransactionStatus.FAILURE);
                transactionRepository.save(tx);
                // Revert changes in case of failure
                buyer.getWallet().setFiatBalance(buyer.getWallet().getFiatBalance().add(cryptoAmount));
                buyer.getWallet().setCryptoBalance(buyer.getWallet().getCryptoBalance().subtract(cryptoAmount));
                ownerAdmin.getWallet().setCryptoBalance(ownerAdmin.getWallet().getCryptoBalance().add(cryptoAmount));
                ownerRepository.save(ownerAdmin);
                ownerRepository.save(buyer);
            }
        });
        return tx.getId();
    }

    public Owner getOwnerByEmail(String email) {
        return ownerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Owner not found: " + email));
    }

    public String getEmailByOwnerId(UUID id) {
        return ownerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Owner not found: " + id))
                .getEmail();
    }

    public Transaction getTransferStatus(UUID trackingId) {
        return transactionRepository.findById(trackingId).orElseThrow();
    }

    public TransactionReceipt mint(BigDecimal amount, String toEmail) throws Exception {
        Owner toOwner = getOwnerByEmail(toEmail);
        toOwner.getWallet().setCryptoBalance(toOwner.getWallet().getCryptoBalance().add(amount));
        ownerRepository.save(toOwner);
        return deliverCoin.mint(toInteger(amount), toEmail).send();
    }

    public TransactionReceipt burn(BigDecimal amount, String fromEmail) throws Exception {
        Owner fromOwner = getOwnerByEmail(fromEmail);
        fromOwner.getWallet().setCryptoBalance(fromOwner.getWallet().getCryptoBalance().subtract(amount));
        ownerRepository.save(fromOwner);

        return deliverCoin.burn(toInteger(amount), fromEmail).send();
    }

    public BigDecimal balanceOf(String email) throws Exception {
        return toDecimal(deliverCoin.balanceOf(email).send());
    }

    public BigDecimal totalSupply() throws Exception {
        return toDecimal(deliverCoin.totalSupply().send());
    }

    public GetCryptoSummaryInfoResponse getCryptoSummaryInfo() throws Exception {
        return GetCryptoSummaryInfoResponse.builder()
                .totalOfTransactions(transactionRepository.count())
                .totalOfOwners(ownerRepository.count())
                .totalOfCryptos(totalSupply())
                .build();
    }

    public void syncBalance(String email) throws Exception {
        Owner owner = getOwnerByEmail(email);
        BigDecimal blockchainBalance = balanceOf(email);
        BigDecimal databaseBalance = owner.getWallet().getCryptoBalance();

        if (!blockchainBalance.equals(databaseBalance)) {
            owner.getWallet().setCryptoBalance(blockchainBalance);
            ownerRepository.save(owner);
        }
    }
}
