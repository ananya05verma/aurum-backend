package com.aurum.ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface GlobalAIUsageRepository extends JpaRepository<GlobalAIUsage, Long> {

    Optional<GlobalAIUsage> findByDate(LocalDate date);
}