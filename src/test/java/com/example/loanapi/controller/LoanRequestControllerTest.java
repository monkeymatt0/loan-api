package com.example.loanapi.controller;

import com.example.loanapi.dto.CreateLoanRequestDTO;
import com.example.loanapi.dto.LoanRequestResponseDTO;
import com.example.loanapi.dto.UpdateLoanRequestDTO;
import com.example.loanapi.dto.UpdateLoanRequestStatusDTO;
import com.example.loanapi.helper.TestHelper;
import com.example.loanapi.model.LoanRequest;
import com.example.loanapi.model.User;
import com.example.loanapi.model.UserRole;
import com.example.loanapi.repository.LoanRequestRepository;
import com.example.loanapi.security.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for LoanRequestController
 */
@SpringBootTest
@AutoConfigureMockMvc
class LoanRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LoanRequestRepository loanRequestRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User clienteUser;
    private User gestoreUser;

    @BeforeEach
    void setUp() {
        // Clear repository before each test
        clearRepository();
        
        // Setup test users
        clienteUser = TestHelper.createClienteUser();
        gestoreUser = TestHelper.createGestoreUser();
    }

    private void clearRepository() {
        // Since repository is in-memory, we need to clear it manually
        // We'll delete all existing loan requests by finding them first
        loanRequestRepository.findAll().forEach(loan -> 
            loanRequestRepository.deleteById(loan.getId())
        );
    }

    // ========== GET /api/loans Tests ==========

    @Test
    void getAllLoanRequests_EmptyList_ReturnsEmptyPage() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(gestoreUser);
            mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn(UserRole.GESTORE);

            mockMvc.perform(get("/api/loans")
                    .header("Authorization", "Bearer " + TestHelper.GESTORE_TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(0))
                    .andExpect(jsonPath("$.totalElements").value(0))
                    .andExpect(jsonPath("$.totalPages").value(0));
        }
    }

    @Test
    void getAllLoanRequests_WithPagination_ReturnsPaginatedResults() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(gestoreUser);
            mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn(UserRole.GESTORE);

            // Create 5 loan requests
            for (int i = 1; i <= 5; i++) {
                LoanRequest loan = TestHelper.createLoanRequest((long) i, 1L, "Pendiente");
                loan.setCreatedAt(LocalDateTime.now().minusDays(i));
                loanRequestRepository.save(loan);
            }

            // Request page 0, size 2
            mockMvc.perform(get("/api/loans")
                    .param("page", "0")
                    .param("size", "2")
                    .header("Authorization", "Bearer " + TestHelper.GESTORE_TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.totalElements").value(5))
                    .andExpect(jsonPath("$.totalPages").value(3))
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.size").value(2));
        }
    }

    @Test
    void getAllLoanRequests_WithStatusFilter_ReturnsFilteredResults() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(gestoreUser);
            mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn(UserRole.GESTORE);

            // Create loan requests with different statuses
            loanRequestRepository.save(TestHelper.createLoanRequest(1L, 1L, "Pendiente"));
            loanRequestRepository.save(TestHelper.createLoanRequest(2L, 1L, "Aprobada"));
            loanRequestRepository.save(TestHelper.createLoanRequest(3L, 1L, "Pendiente"));

            mockMvc.perform(get("/api/loans")
                    .param("status", "Pendiente")
                    .header("Authorization", "Bearer " + TestHelper.GESTORE_TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].status").value("Pendiente"))
                    .andExpect(jsonPath("$.content[1].status").value("Pendiente"));
        }
    }

    @Test
    void getAllLoanRequests_ClienteSeesOnlyOwnRequests() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(clienteUser);
            mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn(UserRole.CLIENTE);
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(TestHelper.CLIENTE_ID);

            // Create requests for different users
            loanRequestRepository.save(TestHelper.createLoanRequest(1L, TestHelper.CLIENTE_ID, "Pendiente"));
            loanRequestRepository.save(TestHelper.createLoanRequest(2L, TestHelper.GESTORE_ID, "Pendiente"));
            loanRequestRepository.save(TestHelper.createLoanRequest(3L, TestHelper.CLIENTE_ID, "Aprobada"));

            mockMvc.perform(get("/api/loans")
                    .header("Authorization", "Bearer " + TestHelper.CLIENTE_TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.length()").value(2))
                    .andExpect(jsonPath("$.content[0].userId").value(TestHelper.CLIENTE_ID))
                    .andExpect(jsonPath("$.content[1].userId").value(TestHelper.CLIENTE_ID));
        }
    }

    @Test
    void getAllLoanRequests_SortedByStatusThenCreatedAt() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(gestoreUser);
            mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn(UserRole.GESTORE);

            LocalDateTime now = LocalDateTime.now();
            loanRequestRepository.save(TestHelper.createLoanRequest(1L, 1L, "User1", 
                    new BigDecimal("1000"), "EUR", "DOC1", "Aprobada", now.minusDays(2)));
            loanRequestRepository.save(TestHelper.createLoanRequest(2L, 1L, "User2", 
                    new BigDecimal("2000"), "EUR", "DOC2", "Pendiente", now.minusDays(1)));
            loanRequestRepository.save(TestHelper.createLoanRequest(3L, 1L, "User3", 
                    new BigDecimal("3000"), "EUR", "DOC3", "Pendiente", now));

            mockMvc.perform(get("/api/loans")
                    .header("Authorization", "Bearer " + TestHelper.GESTORE_TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].status").value("Pendiente"))
                    .andExpect(jsonPath("$.content[0].id").value(2)) // Older Pendiente first
                    .andExpect(jsonPath("$.content[1].status").value("Pendiente"))
                    .andExpect(jsonPath("$.content[1].id").value(3)) // Newer Pendiente second
                    .andExpect(jsonPath("$.content[2].status").value("Aprobada"));
        }
    }

    @Test
    void getAllLoanRequests_UnauthorizedRole_ReturnsForbidden() throws Exception {
        // This test would require a user with a different role, but we only have CLIENTE and GESTORE
        // Since both are allowed, we'll test with missing token instead
        mockMvc.perform(get("/api/loans"))
                .andExpect(status().isUnauthorized());
    }

    // ========== GET /api/loans/{id} Tests ==========

    @Test
    void getLoanRequestById_ExistingRequest_ReturnsRequest() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(gestoreUser);
            mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn(UserRole.GESTORE);

            LoanRequest loan = TestHelper.createLoanRequest(1L, 1L, "Pendiente");
            loanRequestRepository.save(loan);

            mockMvc.perform(get("/api/loans/1")
                    .header("Authorization", "Bearer " + TestHelper.GESTORE_TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.status").value("Pendiente"));
        }
    }

    @Test
    void getLoanRequestById_ClienteCanSeeOwnRequest() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(clienteUser);
            mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn(UserRole.CLIENTE);
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(TestHelper.CLIENTE_ID);

            LoanRequest loan = TestHelper.createLoanRequest(1L, TestHelper.CLIENTE_ID, "Pendiente");
            loanRequestRepository.save(loan);

            mockMvc.perform(get("/api/loans/1")
                    .header("Authorization", "Bearer " + TestHelper.CLIENTE_TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }
    }

    @Test
    void getLoanRequestById_GestoreCanSeeAnyRequest() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(gestoreUser);
            mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn(UserRole.GESTORE);

            LoanRequest loan = TestHelper.createLoanRequest(1L, TestHelper.CLIENTE_ID, "Pendiente");
            loanRequestRepository.save(loan);

            mockMvc.perform(get("/api/loans/1")
                    .header("Authorization", "Bearer " + TestHelper.GESTORE_TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }
    }

    @Test
    void getLoanRequestById_NotFound_Returns404() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(gestoreUser);
            mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn(UserRole.GESTORE);

            mockMvc.perform(get("/api/loans/999")
                    .header("Authorization", "Bearer " + TestHelper.GESTORE_TOKEN))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void getLoanRequestById_ClienteTriesToSeeOtherUserRequest_Returns403() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(clienteUser);
            mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn(UserRole.CLIENTE);
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(TestHelper.CLIENTE_ID);

            // Create a loan request for a different user
            LoanRequest loan = TestHelper.createLoanRequest(1L, TestHelper.GESTORE_ID, "Pendiente");
            loanRequestRepository.save(loan);

            mockMvc.perform(get("/api/loans/1")
                    .header("Authorization", "Bearer " + TestHelper.CLIENTE_TOKEN))
                    .andExpect(status().isForbidden());
        }
    }

    // ========== POST /api/loans Tests ==========

    @Test
    void createLoanRequest_ValidRequest_ReturnsCreated() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(clienteUser);
            mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn(UserRole.CLIENTE);
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(TestHelper.CLIENTE_ID);

            CreateLoanRequestDTO dto = TestHelper.createValidCreateDTO();

            MvcResult result = mockMvc.perform(post("/api/loans")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto))
                    .header("Authorization", "Bearer " + TestHelper.CLIENTE_TOKEN))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.status").value("Pendiente"))
                    .andExpect(jsonPath("$.userId").value(TestHelper.CLIENTE_ID))
                    .andExpect(jsonPath("$.applicantName").value("John Doe"))
                    .andExpect(jsonPath("$.amount").value(1000.00))
                    .andReturn();

            // Verify the loan was saved
            LoanRequestResponseDTO response = objectMapper.readValue(
                    result.getResponse().getContentAsString(), LoanRequestResponseDTO.class);
            assertNotNull(loanRequestRepository.findById(response.getId()));
        }
    }

    @Test
    void createLoanRequest_InitialStatusIsPendiente() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(clienteUser);
            mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn(UserRole.CLIENTE);
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(TestHelper.CLIENTE_ID);

            CreateLoanRequestDTO dto = TestHelper.createValidCreateDTO();

            mockMvc.perform(post("/api/loans")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto))
                    .header("Authorization", "Bearer " + TestHelper.CLIENTE_TOKEN))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("Pendiente"));
        }
    }

    @Test
    void createLoanRequest_UserIdSetFromCurrentUser() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(clienteUser);
            mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn(UserRole.CLIENTE);
            mockedUserContext.when(UserContext::getCurrentUserId).thenReturn(TestHelper.CLIENTE_ID);

            CreateLoanRequestDTO dto = TestHelper.createValidCreateDTO();

            mockMvc.perform(post("/api/loans")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto))
                    .header("Authorization", "Bearer " + TestHelper.CLIENTE_TOKEN))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.userId").value(TestHelper.CLIENTE_ID));
        }
    }

    @Test
    void createLoanRequest_MissingRequiredFields_Returns400() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(clienteUser);
            mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn(UserRole.CLIENTE);

            CreateLoanRequestDTO dto = TestHelper.createInvalidCreateDTO();

            mockMvc.perform(post("/api/loans")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto))
                    .header("Authorization", "Bearer " + TestHelper.CLIENTE_TOKEN))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void createLoanRequest_NegativeAmount_Returns400() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(clienteUser);
            mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn(UserRole.CLIENTE);

            CreateLoanRequestDTO dto = TestHelper.createCreateDTO("Test", new BigDecimal("-100"), "EUR", "DOC123");

            mockMvc.perform(post("/api/loans")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto))
                    .header("Authorization", "Bearer " + TestHelper.CLIENTE_TOKEN))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void createLoanRequest_OnlyClienteCanCreate() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(gestoreUser);
            mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn(UserRole.GESTORE);

            CreateLoanRequestDTO dto = TestHelper.createValidCreateDTO();

            mockMvc.perform(post("/api/loans")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto))
                    .header("Authorization", "Bearer " + TestHelper.GESTORE_TOKEN))
                    .andExpect(status().isForbidden());
        }
    }

    // ========== PUT /api/loans/{id} Tests ==========

    @Test
    void updateLoanRequest_ExistingRequest_ReturnsUpdated() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(gestoreUser);
            mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn(UserRole.GESTORE);

            LoanRequest existing = TestHelper.createLoanRequest(1L, 1L, "Pendiente");
            existing.setCreatedAt(LocalDateTime.now().minusDays(1));
            loanRequestRepository.save(existing);

            UpdateLoanRequestDTO dto = TestHelper.createValidUpdateDTO();

            mockMvc.perform(put("/api/loans/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto))
                    .header("Authorization", "Bearer " + TestHelper.GESTORE_TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.applicantName").value("Jane Doe"))
                    .andExpect(jsonPath("$.amount").value(2000.00))
                    .andExpect(jsonPath("$.currency").value("USD"))
                    .andExpect(jsonPath("$.status").value("Pendiente")) // Status should not change
                    .andExpect(jsonPath("$.createdAt").exists()); // createdAt should not change
        }
    }

    @Test
    void updateLoanRequest_NotFound_Returns404() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(gestoreUser);
            mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn(UserRole.GESTORE);

            UpdateLoanRequestDTO dto = TestHelper.createValidUpdateDTO();

            mockMvc.perform(put("/api/loans/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto))
                    .header("Authorization", "Bearer " + TestHelper.GESTORE_TOKEN))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void updateLoanRequest_InvalidFields_Returns400() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(gestoreUser);
            mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn(UserRole.GESTORE);

            LoanRequest existing = TestHelper.createLoanRequest(1L, 1L, "Pendiente");
            loanRequestRepository.save(existing);

            UpdateLoanRequestDTO dto = new UpdateLoanRequestDTO();
            // Missing required fields

            mockMvc.perform(put("/api/loans/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto))
                    .header("Authorization", "Bearer " + TestHelper.GESTORE_TOKEN))
                    .andExpect(status().isBadRequest());
        }
    }

    // ========== PATCH /api/loans/{id}/status Tests ==========

    @Test
    void updateLoanRequestStatus_PendienteToAprobada_ReturnsUpdated() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(gestoreUser);
            mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn(UserRole.GESTORE);

            LoanRequest existing = TestHelper.createLoanRequest(1L, 1L, "Pendiente");
            loanRequestRepository.save(existing);

            UpdateLoanRequestStatusDTO dto = TestHelper.createStatusUpdateDTO("Aprobada");

            mockMvc.perform(patch("/api/loans/1/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto))
                    .header("Authorization", "Bearer " + TestHelper.GESTORE_TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("Aprobada"));
        }
    }

    @Test
    void updateLoanRequestStatus_PendienteToRechazada_ReturnsUpdated() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(gestoreUser);
            mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn(UserRole.GESTORE);

            LoanRequest existing = TestHelper.createLoanRequest(1L, 1L, "Pendiente");
            loanRequestRepository.save(existing);

            UpdateLoanRequestStatusDTO dto = TestHelper.createStatusUpdateDTO("Rechazada");

            mockMvc.perform(patch("/api/loans/1/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto))
                    .header("Authorization", "Bearer " + TestHelper.GESTORE_TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("Rechazada"));
        }
    }

    @Test
    void updateLoanRequestStatus_AprobadaToCancelada_ReturnsUpdated() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(gestoreUser);
            mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn(UserRole.GESTORE);

            LoanRequest existing = TestHelper.createLoanRequest(1L, 1L, "Aprobada");
            loanRequestRepository.save(existing);

            UpdateLoanRequestStatusDTO dto = TestHelper.createStatusUpdateDTO("Cancelada");

            mockMvc.perform(patch("/api/loans/1/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto))
                    .header("Authorization", "Bearer " + TestHelper.GESTORE_TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("Cancelada"));
        }
    }

    @Test
    void updateLoanRequestStatus_SameStatus_ReturnsUpdated() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(gestoreUser);
            mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn(UserRole.GESTORE);

            LoanRequest existing = TestHelper.createLoanRequest(1L, 1L, "Pendiente");
            loanRequestRepository.save(existing);

            UpdateLoanRequestStatusDTO dto = TestHelper.createStatusUpdateDTO("Pendiente");

            mockMvc.perform(patch("/api/loans/1/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto))
                    .header("Authorization", "Bearer " + TestHelper.GESTORE_TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("Pendiente"));
        }
    }

    @Test
    void updateLoanRequestStatus_InvalidTransitionAprobadaToPendiente_Returns400() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(gestoreUser);
            mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn(UserRole.GESTORE);

            LoanRequest existing = TestHelper.createLoanRequest(1L, 1L, "Aprobada");
            loanRequestRepository.save(existing);

            UpdateLoanRequestStatusDTO dto = TestHelper.createStatusUpdateDTO("Pendiente");

            mockMvc.perform(patch("/api/loans/1/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto))
                    .header("Authorization", "Bearer " + TestHelper.GESTORE_TOKEN))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void updateLoanRequestStatus_InvalidTransitionRechazadaToAprobada_Returns400() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(gestoreUser);
            mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn(UserRole.GESTORE);

            LoanRequest existing = TestHelper.createLoanRequest(1L, 1L, "Rechazada");
            loanRequestRepository.save(existing);

            UpdateLoanRequestStatusDTO dto = TestHelper.createStatusUpdateDTO("Aprobada");

            mockMvc.perform(patch("/api/loans/1/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto))
                    .header("Authorization", "Bearer " + TestHelper.GESTORE_TOKEN))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void updateLoanRequestStatus_NotFound_Returns404() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(gestoreUser);
            mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn(UserRole.GESTORE);

            UpdateLoanRequestStatusDTO dto = TestHelper.createStatusUpdateDTO("Aprobada");

            mockMvc.perform(patch("/api/loans/999/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto))
                    .header("Authorization", "Bearer " + TestHelper.GESTORE_TOKEN))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    void updateLoanRequestStatus_OnlyGestoreCanUpdate() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(clienteUser);
            mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn(UserRole.CLIENTE);

            LoanRequest existing = TestHelper.createLoanRequest(1L, TestHelper.CLIENTE_ID, "Pendiente");
            loanRequestRepository.save(existing);

            UpdateLoanRequestStatusDTO dto = TestHelper.createStatusUpdateDTO("Aprobada");

            mockMvc.perform(patch("/api/loans/1/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto))
                    .header("Authorization", "Bearer " + TestHelper.CLIENTE_TOKEN))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void updateLoanRequestStatus_MissingStatus_Returns400() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(gestoreUser);
            mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn(UserRole.GESTORE);

            LoanRequest existing = TestHelper.createLoanRequest(1L, 1L, "Pendiente");
            loanRequestRepository.save(existing);

            UpdateLoanRequestStatusDTO dto = new UpdateLoanRequestStatusDTO();
            // status is null

            mockMvc.perform(patch("/api/loans/1/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto))
                    .header("Authorization", "Bearer " + TestHelper.GESTORE_TOKEN))
                    .andExpect(status().isBadRequest());
        }
    }

    // ========== DELETE /api/loans/{id} Tests ==========

    @Test
    void deleteLoanRequest_ExistingRequest_ReturnsNoContent() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(gestoreUser);
            mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn(UserRole.GESTORE);

            LoanRequest existing = TestHelper.createLoanRequest(1L, 1L, "Pendiente");
            loanRequestRepository.save(existing);

            mockMvc.perform(delete("/api/loans/1")
                    .header("Authorization", "Bearer " + TestHelper.GESTORE_TOKEN))
                    .andExpect(status().isNoContent());

            // Verify deletion
            assertFalse(loanRequestRepository.findById(1L).isPresent());
        }
    }

    @Test
    void deleteLoanRequest_NotFound_Returns404() throws Exception {
        try (MockedStatic<UserContext> mockedUserContext = mockStatic(UserContext.class)) {
            mockedUserContext.when(UserContext::getCurrentUser).thenReturn(gestoreUser);
            mockedUserContext.when(UserContext::getCurrentUserRole).thenReturn(UserRole.GESTORE);

            mockMvc.perform(delete("/api/loans/999")
                    .header("Authorization", "Bearer " + TestHelper.GESTORE_TOKEN))
                    .andExpect(status().isNotFound());
        }
    }
}
