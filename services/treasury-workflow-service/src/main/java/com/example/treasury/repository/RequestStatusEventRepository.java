package com.example.treasury.repository;

import com.example.treasury.domain.RequestStatusEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestStatusEventRepository extends JpaRepository<RequestStatusEvent, Long> {

    List<RequestStatusEvent> findByRequestIdOrderByCreatedAtAsc(String requestId);
}
