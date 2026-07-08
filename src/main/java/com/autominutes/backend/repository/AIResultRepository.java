package com.autominutes.backend.repository;

import com.autominutes.backend.entity.AIResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AIResultRepository extends JpaRepository<AIResult, Long> {
}