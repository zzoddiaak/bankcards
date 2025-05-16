package example.bankcards.service;

import example.bankcards.config.mapper.UserMapper;
import example.bankcards.dto.user.UserRequestDto;
import example.bankcards.dto.user.UserResponseDto;
import example.bankcards.entity.Role;
import example.bankcards.entity.User;
import example.bankcards.exception.role.RoleNotFound;
import example.bankcards.exception.user.UserNotFound;
import example.bankcards.repository.RoleRepository;
import example.bankcards.repository.UserRepository;
import example.bankcards.service.impl.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User user;
    private Role role;
    private UserResponseDto userResponseDto;

    @BeforeEach
    void setup() {
        role = new Role();
        role.setId(1L);
        role.setName("ROLE_USER");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRoles(Set.of(role));

        userResponseDto = new UserResponseDto();
        userResponseDto.setId(1L);
        userResponseDto.setUsername("testuser");
    }

    @Test
    void getAllUsers_success() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toDto(user)).thenReturn(userResponseDto);

        List<UserResponseDto> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
    }

    @Test
    void getUserById_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userResponseDto);

        UserResponseDto result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void getUserById_userNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFound.class, () -> userService.getUserById(1L));
    }

    @Test
    void updateUser_success() {
        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setUsername("updatedUser");
        requestDto.setRoleIds(Set.of(1L));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userResponseDto);

        UserResponseDto result = userService.updateUser(1L, requestDto);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_userNotFound() {
        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setUsername("any");
        requestDto.setRoleIds(Set.of(1L));

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFound.class, () -> userService.updateUser(1L, requestDto));
    }

    @Test
    void updateUser_roleNotFound() {
        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setUsername("any");
        requestDto.setRoleIds(Set.of(2L));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(RoleNotFound.class, () -> userService.updateUser(1L, requestDto));
    }

    @Test
    void deleteUser_success() {
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }
}

