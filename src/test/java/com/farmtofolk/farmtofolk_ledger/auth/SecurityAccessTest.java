package com.farmtofolk.farmtofolk_ledger.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAccessTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;

    @BeforeEach
    void clearUsers() {
        userRepository.deleteAll();
    }

    @Test
    void publicTraceDoesNotRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/public/trace/missing-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    void protectedApiRejectsMissingToken() throws Exception {
        mockMvc.perform(get("/api/farmers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void farmerCannotUseFieldOfficerListApi() throws Exception {
        User farmer = new User();
        farmer.setName("Farmer");
        farmer.setPhone("9876543210");
        farmer.setPasswordHash(passwordEncoder.encode("password123"));
        farmer.setRole(UserRole.FARMER);
        farmer.setActive(true);
        farmer = userRepository.save(farmer);

        mockMvc.perform(get("/api/farmers")
                        .header("Authorization", "Bearer " + jwtService.generateToken(farmer)))
                .andExpect(status().isForbidden());
    }

    @Test
    void farmerCannotCreateProcurementOrSaleTransactions() throws Exception {
        User farmer = new User();
        farmer.setName("Farmer");
        farmer.setPhone("9876543211");
        farmer.setPasswordHash(passwordEncoder.encode("password123"));
        farmer.setRole(UserRole.FARMER);
        farmer.setActive(true);
        farmer = userRepository.save(farmer);
        String authorization = "Bearer " + jwtService.generateToken(farmer);
        String batchId = "55e3153f-febb-4491-beb1-60a1d9ca0b1f";

        mockMvc.perform(post("/api/batches/{batchId}/procurement", batchId)
                        .header("Authorization", authorization)
                        .contentType("application/json")
                        .content("{\"quantityTaken\":10,\"farmerPricePerUnit\":20,\"paymentStatus\":\"UNPAID\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/batches/{batchId}/sale-transactions", batchId)
                        .header("Authorization", authorization)
                        .contentType("application/json")
                        .content("{\"quantitySold\":1,\"salePricePerUnit\":30}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void farmerCannotReadAdminPaymentReports() throws Exception {
        User farmer = new User();
        farmer.setName("Farmer");
        farmer.setPhone("9876543212");
        farmer.setPasswordHash(passwordEncoder.encode("password123"));
        farmer.setRole(UserRole.FARMER);
        farmer.setActive(true);
        farmer = userRepository.save(farmer);

        mockMvc.perform(get("/api/admin/payments/summary")
                        .header("Authorization", "Bearer " + jwtService.generateToken(farmer)))
                .andExpect(status().isForbidden());
    }
}
