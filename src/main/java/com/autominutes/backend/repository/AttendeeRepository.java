package com.autominutes.backend.repository;

import com.autominutes.backend.entity.Attendee;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendeeRepository extends JpaRepository<Attendee, Long> {
}