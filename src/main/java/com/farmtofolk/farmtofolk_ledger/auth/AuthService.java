package com.farmtofolk.farmtofolk_ledger.auth;

import com.farmtofolk.farmtofolk_ledger.common.error.UnauthorizedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        String login = request.emailOrPhone().trim();
        User user = userRepository.findByEmailIgnoreCase(login)
                .or(() -> userRepository.findByPhone(login))
                .orElseThrow(() -> new UnauthorizedException("Invalid email, phone, or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email, phone, or password");
        }
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new UnauthorizedException("User account is inactive");
        }

        return new LoginResponse(jwtService.generateToken(user), UserResponse.from(user));
    }
}
