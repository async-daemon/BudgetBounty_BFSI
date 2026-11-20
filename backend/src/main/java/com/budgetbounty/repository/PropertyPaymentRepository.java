package com.budgetbounty.repository;

import com.budgetbounty.entity.PropertyPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PropertyPaymentRepository extends JpaRepository<PropertyPayment, Long> {
    List<PropertyPayment> findByPropertyIdOrderByDueDateAsc(Long propertyId);

    @Query("select p from PropertyPayment p where p.id = :id and p.property.user.id = :userId")
    Optional<PropertyPayment> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("select p from PropertyPayment p where p.property.user.id = :userId order by p.dueDate asc")
    List<PropertyPayment> findAllForUser(@Param("userId") Long userId);
}
