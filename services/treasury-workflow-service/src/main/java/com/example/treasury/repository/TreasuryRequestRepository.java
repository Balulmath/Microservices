package com.example.treasury.repository;

import com.example.treasury.domain.RequestStatus;
import com.example.treasury.domain.RequestType;
import com.example.treasury.domain.TreasuryRequest;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TreasuryRequestRepository extends JpaRepository<TreasuryRequest, Long> {

    Optional<TreasuryRequest> findByRequestId(String requestId);

    List<TreasuryRequest> findTop25ByStatusInAndUpdatedAtBefore(Collection<RequestStatus> statuses, LocalDateTime cutoff);

    long countByStatus(RequestStatus status);

    @Query("select r from TreasuryRequest r " +
            "where (:status is null or r.status = :status) " +
            "and (:requestType is null or r.requestType = :requestType) " +
            "and (:clientName is null or lower(r.clientName) like lower(concat('%', :clientName, '%'))) " +
            "order by r.updatedAt desc")
    List<TreasuryRequest> search(@Param("status") RequestStatus status,
                                 @Param("requestType") RequestType requestType,
                                 @Param("clientName") String clientName);
}
