package com.spaceres.repository;

import com.spaceres.entity.Space;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpaceRepository extends JpaRepository<Space, Long> {

    // 사용 가능한 공간 목록
    List<Space> findByStatusOrderByNameAsc(Space.SpaceStatus status);

    // ── Pessimistic Write Lock ─────────────────────────────
    // 예약 생성 전, 공간 정보를 잠금 상태로 조회
    // SELECT ... FOR UPDATE → 다른 트랜잭션이 동시에 접근 불가
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Space s WHERE s.id = :id")
    Optional<Space> findByIdWithLock(@Param("id") Long id);
}
