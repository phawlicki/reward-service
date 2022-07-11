package com.example.reward.api;

import com.example.reward.BaseTest;
import com.example.reward.model.TransactionRequest;
import com.example.reward.model.TransactionUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RewardControllerE2ETest extends BaseTest {
    private static final String GET_POINTS_PATH = "/reward/points";
    private static final String SAVE_TRANSACTION_PATH = "/reward/save";
    private static final String UPDATE_TRANSACTION_PATH = "/reward/update";
    private static final String DELETE_TRANSACTION_PATH = "/reward/delete";
    private static final String CUSTOMER_ID = "123";
    private static final String CUSTOMER_ID_PARAM = "customerId";
    private static final String FROM_PARAM = "from";
    private static final String ID = "id";
    private static final String JSON_PATH_CUSTOMER_ID = "$.customerId";
    private static final String JSON_PATH_AMOUNT = "$.amount";
    private static final String JSON_PATH_TOTAL_POINTS = "$.totalPoints";
    private static final String JSON_PATH_TOTAL_FIELD = "$.*";
    private static final String FROM_DATE = LocalDateTime.now().minusMonths(1).toString();
    private static final String FROM_DATE_FOUR_MONTHS = LocalDateTime.now().minusMonths(4).toString();
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ObjectMapper mapper;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    void shouldGetPointsWhenCorrectParams() throws Exception {
        this.mockMvc.perform(get(GET_POINTS_PATH)
                .param(CUSTOMER_ID_PARAM, CUSTOMER_ID)
                .param(FROM_PARAM, FROM_DATE))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(JSON_PATH_CUSTOMER_ID).value(CUSTOMER_ID))
                .andExpect(jsonPath(JSON_PATH_TOTAL_POINTS).value(400));
    }

    @Test
    void shouldReturnBadRequestStatusCodeWhenGetPointsAndCustomerIdIsEmpty() throws Exception {
        this.mockMvc.perform(get(GET_POINTS_PATH)
                .param(CUSTOMER_ID_PARAM, "")
                .param(FROM_PARAM, FROM_DATE))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestStatusCodeWhenGetPointsAndDateIsEmpty() throws Exception {
        this.mockMvc.perform(get(GET_POINTS_PATH)
                .param(CUSTOMER_ID_PARAM, CUSTOMER_ID)
                .param(FROM_PARAM, ""))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestStatusCodeWhenGetPointsAndFromDateIsMoreThenThreeMonths() throws Exception {
        this.mockMvc.perform(get(GET_POINTS_PATH)
                .param(CUSTOMER_ID_PARAM, CUSTOMER_ID)
                .param(FROM_PARAM, FROM_DATE_FOUR_MONTHS))
                .andDo(print())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void shouldSaveTransactionWhenCorrectRequest() throws Exception {
        TransactionRequest transactionRequest = new TransactionRequest(CUSTOMER_ID, new BigDecimal("200"));
        String request = mapper.writeValueAsString(transactionRequest);
        this.mockMvc.perform(post(SAVE_TRANSACTION_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(JSON_PATH_TOTAL_FIELD, hasSize(5)))
                .andExpect(jsonPath(JSON_PATH_AMOUNT).value(200));
    }

    @Test
    void shouldReturnBadRequestWhenSaveTransactionAndCustomerIdIsBlank() throws Exception {
        TransactionRequest transactionRequest = new TransactionRequest(" ", new BigDecimal("200"));
        String request = mapper.writeValueAsString(transactionRequest);
        this.mockMvc.perform(post(SAVE_TRANSACTION_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateTransactionWhenCorrectRequest() throws Exception {
        TransactionUpdateRequest transactionUpdateRequest =
                new TransactionUpdateRequest(transactions.get(0).getId().toHexString(), new BigDecimal("7899"));
        String request = mapper.writeValueAsString(transactionUpdateRequest);
        this.mockMvc.perform(put(UPDATE_TRANSACTION_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(JSON_PATH_TOTAL_FIELD, hasSize(5)))
                .andExpect(jsonPath(JSON_PATH_AMOUNT).value(7899));
    }

    @Test
    void shouldReturnNotFoundStatusCodeWhenUpdateAndTransactionNotExist() throws Exception {
        TransactionUpdateRequest transactionUpdateRequest = new TransactionUpdateRequest(new ObjectId().toHexString(),
                new BigDecimal("7899"));
        String request = mapper.writeValueAsString(transactionUpdateRequest);
        this.mockMvc.perform(put(UPDATE_TRANSACTION_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteTransactionWhenTransactionExist() throws Exception {
        this.mockMvc.perform(delete(DELETE_TRANSACTION_PATH)
                .param(ID, transactions.get(0).getId().toHexString()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundStatusCodeWhenDeleteAndTransactionNotExist() throws Exception {
        this.mockMvc.perform(delete(DELETE_TRANSACTION_PATH)
                .param(ID, new ObjectId().toHexString()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}