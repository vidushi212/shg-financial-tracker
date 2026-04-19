package com.shg.repository;

import com.shg.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByShgGroupId(Long shgGroupId);
    List<Transaction> findByMemberId(Long memberId);
    List<Transaction> findByType(String type);
    List<Transaction> findByShgGroupIdAndType(Long shgGroupId, String type);
    List<Transaction> findByTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Transaction> findByShgGroupIdAndTransactionDateBetween(Long shgGroupId, LocalDateTime startDate, LocalDateTime endDate);
}
