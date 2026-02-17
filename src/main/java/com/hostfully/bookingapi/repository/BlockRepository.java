package com.hostfully.bookingapi.repository;

import com.hostfully.bookingapi.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface BlockRepository extends JpaRepository<Block, Long> {

    @Query("SELECT b FROM Block b WHERE b.propertyId = :propertyId " +
           "AND b.startDate < :endDate AND b.endDate > :startDate")
    List<Block> findOverlapping(@Param("propertyId") String propertyId,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate);
}
