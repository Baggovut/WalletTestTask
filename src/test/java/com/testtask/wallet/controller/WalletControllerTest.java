package com.testtask.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testtask.wallet.WalletApplication;
import com.testtask.wallet.controller.utils.ControllerUtils;
import com.testtask.wallet.dto.Operations;
import com.testtask.wallet.dto.WalletOperation;
import com.testtask.wallet.entity.Wallet;
import com.testtask.wallet.repository.WalletRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = WalletApplication.class)
@Testcontainers
@AutoConfigureMockMvc
public class WalletControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:alpine");
    private final static int TOTAL_NUMBER_OF_PRE_CREATED_WALLETS = 50;
    private List<Wallet> wallets;

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void beforeEach() throws Exception {
        wallets = ControllerUtils.createUniqueWallets(TOTAL_NUMBER_OF_PRE_CREATED_WALLETS);
        walletRepository.saveAll(wallets);
    }

    @AfterEach
    void afterEach() {
        walletRepository.deleteAll();
    }

    @DisplayName("Check database connection.")
    @Test
    void testPostgresql() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            assertThat(conn).isNotNull();
        }
    }

    @Test
    void walletBalance_getRequest_withExistedWallet_thenValue() throws Exception {
        Wallet existedWallet = ControllerUtils.getRandomExistedWallet(wallets);

        mockMvc.perform(get("/api/v1/wallets/{WALLET_UUID}", existedWallet.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(existedWallet.getBalance().toString()));
    }

    @Test
    void walletBalance_getRequest_withoutExistedWallet_thenNotFound() throws Exception {
        UUID nonExistedUUID = ControllerUtils.createNonExistedUUID(wallets);

        mockMvc.perform(get("/api/v1/wallets/{WALLET_UUID}", nonExistedUUID))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void walletOperation_postRequest_validJson_withoutExistedWallet_thenNotFound() throws Exception {
        UUID nonExistedUUID = ControllerUtils.createNonExistedUUID(wallets);
        String nonExistedWalletJsonValue = objectMapper.writeValueAsString(new WalletOperation()
                .setValletId(nonExistedUUID)
                .setOperationType(Operations.DEPOSIT)
                .setAmount(1f)
        );

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nonExistedWalletJsonValue)
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void walletOperation_postRequest_invalidJson_thenBadRequest() throws Exception {
        WalletOperation walletOperation = new WalletOperation();
        String invalidJson = objectMapper.writeValueAsString(walletOperation);

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());


        UUID existedUUID = ControllerUtils.getRandomExistedWallet(wallets).getId();
        walletOperation.setValletId(existedUUID);
        invalidJson = objectMapper.writeValueAsString(walletOperation);

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());


        walletOperation.setOperationType(Operations.DEPOSIT);
        invalidJson = objectMapper.writeValueAsString(walletOperation);

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());


        walletOperation.setAmount(-10f);
        invalidJson = objectMapper.writeValueAsString(walletOperation);

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());

    }

    @Test
    @Transactional
    void walletOperation_postRequest_validJson_deposit_thenOK() throws Exception {
        Wallet existedWallet = ControllerUtils.getRandomExistedWallet(wallets);
        WalletOperation walletOperation = new WalletOperation()
                .setValletId(existedWallet.getId())
                .setOperationType(Operations.DEPOSIT)
                .setAmount(10f);

        String validJson = objectMapper.writeValueAsString(walletOperation);

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson)
                )
                .andDo(print())
                .andExpect(status().isOk());

        Wallet modifiedWallet = walletRepository.findById(existedWallet.getId()).orElseThrow();

        assertEquals((existedWallet.getBalance() + walletOperation.getAmount()), modifiedWallet.getBalance());
    }

    @Test
    @Transactional
    void walletOperation_postRequest_validJson_withdraw_thenOK() throws Exception {
        Wallet existedWallet = ControllerUtils.getRandomExistedWallet(wallets);
        WalletOperation walletOperation = new WalletOperation()
                .setValletId(existedWallet.getId())
                .setOperationType(Operations.WITHDRAW)
                .setAmount(existedWallet.getBalance() / 10);

        String validJson = objectMapper.writeValueAsString(walletOperation);

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson)
                )
                .andDo(print())
                .andExpect(status().isOk());

        Wallet modifiedWallet = walletRepository.findById(existedWallet.getId()).orElseThrow();

        assertEquals((existedWallet.getBalance() - walletOperation.getAmount()), modifiedWallet.getBalance());
    }

    @Test
    void walletOperation_postRequest_validJson_withdraw_overdraft_thenBadRequest() throws Exception {
        Wallet existedWallet = ControllerUtils.getRandomExistedWallet(wallets);
        WalletOperation walletOperation = new WalletOperation()
                .setValletId(existedWallet.getId())
                .setOperationType(Operations.WITHDRAW)
                .setAmount(existedWallet.getBalance() * 10);

        String validJson = objectMapper.writeValueAsString(walletOperation);

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
