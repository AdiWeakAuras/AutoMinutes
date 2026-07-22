import {Component, Inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';

import {MatDialogModule, MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';

import {MeetingDTO} from '../../core/models/api.model';
import {MatIcon} from '@angular/material/icon';

export type MeetingFormMode = 'create' | 'edit';

export interface MeetingFormDialogData {
  mode: MeetingFormMode;
  meeting?: MeetingDTO;
}

export interface MeetingFormResult {
  title: string;
  description: string | null;
  meetingDate: string;
}

@Component({
  selector: 'app-meeting-form-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIcon],
  templateUrl: './meeting-form-dialog.component.html',
  styleUrls: ['./meeting-form-dialog.component.scss'],
})
export class MeetingFormDialogComponent {
  title: string;
  description: string;
  // format necesar pentru <input type="datetime-local">: "YYYY-MM-DDTHH:mm"
  meetingDateLocal: string;

  constructor(
    private dialogRef: MatDialogRef<MeetingFormDialogComponent, MeetingFormResult>,
    @Inject(MAT_DIALOG_DATA) public data: MeetingFormDialogData
  ) {
    const meeting = data.meeting;
    this.title = meeting?.title ?? '';
    this.description = meeting?.description ?? '';
    this.meetingDateLocal = meeting ? this.toLocalInputValue(meeting.meetingDate) : this.defaultDateTime();
  }

  get isEdit(): boolean {
    return this.data.mode === 'edit';
  }

  get isValid(): boolean {
    return this.title.trim().length > 0 && this.meetingDateLocal.length > 0;
  }

  submit(): void {
    if (!this.isValid) return;
    this.dialogRef.close({
      title: this.title.trim(),
      description: this.description.trim() || null,
      meetingDate: this.meetingDateLocal,
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }

  private toLocalInputValue(iso: string): string {
    // "2026-07-03T14:00:00" -> "2026-07-03T14:00"
    return iso.slice(0, 16);
  }

  private defaultDateTime(): string {
    const now = new Date();
    now.setSeconds(0, 0);
    const pad = (n: number) => n.toString().padStart(2, '0');
    return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}T${pad(now.getHours())}:${pad(now.getMinutes())}`;
  }
}
