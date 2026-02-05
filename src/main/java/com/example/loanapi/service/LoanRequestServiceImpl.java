package com.example.loanapi.service;

import com.example.loanapi.dto.CreateLoanRequestDTO;
import com.example.loanapi.dto.LoanRequestFilter;
import com.example.loanapi.dto.LoanRequestResponseDTO;
import com.example.loanapi.dto.PageRequest;
import com.example.loanapi.dto.PageResponse;
import com.example.loanapi.dto.UpdateLoanRequestDTO;
import com.example.loanapi.dto.UpdateLoanRequestStatusDTO;
import com.example.loanapi.exception.LoanRequestNotFoundException;
import com.example.loanapi.mapper.LoanRequestMapper;
import com.example.loanapi.model.LoanRequest;
import com.example.loanapi.model.LoanStatus;
import com.example.loanapi.model.UserRole;
import com.example.loanapi.repository.LoanRequestRepository;
import com.example.loanapi.security.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of LoanRequestService
 */
@Service
public class LoanRequestServiceImpl implements LoanRequestService {

    private final LoanRequestRepository loanRequestRepository;

    @Autowired
    public LoanRequestServiceImpl(LoanRequestRepository loanRequestRepository) {
        this.loanRequestRepository = loanRequestRepository;
    }

    @Override
    public PageResponse<LoanRequestResponseDTO> getAllLoanRequests(PageRequest pageRequest, LoanRequestFilter filter) {
        List<LoanRequest> allRequests = loanRequestRepository.findAll();
        
        // Filter by userId if CLIENTE
        UserRole currentRole = UserContext.getCurrentUserRole();
        if (currentRole == UserRole.CLIENTE) {
            Long currentUserId = UserContext.getCurrentUserId();
            allRequests = allRequests.stream()
                    .filter(req -> req.getUserId() != null && req.getUserId().equals(currentUserId))
                    .collect(Collectors.toList());
        }
        
        // Apply status filter if present
        if (filter != null && filter.hasStatusFilter()) {
            String normalizedStatus = filter.getNormalizedStatus();
            if (normalizedStatus != null) {
                allRequests = allRequests.stream()
                        .filter(req -> normalizedStatus.equals(req.getStatus()))
                        .collect(Collectors.toList());
            }
        }
        
        // Sort: Pendiente first, then by createdAt (chronological)
        allRequests = allRequests.stream()
                .sorted(createSortingComparator())
                .collect(Collectors.toList());
        
        // Calculate pagination
        long totalElements = allRequests.size();
        int totalPages = (int) Math.ceil((double) totalElements / pageRequest.getSize());
        
        // Apply pagination
        int start = pageRequest.getPage() * pageRequest.getSize();
        
        List<LoanRequestResponseDTO> content = allRequests.stream()
                .skip(start)
                .limit(pageRequest.getSize())
                .map(LoanRequestMapper::toResponseDTO)
                .collect(Collectors.toList());
        
        return new PageResponse<>(content, totalElements, totalPages, pageRequest.getPage(), pageRequest.getSize());
    }
    
    /**
     * Create comparator for sorting: Pendiente first, then by createdAt (chronological)
     * 
     * @return comparator
     */
    private Comparator<LoanRequest> createSortingComparator() {
        return Comparator
                .comparing((LoanRequest req) -> !"Pendiente".equals(req.getStatus())) // Pendiente first (false < true)
                .thenComparing(LoanRequest::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    @Override
    public LoanRequestResponseDTO getLoanRequestById(Long id) {
        LoanRequest loanRequest = loanRequestRepository.findById(id)
                .orElseThrow(() -> new LoanRequestNotFoundException(id));
        return LoanRequestMapper.toResponseDTO(loanRequest);
    }

    @Override
    public LoanRequestResponseDTO createLoanRequest(CreateLoanRequestDTO createLoanRequestDTO) {
        Long currentUserId = UserContext.getCurrentUserId();
        LoanRequest loanRequest = LoanRequestMapper.toModel(createLoanRequestDTO, currentUserId);
        LoanRequest saved = loanRequestRepository.save(loanRequest);
        return LoanRequestMapper.toResponseDTO(saved);
    }

    @Override
    public LoanRequestResponseDTO updateLoanRequest(Long id, UpdateLoanRequestDTO updateLoanRequestDTO) {
        LoanRequest existing = loanRequestRepository.findById(id)
                .orElseThrow(() -> new LoanRequestNotFoundException(id));
        
        LoanRequestMapper.toModel(updateLoanRequestDTO, existing);
        LoanRequest updated = loanRequestRepository.save(existing);
        return LoanRequestMapper.toResponseDTO(updated);
    }

    @Override
    public LoanRequestResponseDTO updateLoanRequestStatus(Long id, UpdateLoanRequestStatusDTO updateStatusDTO) {
        LoanRequest existing = loanRequestRepository.findById(id)
                .orElseThrow(() -> new LoanRequestNotFoundException(id));
        
        String newStatus = updateStatusDTO.getStatus();
        String currentStatus = existing.getStatus();
        
        // Validate status transition
        validateStatusTransition(currentStatus, newStatus);
        
        existing.setStatus(newStatus);
        LoanRequest updated = loanRequestRepository.save(existing);
        return LoanRequestMapper.toResponseDTO(updated);
    }

    @Override
    public void deleteLoanRequest(Long id) {
        // Verify existence before deleting
        loanRequestRepository.findById(id)
                .orElseThrow(() -> new LoanRequestNotFoundException(id));
        loanRequestRepository.deleteById(id);
    }

    /**
     * Validate status transition according to business rules:
     * - Pendiente -> Aprobada or Rechazada (allowed)
     * - Aprobada -> Cancelada (allowed)
     * - All other transitions -> rejected
     * 
     * @param currentStatus the current status
     * @param newStatus the new status
     * @throws IllegalStateException if transition is invalid
     */
    private void validateStatusTransition(String currentStatus, String newStatus) {
        // Normalize status strings (handle case differences)
        String normalizedCurrent = normalizeStatus(currentStatus);
        String normalizedNew = normalizeStatus(newStatus);
        
        // If status is the same, allow it
        if (normalizedCurrent.equals(normalizedNew)) {
            return;
        }
        
        // Valid transitions
        boolean isValidTransition = 
            ("Pendiente".equals(normalizedCurrent) && 
             ("Aprobada".equals(normalizedNew) || "Rechazada".equals(normalizedNew))) ||
            ("Aprobada".equals(normalizedCurrent) && "Cancelada".equals(normalizedNew));
        
        if (!isValidTransition) {
            throw new IllegalStateException(
                String.format("Invalid status transition from '%s' to '%s'. " +
                    "Allowed transitions: Pendiente -> Aprobada/Rechazada, Aprobada -> Cancelada",
                    currentStatus, newStatus));
        }
    }

    /**
     * Normalize status string to handle case differences
     * 
     * @param status the status string
     * @return normalized status string
     */
    private String normalizeStatus(String status) {
        if (status == null) {
            return null;
        }
        // Try to match enum values
        try {
            LoanStatus loanStatus = LoanStatus.fromString(status);
            return loanStatus.getValue();
        } catch (IllegalArgumentException e) {
            // If not a valid enum, return as-is (will be caught by validation)
            return status;
        }
    }
}
