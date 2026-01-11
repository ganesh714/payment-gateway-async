package com.gateway.repositories;

import com.gateway.models.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<Refund, String> {
    List<Refund> findByPaymentId(String paymentId);

    @Query("SELECT SUM(r.amount) FROM Refund r WHERE r.paymentId = ?1 AND r.status IN ('processed', 'pending')")
    Long sumProcessedOrPendingAmountByPaymentId(String paymentId);
}
