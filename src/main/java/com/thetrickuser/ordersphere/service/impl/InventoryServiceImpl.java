package com.thetrickuser.ordersphere.service.impl;

import com.thetrickuser.ordersphere.domain.InventoryResponse;
import com.thetrickuser.ordersphere.domain.ReserveInventoryRequest;
import com.thetrickuser.ordersphere.domain.ReserveItem;
import com.thetrickuser.ordersphere.exception.InventoryNotFoundException;
import com.thetrickuser.ordersphere.exception.InventoryReservationFailedException;
import com.thetrickuser.ordersphere.model.Inventory;
import com.thetrickuser.ordersphere.model.InventoryReservation;
import com.thetrickuser.ordersphere.model.ReservationStatus;
import com.thetrickuser.ordersphere.repository.InventoryRepository;
import com.thetrickuser.ordersphere.repository.InventoryReservationRepository;
import com.thetrickuser.ordersphere.service.InventoryService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);
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
                    log.info("Inventory record not found, creating new inventory record for productId: {}", productId);
                    Inventory inv = new Inventory();
                    inv.setProductId(productId);
                    inv.setAvailableQuantity(0);
                    inv.setReservedQuantity(0);
                    return inv;
                });

        inventory.setAvailableQuantity(quantity);
        inventory.setUpdatedAt(Instant.now());

        Inventory saved = inventoryRepository.save(inventory);
        log.info("Inventory updated for productId: {} to available quantity: {}", productId, quantity);

        return new InventoryResponse(
                saved.getProductId(),
                saved.getAvailableQuantity(),
                saved.getReservedQuantity()
        );
    }

    @Override
    public void reserveInventory(ReserveInventoryRequest request) {

        log.info("Reserving inventory for orderId: {}", request.orderId());
        for (ReserveItem item : request.items()) {
            Inventory inventory = getInventory(item.productId());

            if (inventory.getAvailableQuantity() < item.quantity()) {
                log.error("Insufficient stock for product: {}", item.productId());
                throw new InventoryReservationFailedException("Insufficient stock for product " + item.productId());
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
            log.info("Reserved {} units of product {} for orderId {}", item.quantity(), item.productId(), request.orderId());
        }
    }

    @Override
    public void releaseInventory(String orderId) {

        log.info("Releasing inventory for orderId: {}", orderId);
        List<InventoryReservation> reservations =
                reservationRepository.findByOrderIdAndStatus(
                        orderId, ReservationStatus.ACTIVE
                );

        for (InventoryReservation reservation : reservations) {

            Inventory inventory = getInventory(reservation.getProductId());

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
            log.info("Released {} units of product {} for orderId {}", reservation.getQuantity(), reservation.getProductId(), orderId);
        }

        log.info("Released inventory for orderId: {}", orderId);
    }

    @Override
    public void expireReservations() {

        List<InventoryReservation> expired =
                reservationRepository.findByExpiresAtBeforeAndStatus(
                        Instant.now(), ReservationStatus.ACTIVE
                );

        for (InventoryReservation reservation : expired) {

            Inventory inventory = getInventory(reservation.getProductId());

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

    private Inventory getInventory(String productId) {
        return inventoryRepository.findById(productId)
                .orElseThrow(() -> {
                    log.error("Inventory not found for product: {}", productId);
                    return new InventoryNotFoundException("Inventory not found for product: " + productId);
                });
    }


}
