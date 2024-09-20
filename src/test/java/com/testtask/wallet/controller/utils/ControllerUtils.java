package com.testtask.wallet.controller.utils;

import com.testtask.wallet.entity.Wallet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

final public class ControllerUtils {
    private ControllerUtils() {
    }

    public static List<Wallet> createUniqueWallets(int maxWallets) {
        List<Wallet> wallets = new ArrayList<>();

        List<UUID> uuids = Stream
                .generate(UUID::randomUUID)
                .distinct()
                .limit(maxWallets)
                .toList();
        for (int currentWalletPosition = 0; currentWalletPosition < maxWallets; currentWalletPosition++) {
            float balance = (new Random().nextFloat())*(new Random().nextInt(100));
            //rounding to two digits
            int rounding = 2;
            balance = (float) (Math.ceil(balance * Math.pow(10, rounding)) / Math.pow(10, rounding));

            Wallet currentWallet = new Wallet()
                    .setId(uuids.get(currentWalletPosition))
                    .setBalance(balance);
            wallets.add(currentWallet);
        }

        return wallets;
    }

    public static Wallet getRandomExistedWallet(List<Wallet> wallets) {
        return wallets.get(new Random().nextInt(wallets.size()));
    }

    public static UUID createNonExistedUUID(List<Wallet> wallets) {
        UUID nonExistedUUID;
        List<UUID> uuidList = wallets.stream().map(Wallet::getId).toList();

        do {
            nonExistedUUID = UUID.randomUUID();
        } while (uuidList.contains(nonExistedUUID));

        return nonExistedUUID;
    }
}
