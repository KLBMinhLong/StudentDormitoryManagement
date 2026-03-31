package com.dormitory.management.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dormitory.management.entity.Invoice;
import com.dormitory.management.entity.enums.InvoiceStatus;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @Query("""
            SELECT i FROM Invoice i
            WHERE (:month IS NULL OR i.month = :month)
              AND (:year IS NULL OR i.year = :year)
              AND (:status IS NULL OR i.status = :status)
            """)
    Page<Invoice> searchInvoices(
            @Param("month") Integer month,
            @Param("year") Integer year,
            @Param("status") InvoiceStatus status,
            Pageable pageable);

    Optional<Invoice> findByRoomIdAndMonthAndYear(Long roomId, int month, int year);
}
