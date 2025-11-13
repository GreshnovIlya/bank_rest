package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthorizationRequest;
import com.example.bankcards.dto.RegistrationRequest;
import com.example.bankcards.entity.Role;
import com.example.bankcards.exception.NotFoundException;
import com.example.bankcards.exception.UserException;
import com.example.bankcards.service.AuthorizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthorizationService authorizationService;

    RegistrationRequest registrationRequest =new RegistrationRequest("Anton", "password", Role.USER);
    AuthorizationRequest authorizationRequest = new AuthorizationRequest("Anton", "password");
    String token = "token";

    @Test
    void registration_Successful() throws Exception {
        when(authorizationService.registration(registrationRequest))
                .thenReturn(token);

        mockMvc.perform(MockMvcRequestBuilders.post("/authorization/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string(token));
    }

    @Test
    void registration_IncorrectRequest() throws Exception {
        RegistrationRequest registrationRequestIncorrect =
                new RegistrationRequest("A", "pas", Role.USER);

        mockMvc.perform(MockMvcRequestBuilders.post("/authorization/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequestIncorrect)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registration_UserWithUsernameExist() throws Exception {
        when(authorizationService.registration(registrationRequest))
                .thenThrow(new UserException("Пользователь с таким именем уже существует"));

        mockMvc.perform(MockMvcRequestBuilders.post("/authorization/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void authorization_Successful() throws Exception {
        when(authorizationService.authorization(authorizationRequest))
                .thenReturn(token);

        mockMvc.perform(MockMvcRequestBuilders.post("/authorization/authorization")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorizationRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(token));
    }

    @Test
    void authorization_IncorrectRequest() throws Exception {
        AuthorizationRequest authorizationRequestIncorrect = new AuthorizationRequest("A", "pas");

        mockMvc.perform(MockMvcRequestBuilders.post("/authorization/authorization")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorizationRequestIncorrect)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void authorization_NotFoundUser() throws Exception {
        when(authorizationService.authorization(authorizationRequest))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(MockMvcRequestBuilders.post("/authorization/authorization")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authorizationRequest)))
                .andExpect(status().isNotFound());
    }
}
