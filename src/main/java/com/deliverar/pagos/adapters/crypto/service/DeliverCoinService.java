package com.deliverar.pagos.adapters.crypto.service;

import com.deliverar.pagos.domain.dtos.TransferRequest;
import com.deliverar.pagos.domain.entities.*;
import com.deliverar.pagos.domain.repositories.OwnerRepository;
import com.deliverar.pagos.domain.repositories.TransactionRepository;
import jakarta.annotation.PostConstruct;
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

    public DeliverCoinService(TransactionRepository transactionRepository, OwnerRepository ownerRepository) {
        this.transactionRepository = transactionRepository;
        this.ownerRepository = ownerRepository;
    }

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
        Owner fromOwner = ownerRepository.findByEmail(request.getFromEmail())
                .orElseThrow(() -> new IllegalArgumentException("Owner no encontrado: " + request.getFromEmail()));

        Owner toOwner = ownerRepository.findByEmail(request.getToEmail())
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + request.getToEmail()));

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
            } catch (Exception e) {
                tx.setStatus(TransactionStatus.FAILURE);
                transactionRepository.save(tx);
            }
        });

        return tx.getId();
    }


    public String getEmailByOwnerId(UUID id) {
        return ownerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Owner no encontrado: " + id))
                .getEmail();
    }

    public Transaction getTransferStatus(UUID trackingId) {
        return transactionRepository.findById(trackingId).orElseThrow();
    }

    public TransactionReceipt mint(BigDecimal amount, String toEmail) throws Exception {
        return deliverCoin.mint(toInteger(amount), toEmail).send();
    }

    public TransactionReceipt burn(BigDecimal amount, String fromEmail) throws Exception {
        return deliverCoin.burn(toInteger(amount), fromEmail).send();
    }

    public BigDecimal balanceOf(String email) throws Exception {
        return toDecimal(deliverCoin.balanceOf(email).send());
    }

    public BigDecimal totalSupply() throws Exception {
        return toDecimal(deliverCoin.totalSupply().send());
    }
}
