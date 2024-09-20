package com.testtask.wallet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Entity
@Table(name = "wallets")
@Data
@Accessors(chain = true)
public class Wallet {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;
    @Column(name = "wallet_balance", nullable = false)
    @Min(0)
    private Float balance;
}
