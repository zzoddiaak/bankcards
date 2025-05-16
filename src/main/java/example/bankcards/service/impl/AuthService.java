package example.bankcards.service.impl;

import example.bankcards.dto.login.AuthResponseDto;
import example.bankcards.dto.login.LoginRequestDto;
import example.bankcards.dto.login.RegisterRequestDto;
import example.bankcards.entity.Role;
import example.bankcards.entity.User;
import example.bankcards.exception.role.RoleNotFound;
import example.bankcards.exception.user.UserNotFoundLogin;
import example.bankcards.exception.user.UsernameExists;
import example.bankcards.repository.RoleRepository;
import example.bankcards.repository.UserRepository;
import example.bankcards.security.UserDetailsConfig;
import example.bankcards.security.jwt.JwtService;
import example.bankcards.service.api.AuthServiceInterface;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService implements AuthServiceInterface {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponseDto register(RegisterRequestDto request) {
        log.info("Attempting to register user: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Username already exists: {}", request.getUsername());
            throw new UsernameExists(request.getUsername());
        }

        Set<Role> roles = request.getRoleIds().stream()
                .map(roleId -> {
                    log.debug("Fetching role with id: {}", roleId);
                    return roleRepository.findById(roleId)
                            .orElseThrow(() -> {
                                log.error("Role not found with id: {}", roleId);
                                return new RoleNotFound(roleId);
                            });
                })
                .collect(Collectors.toSet());

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        log.debug("User roles after registration: {}",
                savedUser.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()));

        String token = jwtService.generateToken(new UserDetailsConfig(savedUser));
        log.debug("Generated JWT token for user: {}", request.getUsername());

        return AuthResponseDto.builder()
                .token(token)
                .build();
    }

    @Override
    public AuthResponseDto authenticate(LoginRequestDto request) {
        log.info("Authentication attempt for user: {}", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        log.debug("Authentication successful. Authorities: {}",
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.error("User not found after authentication: {}", request.getUsername());
                    return new UserNotFoundLogin(request.getUsername());
                });

        log.debug("User roles from database: {}",
                user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()));

        String token = jwtService.generateToken(new UserDetailsConfig(user));
        log.info("JWT token generated for user: {}", request.getUsername());

        return AuthResponseDto.builder()
                .token(token)
                .build();
    }
}