package example.bankcards.service.api;

import example.bankcards.dto.login.AuthResponseDto;
import example.bankcards.dto.login.LoginRequestDto;
import example.bankcards.dto.login.RegisterRequestDto;

public interface AuthServiceInterface {
    AuthResponseDto register(RegisterRequestDto request);
    AuthResponseDto authenticate(LoginRequestDto request);
}

