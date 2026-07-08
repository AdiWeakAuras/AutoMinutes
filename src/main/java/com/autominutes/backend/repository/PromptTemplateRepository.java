package com.autominutes.backend.repository;

import com.autominutes.backend.entity.PromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, Long> {
}