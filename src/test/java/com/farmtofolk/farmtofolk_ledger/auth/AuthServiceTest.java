package com.farmtofolk.farmtofolk_ledger.auth;

import com.farmtofolk.farmtofolk_ledger.common.error.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;

    @Test
    void loginRejectsInactiveUser() {
        User user = new User();
        user.setPasswordHash("hash");
        user.setActive(false);
        when(userRepository.findByEmailIgnoreCase("farmer@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hash")).thenReturn(true);

        AuthService service = new AuthService(userRepository, passwordEncoder, jwtService);

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> service.login(new LoginRequest("farmer@example.com", "password"))
        );
        assertEquals("User account is inactive", exception.getMessage());
        verifyNoInteractions(jwtService);
    }
}
