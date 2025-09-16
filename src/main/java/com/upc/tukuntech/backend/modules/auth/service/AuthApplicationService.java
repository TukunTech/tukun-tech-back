package com.upc.tukuntech.backend.modules.auth.service;

import com.upc.tukuntech.backend.modules.auth.dto.LoginRequest;
import com.upc.tukuntech.backend.modules.auth.dto.LoginResponse;
import com.upc.tukuntech.backend.modules.auth.dto.UserSummary;
import com.upc.tukuntech.backend.modules.auth.entity.UserEntity;
import com.upc.tukuntech.backend.modules.auth.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.stream.Collectors;

@Service
public class AuthApplicationService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final SessionService sessionService;

    public AuthApplicationService(AuthenticationManager authenticationManager, JwtService jwtService, UserRepository userRepository, SessionService sessionService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.sessionService = sessionService;
    }

    public LoginResponse login(LoginRequest request, String clientIp, String userAgent) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        String accessToken = jwtService.generateAccessToken(user);
        long accessTtl     = jwtService.getAccessTtlSeconds();
        Instant accessExpAt = Instant.now().plusSeconds(accessTtl);

        String refreshToken = sessionService.registerLogin(user, clientIp, userAgent, accessExpAt);

        var roles = user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet());
        UserSummary summary = new UserSummary(user.getId(), user.getEmail(), roles);

        return new LoginResponse(accessToken, "Bearer", accessTtl, refreshToken, summary);
    }


}
