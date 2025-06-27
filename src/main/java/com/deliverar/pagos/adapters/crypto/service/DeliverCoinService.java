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
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.deliverar.pagos.adapters.crypto.service.AmountConverter.toDecimal;
import static com.deliverar.pagos.adapters.crypto.service.AmountConverter.toInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class DeliverCoinService {

    private static final Logger log = LoggerFactory.getLogger(DeliverCoinService.class);

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

    @PersistenceContext
    private EntityManager entityManager;

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

        // Create transaction - let JPA handle timestamps automatically
        Transaction tx = Transaction.builder()
                .id(UUID.randomUUID())
                .originOwner(fromOwner)
                .destinationOwner(toOwner)
                .amount(request.getAmount())
                .currency(CurrencyType.CRYPTO)
                .conversionRate(BigDecimal.ONE)
                .concept("TRANSFER")
                .status(TransactionStatus.PENDING)
                .transactionDate(Instant.now())
                .build();

        transactionRepository.save(tx);

        CompletableFuture.runAsync(() -> {
            try {
                log.info("Starting async blockchain transaction for transfer, transaction ID: {}", tx.getId());
                TransactionReceipt receipt = deliverCoin
                        .transfer(request.getFromEmail(), request.getToEmail(), toInteger(request.getAmount()))
                        .send();
                log.info("Blockchain transaction successful for transfer, transaction ID: {}, hash: {}", tx.getId(), receipt.getTransactionHash());

                // Reload transaction from database to avoid concurrency issues
                Transaction updatedTx = transactionRepository.findById(tx.getId()).orElseThrow();
                updatedTx.setStatus(TransactionStatus.SUCCESS);
                updatedTx.setBlockchainTxHash(receipt.getTransactionHash());
                transactionRepository.save(updatedTx);
                log.info("Transaction status updated to SUCCESS for ID: {}", tx.getId());
                
                ownerRepository.save(fromOwner);
                ownerRepository.save(toOwner);

                // <<< SINCRONIZAR DESPUÉS DE TRANSFERENCIA EXITOSA
                syncBalance(request.getFromEmail());
                syncBalance(request.getToEmail());

            } catch (Exception e) {
                log.error("Error in async blockchain transaction for transfer, transaction ID: {}", tx.getId(), e);
                // Reload transaction from database to avoid concurrency issues
                Transaction updatedTx = transactionRepository.findById(tx.getId()).orElseThrow();
                updatedTx.setStatus(TransactionStatus.FAILURE);
                transactionRepository.save(updatedTx);
                log.info("Transaction status updated to FAILURE for ID: {}", tx.getId());
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

        // Create transaction records - let JPA handle timestamps automatically
        Transaction tx = Transaction.builder()
                .id(UUID.randomUUID())
                .originOwner(ownerAdmin)
                .destinationOwner(buyer)
                .amount(cryptoAmount)
                .currency(CurrencyType.CRYPTO)
                .conversionRate(BigDecimal.ONE)
                .concept(TransactionConcept.BUY_CRYPTO.name())
                .status(TransactionStatus.PENDING)
                .transactionDate(Instant.now())
                .build();

        transactionRepository.save(tx);

        CompletableFuture.runAsync(() -> {
            try {
                log.info("Starting async blockchain transaction for buy crypto, transaction ID: {}", tx.getId());
                TransactionReceipt receipt = deliverCoin.transfer(ownerEmail, email, toInteger(cryptoAmount)).send();
                log.info("Blockchain transaction successful for buy crypto, transaction ID: {}, hash: {}", tx.getId(), receipt.getTransactionHash());
                
                // Reload transaction from database to avoid concurrency issues
                Transaction updatedTx = transactionRepository.findById(tx.getId()).orElseThrow();
                updatedTx.setStatus(TransactionStatus.SUCCESS);
                updatedTx.setBlockchainTxHash(receipt.getTransactionHash());
                transactionRepository.save(updatedTx);
                log.info("Transaction status updated to SUCCESS for ID: {}", tx.getId());
                
                ownerRepository.save(ownerAdmin);
                ownerRepository.save(buyer);

                // <<< SINCRONIZAR DESPUÉS DE COMPRA EXITOSA
                syncBalance(email);
                syncBalance(ownerEmail);

            } catch (Exception e) {
                log.error("Error in async blockchain transaction for buy crypto, transaction ID: {}", tx.getId(), e);
                // Reload transaction from database to avoid concurrency issues
                Transaction updatedTx = transactionRepository.findById(tx.getId()).orElseThrow();
                updatedTx.setStatus(TransactionStatus.FAILURE);
                transactionRepository.save(updatedTx);
                log.info("Transaction status updated to FAILURE for ID: {}", tx.getId());
                
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

    public UUID sellCryptoForFiat(String email, BigDecimal cryptoAmount) throws Exception {
        Owner seller = getOwnerByEmail(email);
        Owner ownerAdmin = getOwnerByEmail(ownerEmail);

        // Verify balance de crypto del vendedor
        if (seller.getWallet().getCryptoBalance().compareTo(cryptoAmount) < 0) {
            throw new BadRequestException("Insufficient crypto balance");
        }

        // Transfer crypto from seller to owner
        seller.getWallet().setCryptoBalance(seller.getWallet().getCryptoBalance().subtract(cryptoAmount));
        ownerAdmin.getWallet().setCryptoBalance(ownerAdmin.getWallet().getCryptoBalance().add(cryptoAmount));

        // Add fiat to seller's wallet (1:1 conversion rate)
        seller.getWallet().setFiatBalance(seller.getWallet().getFiatBalance().add(cryptoAmount));

        // Create transaction record - let JPA handle timestamps automatically
        Transaction tx = Transaction.builder()
                .id(UUID.randomUUID())
                .originOwner(seller)
                .destinationOwner(ownerAdmin)
                .amount(cryptoAmount)
                .currency(CurrencyType.CRYPTO)
                .conversionRate(BigDecimal.ONE)
                .concept(TransactionConcept.SELL_CRYPTO.name())
                .status(TransactionStatus.PENDING)
                .transactionDate(Instant.now())
                .build();

        transactionRepository.save(tx);

        CompletableFuture.runAsync(() -> {
            try {
                log.info("Starting async blockchain transaction for sell crypto, transaction ID: {}", tx.getId());
                TransactionReceipt receipt = deliverCoin.transfer(email, ownerEmail, toInteger(cryptoAmount)).send();
                log.info("Blockchain transaction successful for sell crypto, transaction ID: {}, hash: {}", tx.getId(), receipt.getTransactionHash());
                
                // Reload transaction from database to avoid concurrency issues
                Transaction updatedTx = transactionRepository.findById(tx.getId()).orElseThrow();
                updatedTx.setStatus(TransactionStatus.SUCCESS);
                updatedTx.setBlockchainTxHash(receipt.getTransactionHash());
                transactionRepository.save(updatedTx);
                log.info("Transaction status updated to SUCCESS for ID: {}", tx.getId());
                
                ownerRepository.save(seller);
                ownerRepository.save(ownerAdmin);

                // Sincronizar después de venta exitosa
                syncBalance(email);
                syncBalance(ownerEmail);

            } catch (Exception e) {
                log.error("Error in async blockchain transaction for sell crypto, transaction ID: {}", tx.getId(), e);
                // Reload transaction from database to avoid concurrency issues
                Transaction updatedTx = transactionRepository.findById(tx.getId()).orElseThrow();
                updatedTx.setStatus(TransactionStatus.FAILURE);
                transactionRepository.save(updatedTx);
                log.info("Transaction status updated to FAILURE for ID: {}", tx.getId());
                
                // Revert changes in case of failure
                seller.getWallet().setCryptoBalance(seller.getWallet().getCryptoBalance().add(cryptoAmount));
                seller.getWallet().setFiatBalance(seller.getWallet().getFiatBalance().subtract(cryptoAmount));
                ownerAdmin.getWallet().setCryptoBalance(ownerAdmin.getWallet().getCryptoBalance().subtract(cryptoAmount));
                ownerRepository.save(seller);
                ownerRepository.save(ownerAdmin);
            }
        });
        return tx.getId();
    }

    public Owner getOwnerByEmail(String email) {
        return ownerRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Owner not found: " + email));
    }

    public String getEmailByOwnerId(UUID id) {
        return ownerRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Owner not found: " + id))
                .getEmail();
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public Transaction getTransferStatus(UUID trackingId) {
        // Clear persistence context to force fresh read from database
        entityManager.clear();
        
        // Force a fresh read from database to avoid JPA caching issues
        Transaction transaction = transactionRepository.findById(trackingId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + trackingId));
        log.debug("Retrieved transaction status for ID {}: {}", trackingId, transaction.getStatus());
        return transaction;
    }

    public TransactionReceipt mint(BigDecimal amount) throws Exception {
        Owner ownerAdmin = getOwnerByEmail(ownerEmail);
        ownerAdmin.getWallet().setCryptoBalance(ownerAdmin.getWallet().getCryptoBalance().add(amount));
        ownerRepository.save(ownerAdmin);
        return deliverCoin.mint(toInteger(amount), ownerEmail).send();
    }

    public TransactionReceipt burn(BigDecimal amount) throws Exception {
        Owner ownerAdmin = getOwnerByEmail(ownerEmail);
        ownerAdmin.getWallet().setCryptoBalance(ownerAdmin.getWallet().getCryptoBalance().subtract(amount));
        ownerRepository.save(ownerAdmin);

        return deliverCoin.burn(toInteger(amount), ownerEmail).send();
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
        BigDecimal databaseBalance = owner.getWallet().getCryptoBalance().setScale(2, RoundingMode.HALF_UP);

        if (!blockchainBalance.equals(databaseBalance)) {
            owner.getWallet().setCryptoBalance(blockchainBalance);
            ownerRepository.save(owner);
        }
    }
}
