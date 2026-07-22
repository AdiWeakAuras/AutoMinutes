import {Component, OnInit, ChangeDetectorRef} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {forkJoin, of} from 'rxjs';
import {catchError, switchMap} from 'rxjs/operators';
import {MatCardModule} from '@angular/material/card';
import {MatTableModule} from '@angular/material/table';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatDialog, MatDialogModule} from '@angular/material/dialog';
import {MeetingService} from '../../core/services/meeting.service';
import {AttendeeService} from '../../core/services/attendee.service';
import {AttendeeDTO, MeetingSummaryDTO} from '../../core/models/api.model';
import {
  AttendeeDialogComponent,
  AttendeeDialogData,
  MeetingOption,
} from '../attendee-dialog/attendee-dialog.component';

interface AttendeeRow extends AttendeeDTO {
  meetingsCount: number;
  firstMeetingId: number;
}

@Component({
  selector: 'app-attendees-page',
  standalone: true,
  imports: [
    CommonModule, FormsModule, MatCardModule, MatTableModule, MatIconModule,
    MatButtonModule, MatFormFieldModule, MatInputModule, MatTooltipModule,
    MatProgressSpinnerModule, MatDialogModule,
  ],
  templateUrl: './attendees.component.html',
  styleUrls: ['./attendees.component.scss'],
})
export class AttendeesPageComponent implements OnInit {
  attendees: AttendeeRow[] = [];
  meetings: MeetingSummaryDTO[] = [];
  searchTerm = '';
  loading = false;
  error: string | null = null;
  displayedColumns = ['name', 'email', 'role', 'meetings', 'actions'];

  constructor(
    private meetingService: MeetingService,
    private attendeeService: AttendeeService,
    private dialog: MatDialog,
    private cdr: ChangeDetectorRef
  ) {
  }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = null;
    this.cdr.detectChanges();

    this.meetingService.getAllMeetings().pipe(
      switchMap((meetings) => {
        this.meetings = meetings;
        if (!meetings.length) return of([] as AttendeeRow[]);
        const requests = meetings.map((m) =>
          this.attendeeService.getAttendeesForMeeting(m.id).pipe(
            catchError(() => of([] as AttendeeDTO[]))
          )
        );
        return forkJoin(requests).pipe(
          switchMap((attendeesPerMeeting) => {
            const byId = new Map<number, AttendeeRow>();
            attendeesPerMeeting.forEach((list, index) => {
              const meetingId = meetings[index].id;
              list.forEach((attendee) => {
                const existing = byId.get(attendee.id);
                if (existing) {
                  existing.meetingsCount += 1;
                } else {
                  byId.set(attendee.id, {...attendee, meetingsCount: 1, firstMeetingId: meetingId});
                }
              });
            });
            return of(Array.from(byId.values()));
          })
        );
      })
    ).subscribe({
      next: (rows) => {
        this.attendees = rows.sort((a, b) => a.name.localeCompare(b.name));
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = 'Nu am putut incarca lista de participanti.';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  get filteredAttendees(): AttendeeRow[] {
    const term = this.searchTerm.trim().toLowerCase();
    if (!term) return this.attendees;
    return this.attendees.filter(
      (a) => a.name.toLowerCase().includes(term) || a.email.toLowerCase().includes(term)
    );
  }

  get meetingOptions(): MeetingOption[] {
    return this.meetings.map((m) => ({id: m.id, title: m.title}));
  }

  openAddDialog(): void {
    const dialogRef = this.dialog.open(AttendeeDialogComponent, {
      data: {
        meetingId: 0, meetingTitle: '', existingAttendees: [],
        meetingOptions: this.meetingOptions,
      } as AttendeeDialogData,
      width: '480px', disableClose: true,
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) this.load();
    });
  }

  openEditDialog(attendee: AttendeeRow): void {
    const meetingTitle = this.meetings.find((m) => m.id === attendee.firstMeetingId)?.title ?? '';
    const dialogRef = this.dialog.open(AttendeeDialogComponent, {
      data: {
        meetingId: attendee.firstMeetingId, meetingTitle,
        existingAttendees: [], editAttendee: attendee,
      } as AttendeeDialogData,
      width: '480px', disableClose: true,
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) this.load();
    });
  }

  initials(name: string): string {
    return name.split(' ').map((p) => p[0]).join('').toUpperCase();
  }
}
