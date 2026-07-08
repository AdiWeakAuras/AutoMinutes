package com.autominutes.backend.repository;

import com.autominutes.backend.entity.ActionItem;
import com.autominutes.backend.entity.Attendee;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionItemRepository extends JpaRepository<ActionItem, Long> {
}