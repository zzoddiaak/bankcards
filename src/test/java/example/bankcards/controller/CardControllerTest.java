package example.bankcards.controller;

import example.bankcards.dto.card.CardRequestDto;
import example.bankcards.dto.card.CardResponseDto;
import example.bankcards.entity.CardStatus;
import example.bankcards.security.jwt.JwtService;
import example.bankcards.service.api.CardServiceInterface;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;


    @MockBean
    private CardServiceInterface cardService;

    @Autowired
    private ObjectMapper objectMapper;

    private CardRequestDto createSampleRequestDto() {
        return CardRequestDto.builder()
                .cardNumber("1234567890123456")
                .expirationDate(LocalDate.of(2026, 12, 31))
                .balance(BigDecimal.valueOf(1000))
                .build();
    }

    private CardResponseDto createSampleResponseDto(Long id, Long ownerId) {
        return CardResponseDto.builder()
                .id(id)
                .maskedCardNumber("************3456")
                .expirationDate(LocalDate.of(2026, 12, 31))
                .status("ACTIVE")
                .balance(BigDecimal.valueOf(1000))
                .ownerId(ownerId)
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateCard() throws Exception {
        Long ownerId = 1L;
        CardRequestDto requestDto = createSampleRequestDto();
        CardResponseDto responseDto = createSampleResponseDto(1L, ownerId);

        Mockito.when(cardService.createCard(eq(ownerId), any(CardRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/cards/{ownerId}", ownerId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.maskedCardNumber").value(responseDto.getMaskedCardNumber()))
                .andExpect(jsonPath("$.status").value(responseDto.getStatus()))
                .andExpect(jsonPath("$.balance").value(responseDto.getBalance()))
                .andExpect(jsonPath("$.ownerId").value(responseDto.getOwnerId()));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void testGetUserCards() throws Exception {
        Long userId = 1L;
        CardResponseDto responseDto = createSampleResponseDto(1L, userId);

        Page<CardResponseDto> page = new PageImpl<>(List.of(responseDto), PageRequest.of(0, 10), 1);

        Mockito.when(cardService.getUserCards(
                eq(userId),
                any(),
                any(),
                any(),
                any(),
                any())).thenReturn(page);

        mockMvc.perform(get("/api/cards/users/{userId}/cards", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(responseDto.getId()));
    }


    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void testGetCardDetails() throws Exception {
        Long userId = 1L;
        Long cardId = 2L;
        CardResponseDto responseDto = createSampleResponseDto(cardId, userId);

        Mockito.when(cardService.getCardDetails(userId, cardId)).thenReturn(responseDto);

        mockMvc.perform(get("/api/cards/user/{userId}/card/{cardId}", userId, cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.ownerId").value(responseDto.getOwnerId()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testBlockCard() throws Exception {
        Long userId = 1L;
        Long cardId = 2L;

        Mockito.doNothing().when(cardService).blockCard(userId, cardId);

        mockMvc.perform(post("/api/cards/user/{userId}/card/{cardId}/block", userId, cardId)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateStatus() throws Exception {
        Long cardId = 2L;
        CardStatus status = CardStatus.ACTIVE;

        Mockito.doNothing().when(cardService).updateCardStatusAsAdmin(cardId, status);

        mockMvc.perform(patch("/api/cards/admin/{cardId}/status", cardId)
                        .with(csrf())
                        .param("status", status.name()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteCard() throws Exception {
        Long cardId = 2L;

        Mockito.doNothing().when(cardService).deleteCard(cardId);

        mockMvc.perform(delete("/api/cards/{cardId}", cardId)
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllCards() throws Exception {
        CardResponseDto responseDto = createSampleResponseDto(1L, 1L);
        List<CardResponseDto> list = List.of(responseDto);

        Mockito.when(cardService.getAllCards()).thenReturn(list);

        mockMvc.perform(get("/api/cards/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(responseDto.getId()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testFindCardsPendingBlock() throws Exception {
        CardResponseDto responseDto = createSampleResponseDto(1L, 1L);
        List<CardResponseDto> list = List.of(responseDto);

        Mockito.when(cardService.findCardsPendingBlock()).thenReturn(list);

        mockMvc.perform(get("/api/cards/admin/cards/pending-block"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(responseDto.getId()));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "USER"})
    void testRequestCardBlock() throws Exception {
        Long userId = 1L;
        Long cardId = 2L;

        Mockito.doNothing().when(cardService).requestCardBlock(userId, cardId);

        mockMvc.perform(post("/api/cards/request/user/{userId}/card/{cardId}/block", userId, cardId)
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}
