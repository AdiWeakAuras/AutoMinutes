import {Component, OnInit, ChangeDetectorRef} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';

import {MatCardModule} from '@angular/material/card';
import {MatTabsModule} from '@angular/material/tabs';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatChipsModule} from '@angular/material/chips';
import {MatPaginatorModule, PageEvent} from '@angular/material/paginator';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatDialog, MatDialogModule} from '@angular/material/dialog';

import {MeetingService} from '../../core/services/meeting.service';
import {AiProcessingService} from '../../core/services/ai-processing.service';
import {AttendeeService} from '../../core/services/attendee.service';
import {
  AttendeeDTO,
  MeetingDTO,
  MeetingSummaryDTO,
  ProcessingStatus,
  ActionItemStatus
} from '../../core/models/api.model';
import {
  MeetingFormDialogComponent,
  MeetingFormResult,
} from '../meeting-form-dialog/meeting-form-dialog.component';
import {
  TranscriptDialogComponent,
  TranscriptDialogData,
} from '../transcript-dialog/transcript-dialog.component';
import {
  AttendeeDialogComponent,
  AttendeeDialogData,
} from '../attendee-dialog/attendee-dialog.component';

@Component({
  selector: 'app-meetings-page',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatTabsModule,
    MatIconModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatChipsModule,
    MatPaginatorModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
    MatDialogModule,
  ],
  templateUrl: './meetings.component.html',
  styleUrls: ['./meetings.component.scss'],
})
export class MeetingsPageComponent implements OnInit {
  meetings: MeetingSummaryDTO[] = [];
  selectedMeeting: MeetingDTO | null = null;

  searchTerm = '';
  pageIndex = 0;
  pageSize = 7;

  loadingList = false;
  loadingDetail = false;
  listError: string | null = null;
  detailError: string | null = null;

  reprocessing = false;
  reprocessError: string | null = null;
  deleting = false;
  formError: string | null = null;

  constructor(
    private meetingService: MeetingService,
    private aiProcessingService: AiProcessingService,
    private attendeeService: AttendeeService,
    private cdr: ChangeDetectorRef,
    private dialog: MatDialog
  ) {
  }

  ngOnInit(): void {
    this.loadMeetings();
  }

  // ─── Date loading ───────────────────────────────────────────────────────────

  loadMeetings(): void {
    this.loadingList = true;
    this.listError = null;
    this.cdr.detectChanges();

    this.meetingService.getAllMeetings().subscribe({
      next: (meetings) => {
        this.meetings = meetings;
        this.loadingList = false;
        this.cdr.detectChanges();
        if (meetings.length && !this.selectedMeeting) {
          this.selectMeeting(meetings[0].id);
        }
      },
      error: () => {
        this.listError = 'Nu am putut incarca lista de meetinguri. Verifica daca backend-ul ruleaza.';
        this.loadingList = false;
        this.cdr.detectChanges();
      },
    });
  }

  selectMeeting(id: number): void {
    this.loadingDetail = true;
    this.detailError = null;
    this.selectedMeeting = null;
    this.cdr.detectChanges();

    this.meetingService.getMeetingById(id).subscribe({
      next: (meeting) => {
        this.selectedMeeting = meeting;
        this.loadingDetail = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.detailError = 'Nu am putut incarca detaliile acestui meeting.';
        this.loadingDetail = false;
        this.cdr.detectChanges();
      },
    });
  }

  // ─── Getters ─────────────────────────────────────────────────────────────────

  get filteredMeetings(): MeetingSummaryDTO[] {
    const term = this.searchTerm.trim().toLowerCase();
    if (!term) return this.meetings;
    return this.meetings.filter((m) => m.title.toLowerCase().includes(term));
  }

  /**
   * AIResult-urile se acumuleaza (reprocesarea nu le sterge pe cele vechi), iar Hibernate
   * nu garanteaza ordinea colectiei @OneToMany fara @OrderBy pe backend. De aceea NU folosim
   * aiResults[0] ca fiind "cel mai recent" - alegem explicit id-ul maxim (auto-increment,
   * deci cel mai mare id = cea mai recenta inregistrare).
   */
  get latestAiResult() {
    const results = this.selectedMeeting?.transcript?.aiResults;
    if (!results || !results.length) return null;
    return results.reduce((latest, r) => (r.id > latest.id ? r : latest), results[0]);
  }

  get hasBeenProcessed(): boolean {
    return !!this.latestAiResult;
  }

  get hasTranscript(): boolean {
    return !!this.selectedMeeting?.transcript;
  }

  // ─── Meeting CRUD ─────────────────────────────────────────────────────────────

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(MeetingFormDialogComponent, {
      data: {mode: 'create'},
      width: '480px',
      disableClose: true,
    });

    dialogRef.afterClosed().subscribe((result: MeetingFormResult | undefined) => {
      if (!result) return;
      this.formError = null;
      this.meetingService
        .createMeeting({
          title: result.title,
          description: result.description,
          meetingDate: result.meetingDate,
        })
        .subscribe({
          next: (created) => {
            this.loadMeetings();
            this.selectMeeting(created.id);
          },
          error: () => {
            this.formError = 'Crearea meeting-ului a esuat.';
            this.cdr.detectChanges();
          },
        });
    });
  }

  openEditDialog(): void {
    if (!this.selectedMeeting) return;
    const meetingId = this.selectedMeeting.id;

    const dialogRef = this.dialog.open(MeetingFormDialogComponent, {
      data: {mode: 'edit', meeting: this.selectedMeeting},
      width: '480px',
      disableClose: true,
    });

    dialogRef.afterClosed().subscribe((result: MeetingFormResult | undefined) => {
      if (!result) return;
      this.formError = null;
      this.meetingService
        .updateMeeting(meetingId, {
          title: result.title,
          description: result.description,
          meetingDate: result.meetingDate,
        })
        .subscribe({
          next: () => {
            this.loadMeetings();
            this.selectMeeting(meetingId);
          },
          error: () => {
            this.formError = 'Actualizarea meeting-ului a esuat.';
            this.cdr.detectChanges();
          },
        });
    });
  }

  confirmDeleteMeeting(): void {
    if (!this.selectedMeeting || this.deleting) return;

    const meeting = this.selectedMeeting;
    const confirmed = window.confirm(
      `Esti sigur ca vrei sa stergi "${meeting.title}"?\n\nAceasta actiune va sterge si transcriptul, rezultatele AI si toate action items asociate. Nu poate fi anulata.`
    );
    if (!confirmed) return;

    this.deleting = true;
    this.cdr.detectChanges();

    this.meetingService.deleteMeeting(meeting.id).subscribe({
      next: () => {
        this.deleting = false;
        this.selectedMeeting = null;
        this.loadMeetings();
      },
      error: () => {
        this.deleting = false;
        this.cdr.detectChanges();
      },
    });
  }

  // ─── Transcript ───────────────────────────────────────────────────────────────

  openTranscriptDialog(): void {
    if (!this.selectedMeeting) return;

    const data: TranscriptDialogData = {
      meetingId: this.selectedMeeting.id,
      meetingTitle: this.selectedMeeting.title,
      existingTranscript: this.selectedMeeting.transcript ?? null,
    };

    const dialogRef = this.dialog.open(TranscriptDialogComponent, {
      data,
      width: '640px',
      disableClose: true,
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.selectMeeting(this.selectedMeeting!.id);
      }
    });
  }

  // ─── Attendees ────────────────────────────────────────────────────────────────

  openAddAttendeeDialog(): void {
    if (!this.selectedMeeting) return;

    const data: AttendeeDialogData = {
      meetingId: this.selectedMeeting.id,
      meetingTitle: this.selectedMeeting.title,
      existingAttendees: this.selectedMeeting.attendees,
    };

    const dialogRef = this.dialog.open(AttendeeDialogComponent, {
      data,
      width: '480px',
      disableClose: true,
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.selectMeeting(this.selectedMeeting!.id);
      }
    });
  }

  openEditAttendeeDialog(attendee: AttendeeDTO): void {
    if (!this.selectedMeeting) return;

    const data: AttendeeDialogData = {
      meetingId: this.selectedMeeting.id,
      meetingTitle: this.selectedMeeting.title,
      existingAttendees: this.selectedMeeting.attendees,
      editAttendee: attendee,
    };

    const dialogRef = this.dialog.open(AttendeeDialogComponent, {
      data,
      width: '480px',
      disableClose: true,
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.selectMeeting(this.selectedMeeting!.id);
      }
    });
  }

  removeAttendee(attendeeId: number): void {
    if (!this.selectedMeeting) return;
    const meetingId = this.selectedMeeting.id;

    this.attendeeService.removeAttendee(meetingId, attendeeId).subscribe({
      next: () => this.selectMeeting(meetingId),
      error: () => this.selectMeeting(meetingId),
    });
  }

  // ─── AI Processing ────────────────────────────────────────────────────────────

  reprocessMeeting(): void {
    if (!this.selectedMeeting || this.reprocessing) return;

    const meetingId = this.selectedMeeting.id;
    this.reprocessing = true;
    this.reprocessError = null;
    this.cdr.detectChanges();

    this.aiProcessingService.processTranscript(meetingId).subscribe({
      next: () => {
        this.reprocessing = false;
        this.cdr.detectChanges();
        this.selectMeeting(meetingId);
        this.loadMeetings();
      },
      error: (err) => {
        this.reprocessing = false;
        this.reprocessError =
          err?.status === 503
            ? 'Modelul AI nu a putut fi contactat. Verifica daca Ollama ruleaza.'
            : err?.status === 502
              ? 'Modelul AI a raspuns intr-un format care nu a putut fi procesat.'
              : 'Reprocesarea a esuat.';
        this.cdr.detectChanges();
        this.loadMeetings();
      },
    });
  }

  // ─── Helpers ──────────────────────────────────────────────────────────────────

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
  }

  statusClass(status: ProcessingStatus): string {
    switch (status) {
      case 'DONE':
        return 'status-chip status-chip--completed';
      case 'PROCESSING':
        return 'status-chip status-chip--processing';
      case 'PENDING':
        return 'status-chip status-chip--pending';
      case 'FAILED':
        return 'status-chip status-chip--failed';
      default:
        return 'status-chip';
    }
  }

  actionItemStatusClass(status: ActionItemStatus): string {
    switch (status) {
      case 'OPEN':
        return 'status-chip status-chip--open';
      case 'IN_PROGRESS':
        return 'status-chip status-chip--progress';
      case 'DONE':
        return 'status-chip status-chip--done';
      case 'UNKNOWN':
      default:
        return 'status-chip status-chip--unknown';
    }
  }

  formatDate(iso: string): string {
    return new Date(iso).toLocaleString('ro-RO', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }
}
