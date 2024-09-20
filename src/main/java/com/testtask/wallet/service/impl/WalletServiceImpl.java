package com.testtask.wallet.service.impl;

import com.testtask.wallet.dto.Operations;
import com.testtask.wallet.dto.WalletOperation;
import com.testtask.wallet.entity.Wallet;
import com.testtask.wallet.repository.WalletRepository;
import com.testtask.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepository;

    @Override
    public void walletOperation(WalletOperation walletOperation) {
        Wallet existedWallet = walletRepository.findById(walletOperation.getValletId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );

        if (walletOperation.getOperationType().equals(Operations.DEPOSIT)) {
            existedWallet.setBalance(existedWallet.getBalance() + walletOperation.getAmount());

        } else if (walletOperation.getOperationType().equals(Operations.WITHDRAW)) {
            if (walletOperation.getAmount() > existedWallet.getBalance()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            } else {
                existedWallet.setBalance(existedWallet.getBalance() - walletOperation.getAmount());
            }
        }

        walletRepository.save(existedWallet);
    }

    @Override
    public float walletBalance(UUID id) {
        return walletRepository.findById(id).orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
                )
                .getBalance();
    }
}
