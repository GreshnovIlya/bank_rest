package com.example.bankcards.controller;

import com.example.bankcards.config.SecurityConfig;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.NewCardDto;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardException;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
public class AdminControllerCardTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CardService cardService;

    //@MockBean
    //private UserService userService;

    User user = new User(1L, "Anton", "bgyfygvbhjnug", Role.USER);
    UserDto userDto = new UserDto("Anton", Role.USER);
    CardDto cardDto = new CardDto("1111 1111 1111 1111", user.getUsername(), "12/26",
            CardStatus.ACTIVE, 0);
    CardDto cardDtoBlock = new CardDto("1111 1111 1111 1111", user.getUsername(), "12/26",
            CardStatus.BLOCKED, 0);
    NewCardDto newCardDto = new NewCardDto("1111 1111 1111 1111", user.getUsername(), "12/26");

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void createCard_Successful() throws Exception {
        when(cardService.createCard(any(NewCardDto.class)))
                .thenReturn(cardDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCardDto)))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.cardNumber")
                        .value(cardDto.getCardNumber()));
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void createCard_UserRoleIsUser() throws Exception {
        when(cardService.createCard(any(NewCardDto.class)))
                .thenReturn(cardDto);

        mockMvc.perform(MockMvcRequestBuilders.post("/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCardDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void createCard_CardExist() throws Exception {
        when(cardService.createCard(any(NewCardDto.class)))
                .thenThrow(new CardException("Карта с этим номером уже существует"));

        mockMvc.perform(MockMvcRequestBuilders.post("/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCardDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void blockCard_Successful() throws Exception {
        when(cardService.blockCard(cardDto.getCardNumber()))
                .thenReturn(cardDtoBlock);

        mockMvc.perform(MockMvcRequestBuilders.patch("/admin/cards/1111 1111 1111 1111/block"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.cardStatus")
                        .value(cardDtoBlock.getCardStatus().toString()));
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void blockCard_CartBlocked() throws Exception {
        when(cardService.blockCard(cardDtoBlock.getCardNumber()))
                .thenThrow(new CardException("Карта уже заблокирована или истек срок ее действия"));

        mockMvc.perform(MockMvcRequestBuilders.patch("/admin/cards/1111 1111 1111 1111/block"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void activateCard_Successful() throws Exception {
        when(cardService.activateCard(cardDtoBlock.getCardNumber()))
                .thenReturn(cardDto);

        mockMvc.perform(MockMvcRequestBuilders.patch("/admin/cards/1111 1111 1111 1111/activate"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.cardStatus")
                        .value(cardDto.getCardStatus().toString()));
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void activateCard_CartActive() throws Exception {
        when(cardService.activateCard(cardDto.getCardNumber()))
                .thenThrow(new CardException("Карта уже активна или истек срок ее действия"));

        mockMvc.perform(MockMvcRequestBuilders.patch("/admin/cards/1111 1111 1111 1111/activate"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void deleteCard_Successful() throws Exception {
        doNothing().when(cardService).deleteCard(cardDtoBlock.getCardNumber());

        mockMvc.perform(MockMvcRequestBuilders.delete("/admin/cards/1111 1111 1111 1111"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void getCards_Successful() throws Exception {
        when(cardService.getCards("", "", 0, 10, "id", "asc"))
                .thenReturn(List.of(cardDto));

        mockMvc.perform(MockMvcRequestBuilders.get("/admin/cards")
                        .param("username", "")
                        .param("cardStatus", "")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("sortMode", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].cardNumber").value(cardDto.getCardNumber()));
    }
}
