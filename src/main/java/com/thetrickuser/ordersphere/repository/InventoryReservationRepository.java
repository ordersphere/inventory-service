package com.thetrickuser.ordersphere.repository;

import com.thetrickuser.ordersphere.model.InventoryReservation;
import com.thetrickuser.ordersphere.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {

    List<InventoryReservation> findByOrderIdAndStatus(String orderId, ReservationStatus status);
    List<InventoryReservation> findByExpiresAtBeforeAndStatus(Instant expiresAt, ReservationStatus status);
}
