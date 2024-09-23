package com.example.accountmission.controller;

import com.example.accountmission.dto.CancelBalance;
import com.example.accountmission.dto.TransactionDto;
import com.example.accountmission.dto.UseBalance;
import com.example.accountmission.service.TransactionService;
import com.example.accountmission.type.TransactionResultType;
import com.example.accountmission.type.TransactionType;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {
    @MockBean
    private TransactionService transactionService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("거래 성공")
    void successUseBalance() throws Exception {
        //given
        given(transactionService.useBalance(anyLong(), anyString(), anyLong()))
                .willReturn(TransactionDto
                        .builder()
                        .accountNumber("1234567890")
                        .transactionResultType(TransactionResultType.SUCCESS)
                        .transactionId("transactionId")
                        .amount(1000L)
                        .transactedAt(LocalDateTime.now())
                        .build());
        //when
        ResultActions perform = mockMvc.perform(post("/transaction/use")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new UseBalance.Request(1L, "1234567890",
                                1000L)
                ))).andDo(print());
        //then
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber")
                        .value("1234567890"))
                .andExpect(jsonPath("$.transactionResultType")
                        .value("SUCCESS"))
                .andExpect(jsonPath("$.transactionId")
                        .value("transactionId"))
                .andExpect(jsonPath("$.amount")
                        .value(1000L));
    }
    @Test
    @DisplayName("거래 취소 성공")
    void successCancelBalance() throws Exception {
        //given
        given(transactionService.cancelBalance(anyString(), anyString(), anyLong()))
                .willReturn(TransactionDto
                        .builder()
                        .accountNumber("1234567890")
                        .transactionResultType(TransactionResultType.SUCCESS)
                        .transactionId("transactionId")
                        .amount(1000L)
                        .transactedAt(LocalDateTime.now())
                        .build());
        //when
        ResultActions perform = mockMvc.perform(post("/transaction/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new CancelBalance.Request("transactionId", "1234567890",
                                1000L)
                ))).andDo(print());
        //then
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber")
                        .value("1234567890"))
                .andExpect(jsonPath("$.transactionResultType")
                        .value("SUCCESS"))
                .andExpect(jsonPath("$.transactionId")
                        .value("transactionId"))
                .andExpect(jsonPath("$.amount")
                        .value(1000L));
    }
    @Test
    @DisplayName("QueryTransaction 성공")
    void successQueryTransaction() throws Exception {
        //given
        given(transactionService.queryTransaction(anyString()))
                .willReturn(TransactionDto.builder()
                        .accountNumber("1234567890")
                        .transactionResultType(TransactionResultType.SUCCESS)
                        .transactionType(TransactionType.USE)
                        .transactedAt(LocalDateTime.now())
                        .amount(1000L)
                        .transactionId("transactionId")
                        .build());

        //when
        ResultActions perform = mockMvc.perform(get("/transaction/transactionId"))
                .andDo(print());
        //then
        perform.andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.transactionResultType").value("SUCCESS"))
                .andExpect(jsonPath("$.transactionType").value("USE"))
                .andExpect(jsonPath("$.amount").value(1000L));
    }
}