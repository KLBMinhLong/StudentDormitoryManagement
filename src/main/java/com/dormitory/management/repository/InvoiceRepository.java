package com.dormitory.management.repository;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
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

        @EntityGraph(attributePaths = {"room", "room.building", "student"})
        Optional<Invoice> findWithRelationsById(Long id);

        @EntityGraph(attributePaths = {"room", "room.building", "student"})
        Optional<Invoice> findFirstByInvoiceCodeIgnoreCase(String invoiceCode);

        Optional<Invoice> findByPaymentOrderCode(String paymentOrderCode);

        Optional<Invoice> findByIdAndStudentId(Long id, Long studentId);

        @EntityGraph(attributePaths = {"room", "room.building", "student"})
    @Query("""
            SELECT i FROM Invoice i
            JOIN i.room r
            LEFT JOIN r.building b
            LEFT JOIN i.student s
            WHERE (:month IS NULL OR i.month = :month)
              AND (:year IS NULL OR i.year = :year)
              AND (:status IS NULL OR i.status = :status)
              AND (:buildingId IS NULL OR b.id = :buildingId)
              AND (
                :keyword IS NULL OR :keyword = ''
                OR LOWER(COALESCE(s.fullName, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(COALESCE(s.studentCode, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(COALESCE(r.roomNumber, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(COALESCE(i.invoiceCode, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
    Page<Invoice> searchInvoicesForAdmin(
            @Param("month") Integer month,
            @Param("year") Integer year,
            @Param("status") InvoiceStatus status,
            @Param("buildingId") Long buildingId,
            @Param("keyword") String keyword,
            Pageable pageable);

                @EntityGraph(attributePaths = {"room", "room.building", "student"})
    @Query("""
            SELECT i FROM Invoice i
            WHERE i.student.id = :studentId
              AND (:month IS NULL OR i.month = :month)
              AND (:year IS NULL OR i.year = :year)
              AND (:status IS NULL OR i.status = :status)
            """)
    Page<Invoice> searchInvoicesForStudent(
            @Param("studentId") Long studentId,
            @Param("month") Integer month,
            @Param("year") Integer year,
            @Param("status") InvoiceStatus status,
            Pageable pageable);

    boolean existsByStudentIdAndRoomIdAndMonthAndYearAndStatusNot(
            Long studentId,
            Long roomId,
            int month,
            int year,
            InvoiceStatus status);

    boolean existsByRoomIdAndMonthAndYearAndStatusNot(
            Long roomId,
            int month,
            int year,
            InvoiceStatus status);

    @Query("""
            SELECT DISTINCT i.room.id
            FROM Invoice i
            WHERE i.room.id IN :roomIds
              AND i.month = :month
              AND i.year = :year
              AND i.status <> :status
            """)
    java.util.List<Long> findRoomIdsWithInvoicesForPeriod(
            @Param("roomIds") Collection<Long> roomIds,
            @Param("month") int month,
            @Param("year") int year,
            @Param("status") InvoiceStatus status);

    @Modifying
    @Query("""
            UPDATE Invoice i
            SET i.status = com.dormitory.management.entity.enums.InvoiceStatus.OVERDUE,
                                                                i.overdueMarkedAt = :markedAt
            WHERE i.status = com.dormitory.management.entity.enums.InvoiceStatus.UNPAID
              AND i.dueAt IS NOT NULL
              AND i.dueAt < :now
            """)
    int markOverdueInvoices(@Param("now") LocalDateTime now, @Param("markedAt") LocalDateTime markedAt);

    long countByStatus(InvoiceStatus status);

    @Query("""
            SELECT COALESCE(SUM(i.totalAmount), 0)
            FROM Invoice i
            WHERE i.status = :status
            """)
    BigDecimal sumTotalAmountByStatus(@Param("status") InvoiceStatus status);

    @Query("""
            SELECT COALESCE(SUM(i.totalAmount), 0)
            FROM Invoice i
            WHERE i.status = :status
              AND i.paidAt >= :fromTime
              AND i.paidAt < :toTime
            """)
    BigDecimal sumRevenueByPaidAtRange(
            @Param("status") InvoiceStatus status,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime);

    @Query("""
            SELECT YEAR(i.paidAt), MONTH(i.paidAt), COALESCE(SUM(i.totalAmount), 0)
            FROM Invoice i
            WHERE i.status = :status
              AND i.paidAt >= :fromTime
              AND i.paidAt < :toTime
            GROUP BY YEAR(i.paidAt), MONTH(i.paidAt)
            ORDER BY YEAR(i.paidAt), MONTH(i.paidAt)
            """)
    java.util.List<Object[]> aggregateRevenueByPaidMonth(
            @Param("status") InvoiceStatus status,
            @Param("fromTime") LocalDateTime fromTime,
            @Param("toTime") LocalDateTime toTime);
}
