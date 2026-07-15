package com.autominutes.backend.service.impl;

import com.autominutes.backend.entity.ProcessingStatus;
import com.autominutes.backend.repository.MeetingRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles isolated status updates on {@code Meeting} that must survive regardless of what happens
 * in the caller's transaction — most notably, marking a meeting as FAILED even when the surrounding
 * transaction is about to roll back due to the exception that triggered the failure.
 *
 * <p>Kept as a separate bean (not a private/protected method on {@link AiProcessingServiceImpl}) so
 * that {@code @Transactional(REQUIRES_NEW)} actually takes effect: self-invocation within the same
 * class bypasses Spring's proxy and silently ignores the annotation.
 */
@Component
public class MeetingStatusUpdater {

  private final MeetingRepository meetingRepository;

  public MeetingStatusUpdater(MeetingRepository meetingRepository) {
    this.meetingRepository = meetingRepository;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void markAsFailed(Long meetingId) {
    meetingRepository
        .findById(meetingId)
        .ifPresent(
            m -> {
              m.setProcessingStatus(ProcessingStatus.FAILED);
              meetingRepository.save(m);
            });
  }
}
