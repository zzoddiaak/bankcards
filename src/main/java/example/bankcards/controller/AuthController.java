package example.bankcards.controller;

import example.bankcards.dto.login.AuthResponseDto;
import example.bankcards.dto.login.LoginRequestDto;
import example.bankcards.dto.login.RegisterRequestDto;
import example.bankcards.service.api.AuthServiceInterface;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "Регистрация и логин пользователей")
public class AuthController {

    private final AuthServiceInterface authService;

    @Operation(summary = "Регистрация пользователя")
    @ApiResponse(responseCode = "200", description = "Успешная регистрация")
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@RequestBody RegisterRequestDto request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Аутентификация пользователя (логин)")
    @ApiResponse(responseCode = "200", description = "Успешный вход и получение токена")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }
}

