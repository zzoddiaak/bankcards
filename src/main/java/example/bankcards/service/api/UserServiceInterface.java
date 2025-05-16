package example.bankcards.service.api;

import example.bankcards.dto.user.UserRequestDto;
import example.bankcards.dto.user.UserResponseDto;

import java.util.List;

public interface UserServiceInterface {
    List<UserResponseDto> getAllUsers();
    UserResponseDto getUserById(Long id);
    UserResponseDto updateUser(Long id, UserRequestDto requestDto);
    void deleteUser(Long id);
}

