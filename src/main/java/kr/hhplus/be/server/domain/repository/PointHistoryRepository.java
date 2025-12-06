package kr.hhplus.be.server.domain.repository;


import kr.hhplus.be.server.domain.entity.PointHistory;
import kr.hhplus.be.server.domain.entity.PointTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    List<PointHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<PointHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT ph FROM PointHistory ph WHERE ph.userId = :userId AND ph.type = :type ORDER BY ph.createdAt DESC")
    List<PointHistory> findByUserIdAndType(@Param("userId") Long userId,
                                           @Param("type") PointTransactionType type);

    @Query("SELECT ph FROM PointHistory ph WHERE ph.userId = :userId AND ph.createdAt BETWEEN :startDate AND :endDate ORDER BY ph.createdAt DESC")
    List<PointHistory> findByUserIdAndDateRange(@Param("userId") Long userId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);
}
