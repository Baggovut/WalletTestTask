package com.testtask.wallet.repository;

import com.testtask.wallet.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends CrudRepository<Wallet, UUID> {
    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Wallet> findById(UUID uuid);

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    <S extends Wallet> S save(S entity);
}
