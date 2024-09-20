package com.testtask.wallet.service;

import com.testtask.wallet.dto.WalletOperation;

import java.util.UUID;

public interface WalletService {
    void walletOperation(WalletOperation walletOperation);

    float walletBalance(UUID id);
}
