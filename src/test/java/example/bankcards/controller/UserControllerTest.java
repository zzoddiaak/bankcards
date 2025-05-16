package example.bankcards.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import example.bankcards.dto.user.UserRequestDto;
import example.bankcards.dto.user.UserResponseDto;
import example.bankcards.security.jwt.JwtService;
import example.bankcards.service.api.UserServiceInterface;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import java.util.Set;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserServiceInterface userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetAllUsers() throws Exception {
        UserResponseDto user1 = UserResponseDto.builder()
                .id(1L)
                .username("user1")
                .roles(Set.of("ROLE_USER"))
                .cards(List.of())
                .build();

        UserResponseDto user2 = UserResponseDto.builder()
                .id(2L)
                .username("admin")
                .roles(Set.of("ROLE_ADMIN"))
                .cards(List.of())
                .build();

        Mockito.when(userService.getAllUsers()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[1].username").value("admin"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetUserById() throws Exception {
        Long userId = 1L;

        UserResponseDto user = UserResponseDto.builder()
                .id(userId)
                .username("user1")
                .roles(Set.of("ROLE_USER"))
                .cards(List.of())
                .build();

        Mockito.when(userService.getUserById(userId)).thenReturn(user);

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.roles").isArray());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testUpdateUser() throws Exception {
        Long userId = 1L;

        UserRequestDto updateDto = UserRequestDto.builder()
                .username("updatedUser")
                .roleIds(Set.of(1L, 2L))
                .build();

        UserResponseDto updatedUser = UserResponseDto.builder()
                .id(userId)
                .username("updatedUser")
                .roles(Set.of("ROLE_USER", "ROLE_ADMIN"))
                .cards(List.of())
                .build();

        Mockito.when(userService.updateUser(eq(userId), any(UserRequestDto.class)))
                .thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/{id}", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("updatedUser"))
                .andExpect(jsonPath("$.roles").isArray());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testDeleteUser() throws Exception {
        Long userId = 1L;

        mockMvc.perform(delete("/api/users/{id}", userId)
                        .with(csrf())
                )
                .andExpect(status().isNoContent());

        Mockito.verify(userService).deleteUser(userId);
    }
}
