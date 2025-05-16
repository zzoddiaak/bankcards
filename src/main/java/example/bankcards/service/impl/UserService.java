package example.bankcards.service.impl;

import example.bankcards.config.mapper.UserMapper;
import example.bankcards.dto.user.UserRequestDto;
import example.bankcards.dto.user.UserResponseDto;
import example.bankcards.entity.Role;
import example.bankcards.entity.User;
import example.bankcards.exception.role.RoleNotFound;
import example.bankcards.exception.user.UserNotFound;
import example.bankcards.repository.RoleRepository;
import example.bankcards.repository.UserRepository;
import example.bankcards.service.api.UserServiceInterface;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserServiceInterface {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFound(id));
        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto updateUser(Long id, UserRequestDto requestDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFound(id));

        user.setUsername(requestDto.getUsername());

        Set<Role> roles = requestDto.getRoleIds().stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new RoleNotFound(roleId)))
                .collect(Collectors.toSet());

        user.setRoles(roles);
        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }


    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}



