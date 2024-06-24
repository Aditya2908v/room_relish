package org.example.roomrelish.repository;

import org.example.roomrelish.models.Payment;
import org.jetbrains.annotations.TestOnly;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface PaymentRepository extends CrudRepository<Payment, String> {
    List<Payment> findAllByUserId(String _userId);

    Optional<Payment> findByBookingId(String bookingId);
}
