import {Component, Inject, ChangeDetectorRef} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';

import {MatDialogModule, MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatIconModule} from '@angular/material/icon';

import {AttendeeService} from '../../core/services/attendee.service';
import {AttendeeDTO} from '../../core/models/api.model';

export interface MeetingOption {
  id: number;
  title: string;
}

export interface AttendeeDialogData {
  meetingId: number;
  meetingTitle: string;
  existingAttendees: AttendeeDTO[];
  editAttendee?: AttendeeDTO;
  meetingOptions?: MeetingOption[];
}

@Component({
  selector: 'app-attendee-dialog',
  standalone: true,
  imports: [
    CommonModule, FormsModule, MatDialogModule, MatButtonModule,
    MatFormFieldModule, MatInputModule, MatSelectModule,
    MatProgressSpinnerModule, MatIconModule,
  ],
  templateUrl: './attendee-dialog.component.html',
  styleUrls: ['./attendee-dialog.component.scss'],
})
export class AttendeeDialogComponent {
  name: string;
  email: string;
  role: string;
  selectedMeetingId: number;

  saving = false;
  error: string | null = null;

  get isEdit(): boolean {
    return !!this.data.editAttendee;
  }

  get needsMeetingSelect(): boolean {
    return !!(this.data.meetingOptions?.length && !this.isEdit);
  }

  get isValid(): boolean {
    const hasMeeting = this.needsMeetingSelect ? this.selectedMeetingId > 0 : true;
    return this.name.trim().length > 0 && this.email.trim().length > 0 && hasMeeting;
  }

  get isDuplicate(): boolean {
    if (this.isEdit) {
      return this.data.existingAttendees.some(
        (a) =>
          a.email.toLowerCase() === this.email.trim().toLowerCase() &&
          a.id !== this.data.editAttendee!.id
      );
    }
    return this.data.existingAttendees.some(
      (a) => a.email.toLowerCase() === this.email.trim().toLowerCase()
    );
  }

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: AttendeeDialogData,
    private dialogRef: MatDialogRef<AttendeeDialogComponent>,
    private attendeeService: AttendeeService,
    private cdr: ChangeDetectorRef
  ) {
    this.name = data.editAttendee?.name ?? '';
    this.email = data.editAttendee?.email ?? '';
    this.role = data.editAttendee?.role ?? '';
    this.selectedMeetingId = data.meetingId ?? 0;
  }

  save(): void {
    if (!this.isValid || this.saving) return;

    this.saving = true;
    this.error = null;
    this.cdr.detectChanges();

    const meetingId = this.needsMeetingSelect
      ? this.selectedMeetingId
      : this.data.meetingId;

    const request$ = this.isEdit
      ? this.attendeeService.updateAttendee(meetingId, this.data.editAttendee!.id, {
        name: this.name.trim(),
        email: this.email.trim(),
        role: this.role.trim() || null,
      })
      : this.attendeeService.addAttendee(meetingId, {
        name: this.name.trim(),
        email: this.email.trim(),
        role: this.role.trim() || null,
      });

    request$.subscribe({
      next: (attendee) => {
        this.saving = false;
        this.cdr.detectChanges();
        this.dialogRef.close(attendee);
      },
      error: (err) => {
        this.saving = false;
        this.error =
          err?.status === 409
            ? 'Acest email este deja folosit de un alt participant.'
            : this.isEdit
              ? 'Actualizarea a esuat. Incearca din nou.'
              : 'Adaugarea a esuat. Incearca din nou.';
        this.cdr.detectChanges();
      },
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
