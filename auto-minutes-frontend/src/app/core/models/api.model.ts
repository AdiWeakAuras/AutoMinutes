// Oglindă exactă a enum-urilor din com.autominutes.backend.entity

export type ProcessingStatus = 'PENDING' | 'PROCESSING' | 'DONE' | 'FAILED';
export type ActionItemStatus = 'OPEN' | 'IN_PROGRESS' | 'DONE' | 'UNKNOWN';

// --- Response DTOs (com.autominutes.backend.dto) ---

export interface AttendeeDTO {
  id: number;
  name: string;
  email: string;
  role: string | null;
}

export interface ActionItemDTO {
  id: number;
  description: string;
  proposedAssignee: string | null;
  deadline: string | null; // LocalDate -> "YYYY-MM-DD"
  status: ActionItemStatus;
}

export interface AIResultDTO {
  id: number;
  summary: string | null;
  detailedSummary: string | null;
  decisions: string | null;
  followUpNotes: string | null;
  actionItems: ActionItemDTO[];
}

export interface TranscriptDTO {
  id: number;
  content: string;
  createdAt: string; // LocalDateTime -> ISO string
  aiResults: AIResultDTO[];
}

export interface MeetingDTO {
  id: number;
  title: string;
  description: string | null;
  meetingDate: string; // LocalDateTime -> ISO string
  processingStatus: ProcessingStatus;
  transcript: TranscriptDTO | null;
  attendees: AttendeeDTO[];
}

export interface MeetingSummaryDTO {
  id: number;
  title: string;
  meetingDate: string;
  processingStatus: ProcessingStatus;
  attendeeCount: number;
}

// --- Request DTOs ---

export interface MeetingCreateRequest {
  title: string;
  description?: string | null;
  meetingDate: string; // ISO LocalDateTime
}

export interface MeetingUpdateRequest {
  title?: string | null;
  description?: string | null;
  meetingDate?: string | null;
  processingStatus?: ProcessingStatus | null;
}

export interface TranscriptCreateRequest {
  content: string;
}

export interface TranscriptUpdateRequest {
  content: string;
}

export interface AttendeeCreateRequest {
  name: string;
  email: string;
  role?: string | null;
}

export interface AttendeeUpdateRequest {
  name?: string | null;
  email?: string | null;
  role?: string | null;
}

export interface ProcessMeetingRequest {
  promptTemplateId?: number | null;
}

// --- Error shape (GlobalExceptionHandler) ---
// Presupus din ErrorResponse(status, error, message, path[, details]) - de confirmat
export interface ErrorResponse {
  status: number;
  error: string;
  message: string;
  path: string;
  details?: string[];
}
