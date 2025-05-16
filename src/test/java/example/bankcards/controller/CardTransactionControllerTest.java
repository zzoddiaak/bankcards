package example.bankcards.controller;

import example.bankcards.dto.card.CardTransactionDto;
import example.bankcards.dto.card.CardTransferRequestDto;
import example.bankcards.security.jwt.JwtService;
import example.bankcards.service.api.CardTransactionServiceInterface;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardTransactionController.class)
class CardTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardTransactionServiceInterface cardTransactionService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testTransferBetweenOwnCards() throws Exception {
        Long userId = 1L;
        CardTransferRequestDto requestDto = CardTransferRequestDto.builder()
                .fromCardId(100L)
                .toCardId(200L)
                .amount(BigDecimal.valueOf(500))
                .description("Test transfer")
                .build();

        CardTransactionDto responseDto = CardTransactionDto.builder()
                .id(10L)
                .amount(requestDto.getAmount())
                .timestamp(LocalDateTime.now())
                .fromCardId(requestDto.getFromCardId())
                .toCardId(requestDto.getToCardId())
                .description(requestDto.getDescription())
                .build();

        Mockito.when(cardTransactionService.transferBetweenOwnCards(eq(userId), any(CardTransferRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/api/transactions/transfer/{userId}", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId()))
                .andExpect(jsonPath("$.amount").value(responseDto.getAmount().doubleValue()))
                .andExpect(jsonPath("$.fromCardId").value(responseDto.getFromCardId()))
                .andExpect(jsonPath("$.toCardId").value(responseDto.getToCardId()))
                .andExpect(jsonPath("$.description").value(responseDto.getDescription()));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetTransactionHistory() throws Exception {
        Long userId = 1L;
        Long cardId = 100L;
        Pageable pageable = PageRequest.of(0, 10);

        CardTransactionDto transaction1 = CardTransactionDto.builder()
                .id(1L)
                .amount(BigDecimal.valueOf(100))
                .timestamp(LocalDateTime.now().minusDays(1))
                .fromCardId(cardId)
                .toCardId(200L)
                .description("Payment")
                .build();

        CardTransactionDto transaction2 = CardTransactionDto.builder()
                .id(2L)
                .amount(BigDecimal.valueOf(200))
                .timestamp(LocalDateTime.now())
                .fromCardId(300L)
                .toCardId(cardId)
                .description("Refund")
                .build();

        Page<CardTransactionDto> page = new PageImpl<>(List.of(transaction1, transaction2), pageable, 2);

        Mockito.when(cardTransactionService.getTransactionHistory(eq(userId), eq(cardId), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/transactions/history/{userId}/card/{cardId}", userId, cardId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(transaction1.getId()))
                .andExpect(jsonPath("$.content[1].id").value(transaction2.getId()));
    }
}
