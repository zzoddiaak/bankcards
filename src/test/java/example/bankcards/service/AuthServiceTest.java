package example.bankcards.service;

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
import example.bankcards.security.jwt.JwtService;
import example.bankcards.service.impl.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_shouldReturnToken_whenSuccessful() {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("john")
                .password("password")
                .roleIds(Set.of(1L))
                .build();

        Role role = Role.builder()
                .id(1L)
                .name("ROLE_USER")
                .build();
        User savedUser = User.builder()
                .id(1L)
                .username("john")
                .password("encodedPassword")
                .roles(Set.of(role))
                .build();

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any())).thenReturn("mockToken");

        AuthResponseDto response = authService.register(request);

        assertNotNull(response);
        assertEquals("mockToken", response.getToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrowUsernameExists_whenUsernameTaken() {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("john")
                .password("password")
                .roleIds(Set.of(1L))
                .build();

        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThrows(UsernameExists.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldThrowRoleNotFound_whenInvalidRoleId() {
        RegisterRequestDto request = RegisterRequestDto.builder()
                .username("john")
                .password("password")
                .roleIds(Set.of(999L))
                .build();

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RoleNotFound.class, () -> authService.register(request));
    }

    @Test
    void authenticate_shouldReturnToken_whenSuccessful() {
        LoginRequestDto request = LoginRequestDto.builder()
                .username("john")
                .password("password")
                .build();

        Role role = Role.builder()
                .id(1L)
                .name("ROLE_USER")
                .build();
        User user = User.builder()
                .id(1L)
                .username("john")
                .password("encodedPassword")
                .roles(Set.of(role))
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any())).thenReturn("mockToken");

        AuthResponseDto response = authService.authenticate(request);

        assertNotNull(response);
        assertEquals("mockToken", response.getToken());
    }

    @Test
    void authenticate_shouldThrowUserNotFoundLogin_whenUserNotFound() {
        LoginRequestDto request = LoginRequestDto.builder()
                .username("unknown")
                .password("password")
                .build();

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundLogin.class, () -> authService.authenticate(request));
    }
}
