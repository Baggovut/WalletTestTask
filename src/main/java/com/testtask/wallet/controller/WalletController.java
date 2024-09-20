package com.testtask.wallet.controller;

import com.testtask.wallet.dto.WalletOperation;
import com.testtask.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping("/wallet")
    public ResponseEntity<?> walletOperation(@RequestBody @Valid WalletOperation walletOperation) {
        walletService.walletOperation(walletOperation);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/wallets/{WALLET_UUID}")
    public ResponseEntity<Float> walletBalance(@PathVariable(name = "WALLET_UUID") UUID id) {
        Float balance = walletService.walletBalance(id);

        return ResponseEntity.ok().body(balance);
    }
}
