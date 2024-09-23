package com.example.accountmission.controller;

import com.example.accountmission.domain.Account;
import com.example.accountmission.dto.AccountDto;
import com.example.accountmission.dto.CreateAccount;
import com.example.accountmission.dto.DeleteAccount;
import com.example.accountmission.service.AccountService;
import com.example.accountmission.type.AccountStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {
    @MockBean
    private AccountService accountService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // JSON 문자열로 변환시켜줌


    @Test
    @DisplayName("계좌 생성 성공")
    void successCreateAccount() throws Exception {
        //given
        given(accountService.createAccount(anyLong(), anyLong()))
                .willReturn(AccountDto.builder()
                        .userId(1L)
                        .accountNumber("1234567890")
                        .registeredAt(LocalDateTime.now())
                        .unRegisteredAt(LocalDateTime.now())
                        .build());
        //when

        //then
        mockMvc.perform(post("/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new CreateAccount.Request(1L, 1000L)
                )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId")
                        .value(1L))
                .andExpect(jsonPath("$.accountNumber")
                        .value("1234567890"))
                .andDo(print());
    }
    @Test
    @DisplayName("계좌 조회 성공")
    void successGetAccount() throws Exception {
        //given
        final Long id = 1L;
        final AccountStatus accountStatus = AccountStatus.IN_USE;
        final String accountNumber = "4356";

        given(accountService.getAccount(anyLong()))
                .willReturn(Account.builder()
                        .accountStatus(accountStatus)
                        .accountNumber(accountNumber)
                        .build());
        //when
        ResultActions perform = mockMvc.perform(get("/account/" + id)
                        .accept(MediaType.APPLICATION_JSON))
                        .andDo(print());

        //then
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(accountNumber))
                .andExpect(jsonPath("$.accountStatus").value(accountStatus.name()));
    }
    @Test
    @DisplayName("계좌 해지 성공")
    void deleteAccountSuccess() throws Exception {
        //given
        given(accountService.deleteAccount(anyLong(), anyString()))
                .willReturn(AccountDto.builder()
                        .userId(1L)
                        .accountNumber("1234567890")
                        .registeredAt(LocalDateTime.now())
                        .unRegisteredAt(LocalDateTime.now())
                        .build());
        //when
        ResultActions perform = mockMvc.perform(delete("/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new DeleteAccount.Request(1L, "1234567890"))
                ));

        //then
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$.userId")
                        .value(1L))
                .andExpect(jsonPath("$.accountNumber")
                        .value("1234567890"))
                .andDo(print());

    }
    @Test
    @DisplayName("계좌 조회 - 사용자 ID")
    void getAccountsByUserIdSuccess() throws Exception {
        //given
        List<AccountDto> list = new ArrayList<>();
        String accountNumber = "1234567890";
        for (int i = 0; i < 5; i++) {
            list.add(AccountDto.builder()
                    .userId(1L)
                    .accountNumber(accountNumber.substring(
                            0,accountNumber.length()-1) + i)
                    .balance(1000L)
                    .build());
        }
        given(accountService.getAccountsByUserId(anyLong()))
                .willReturn(list);
        //when
        ResultActions perform = mockMvc.perform(get("/account?user_id=1")
                .contentType(MediaType.APPLICATION_JSON));
        //then
        perform.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].balance").value(1000))
                .andExpect(jsonPath("$[0].accountNumber").value("1234567890"));
    }
}