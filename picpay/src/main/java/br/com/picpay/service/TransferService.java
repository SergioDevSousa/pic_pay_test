package br.com.picpay.service;

import br.com.picpay.dto.request.TransferRequest;
import br.com.picpay.dto.response.TransferResponse;
import br.com.picpay.entity.Transaction;
import br.com.picpay.entity.User;
import br.com.picpay.enums.UserType;
import br.com.picpay.exception.BusinessException;
import br.com.picpay.exception.ResourceNotFoundException;
import br.com.picpay.repository.TransactionRepository;
import br.com.picpay.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final AuthorizationService authorizationService;
    private final NotificationService notificationService;

    public TransferService(UserRepository userRepository, TransactionRepository transactionRepository,
                        AuthorizationService authorizationService, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.authorizationService = authorizationService;
        this.notificationService = notificationService;
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        if (request.payer().equals(request.payee()))
            throw new BusinessException("Pagador e recebedor devem ser diferentes");

        User payer = userRepository.findByIdForUpdate(request.payer())
                .orElseThrow(() -> new ResourceNotFoundException("Pagador não encontrado"));
        User payee = userRepository.findByIdForUpdate(request.payee())
                .orElseThrow(() -> new ResourceNotFoundException("Recebedor não encontrado"));

        if (payer.getUserType() == UserType.MERCHANT)
            throw new BusinessException("Lojistas não podem realizar transferências");
        if (payer.getBalance().compareTo(request.value()) < 0)
            throw new BusinessException("Saldo insuficiente");
        if (!authorizationService.authorize())
            throw new BusinessException("Transferência não autorizada");

        payer.debit(request.value());
        payee.credit(request.value());
        Transaction transaction = transactionRepository.save(new Transaction(request.value(), payer, payee));
        notificationService.notify(transaction);

        return new TransferResponse(transaction.getId(), transaction.getValue(), payer.getId(),
                payee.getId(), transaction.getCreatedAt());
    }
}
