package com.testtask.wallet.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class WalletOperation {
    @NotNull
    private UUID valletId;
    @NotNull
    private Operations operationType;
    @Positive
    @NotNull
    private Float amount;
}
