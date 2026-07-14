package com.autominutes.backend.repository;

import com.autominutes.backend.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    @Query("""
            SELECT m.id AS id,
                   m.title AS title,
                   m.meetingDate AS meetingDate,
                   m.processingStatus AS processingStatus,
                   COUNT(ma) AS attendeeCount
            FROM Meeting m
            LEFT JOIN m.meetingAttendees ma
            GROUP BY m.id, m.title, m.meetingDate, m.processingStatus
            """)
    List<MeetingSummaryProjection> findAllSummaries();
}