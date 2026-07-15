package com.autominutes.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "action_item")
public class ActionItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ai_result_id", nullable = false)
  private AIResult aiResult;

  @Column(nullable = false, length = 500)
  private String description;

  @Column(name = "proposed_assignee")
  private String proposedAssignee;

  private LocalDate deadline;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ActionItemStatus status;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public AIResult getAiResult() {
    return aiResult;
  }

  public void setAiResult(AIResult aiResult) {
    this.aiResult = aiResult;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getProposedAssignee() {
    return proposedAssignee;
  }

  public void setProposedAssignee(String proposedAssignee) {
    this.proposedAssignee = proposedAssignee;
  }

  public LocalDate getDeadline() {
    return deadline;
  }

  public void setDeadline(LocalDate deadline) {
    this.deadline = deadline;
  }

  public ActionItemStatus getStatus() {
    return status;
  }

  public void setStatus(ActionItemStatus status) {
    this.status = status;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
