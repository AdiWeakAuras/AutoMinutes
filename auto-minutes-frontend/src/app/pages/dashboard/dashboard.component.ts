import {Component, OnInit, ChangeDetectorRef} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RouterLink} from '@angular/router';

import {MatCardModule} from '@angular/material/card';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatChipsModule} from '@angular/material/chips';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatTooltipModule} from '@angular/material/tooltip';

import {MeetingService} from '../../core/services/meeting.service';
import {MeetingSummaryDTO, ProcessingStatus} from '../../core/models/api.model';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardPageComponent implements OnInit {
  meetings: MeetingSummaryDTO[] = [];
  loading = false;
  error: string | null = null;

  constructor(private meetingService: MeetingService, private cdr: ChangeDetectorRef) {
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = null;
    this.cdr.detectChanges();
    this.meetingService.getAllMeetings().subscribe({
      next: (meetings) => {
        this.meetings = meetings;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Nu am putut incarca datele. Verifica daca backend-ul ruleaza.';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  get recentMeetings(): MeetingSummaryDTO[] {
    return [...this.meetings]
      .sort((a, b) => new Date(b.meetingDate).getTime() - new Date(a.meetingDate).getTime())
      .slice(0, 5);
  }

  get totalMeetings(): number {
    return this.meetings.length;
  }

  get completedCount(): number {
    return this.meetings.filter((m) => m.processingStatus === 'DONE').length;
  }

  get pendingCount(): number {
    return this.meetings.filter((m) => m.processingStatus === 'PENDING').length;
  }

  get failedCount(): number {
    return this.meetings.filter((m) => m.processingStatus === 'FAILED').length;
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
