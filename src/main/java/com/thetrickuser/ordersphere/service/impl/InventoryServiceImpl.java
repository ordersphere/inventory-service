package com.thetrickuser.ordersphere.service.impl;

import com.thetrickuser.ordersphere.domain.InventoryResponse;
import com.thetrickuser.ordersphere.domain.ReserveInventoryRequest;
import com.thetrickuser.ordersphere.domain.ReserveItem;
import com.thetrickuser.ordersphere.model.Inventory;
import com.thetrickuser.ordersphere.model.InventoryReservation;
import com.thetrickuser.ordersphere.model.ReservationStatus;
import com.thetrickuser.ordersphere.repository.InventoryRepository;
import com.thetrickuser.ordersphere.repository.InventoryReservationRepository;
import com.thetrickuser.ordersphere.service.InventoryService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;

    public InventoryServiceImpl(InventoryRepository inventoryRepository, InventoryReservationRepository reservationRepository) {
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public InventoryResponse updateInventory(String productId, int quantity) {

        Inventory inventory = inventoryRepository.findById(productId)
                .orElseGet(() -> {
                    Inventory inv = new Inventory();
                    inv.setProductId(productId);
                    inv.setAvailableQuantity(0);
                    inv.setReservedQuantity(0);
                    return inv;
                });

        inventory.setAvailableQuantity(quantity);
        inventory.setUpdatedAt(Instant.now());

        Inventory saved = inventoryRepository.save(inventory);

        return new InventoryResponse(
                saved.getProductId(),
                saved.getAvailableQuantity(),
                saved.getReservedQuantity()
        );
    }

    @Override
    public void reserveInventory(ReserveInventoryRequest request) {

        for (ReserveItem item : request.items()) {
            Inventory inventory = inventoryRepository.findById(item.productId())
                    .orElseThrow(() ->
                            new RuntimeException("Inventory not found for product " + item.productId())
                    );

            if (inventory.getAvailableQuantity() < item.quantity()) {
                throw new RuntimeException("Insufficient stock for product " + item.productId());
            }

            inventory.setAvailableQuantity(
                    inventory.getAvailableQuantity() - item.quantity()
            );
            inventory.setReservedQuantity(
                    inventory.getReservedQuantity() + item.quantity()
            );
            inventory.setUpdatedAt(Instant.now());

            inventoryRepository.save(inventory);

            InventoryReservation reservation = new InventoryReservation();
            reservation.setOrderId(request.orderId());
            reservation.setProductId(item.productId());
            reservation.setQuantity(item.quantity());
            reservation.setExpiresAt(
                    Instant.now().plusSeconds(request.ttlMinutes() * 60L)
            );
            reservation.setStatus(ReservationStatus.ACTIVE);

            reservationRepository.save(reservation);
        }
    }

    @Override
    public void releaseInventory(String orderId) {

        List<InventoryReservation> reservations =
                reservationRepository.findByOrderIdAndStatus(
                        orderId, ReservationStatus.ACTIVE
                );

        for (InventoryReservation reservation : reservations) {

            Inventory inventory = inventoryRepository.findById(reservation.getProductId())
                    .orElseThrow();

            inventory.setAvailableQuantity(
                    inventory.getAvailableQuantity() + reservation.getQuantity()
            );
            inventory.setReservedQuantity(
                    inventory.getReservedQuantity() - reservation.getQuantity()
            );
            inventory.setUpdatedAt(Instant.now());

            reservation.setStatus(ReservationStatus.RELEASED);

            inventoryRepository.save(inventory);
            reservationRepository.save(reservation);
        }
    }

    @Override
    public void expireReservations() {

        List<InventoryReservation> expired =
                reservationRepository.findByExpiresAtBeforeAndStatus(
                        Instant.now(), ReservationStatus.ACTIVE
                );

        for (InventoryReservation reservation : expired) {

            Inventory inventory = inventoryRepository.findById(reservation.getProductId())
                    .orElseThrow();

            inventory.setAvailableQuantity(
                    inventory.getAvailableQuantity() + reservation.getQuantity()
            );
            inventory.setReservedQuantity(
                    inventory.getReservedQuantity() - reservation.getQuantity()
            );
            inventory.setUpdatedAt(Instant.now());

            reservation.setStatus(ReservationStatus.EXPIRED);

            inventoryRepository.save(inventory);
            reservationRepository.save(reservation);
        }
    }


}
