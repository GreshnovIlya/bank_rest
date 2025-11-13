package com.example.bankcards.controller;
import com.example.bankcards.config.SecurityConfig;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AccessException;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.service.CardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    User user = new User(1L, "Anton", "bgyfygvbhjnug", Role.USER);
    Card cardFirst = new Card(1L, "1111 1111 1111 1111", user, "12/26",
            CardStatus.ACTIVE, 100);
    Card cardSecond = new Card(2L, "1111 1111 1111 1112", user, "12/26",
            CardStatus.ACTIVE, 100);
    CardDto cardDto = new CardDto("1111 1111 1111 1111", user.getUsername(), "12/26",
            CardStatus.ACTIVE, 100);

    @Test
    @WithMockUser(authorities = {"USER"})
    void getCards_Successful() throws Exception {
        when(cardService.getCardsUser(0, 10, "id", "asc"))
                .thenReturn(List.of(cardDto));

        mockMvc.perform(MockMvcRequestBuilders.get("/user/cards")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("sortMode", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].cardNumber").value(cardFirst.getCardNumber()));
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void getCards_UserRoleIsAdmin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/cards")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("sortMode", "asc"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void transfersBetweenCards_Successful() throws Exception {
        doNothing().when(cardService)
                .transfersBetweenCards(cardFirst.getCardNumber(), cardSecond.getCardNumber(), 50);

        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/user/cards/transfers/50/from/1111 1111 1111 1111/to/1111 1111 1111 1112"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void getBalance_Successful() throws Exception {
        when(cardService.getBalance("1111 1111 1111 1111"))
                .thenReturn(100.0);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/user/cards/1111 1111 1111 1111/balance"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void getBalance_USerIsNotCardholder() throws Exception {
        when(cardService.getBalance("1111 1111 1111 2222"))
                .thenThrow(new AccessException("Карта не доступна данному пользователю"));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/user/cards/1111 1111 1111 2222/balance"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void getBalance_NotFound() throws Exception {
        when(cardService.getBalance("1111 1111 1111 2222"))
                .thenThrow(new NotFoundException("Карта не найдена"));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/user/cards/1111 1111 1111 2222/balance"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void blockCard_Successful() throws Exception {
        CardDto cardDtoBlock = new CardDto("1111 1111 1111 1111", user.getUsername(), "12/26",
                CardStatus.BLOCKED, 100);

        when(cardService.blockCard("1111 1111 1111 1111"))
                .thenReturn(cardDtoBlock);

        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/user/cards/1111 1111 1111 1111/block"))
                .andExpect(status().isOk());
    }
}
