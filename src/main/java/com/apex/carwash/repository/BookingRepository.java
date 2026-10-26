package com.apex.carwash.repository;

import com.apex.carwash.entity.Booking;
import com.apex.carwash.entity.BookingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;
import java.time.OffsetDateTime;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b where b.id = :id")
    Optional<Booking> findForUpdate(@Param("id") Long id);

    @Query("""
            select b from Booking b
            where (:branchId is null or b.branch.id = :branchId)
              and (:status is null or b.status = :status)
              and (:filterByDate = false or (b.startTime >= :startTime and b.startTime < :endTime))
            order by b.startTime, b.id
            """)
    List<Booking> findFiltered(Long branchId, BookingStatus status, boolean filterByDate,
                               OffsetDateTime startTime, OffsetDateTime endTime);
}
