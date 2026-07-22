import {Component, Inject, ChangeDetectorRef} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';

import {MatDialogModule, MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatIconModule} from '@angular/material/icon';

import {TranscriptService} from '../../core/services/transcript.service';
import {TranscriptDTO} from '../../core/models/api.model';

export interface TranscriptDialogData {
  meetingId: number;
  meetingTitle: string;
  // daca exista deja un transcript, il editam; daca nu, cream unul nou
  existingTranscript: TranscriptDTO | null;
}

@Component({
  selector: 'app-transcript-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatIconModule,
  ],
  templateUrl: './transcript-dialog.component.html',
  styleUrls: ['./transcript-dialog.component.scss'],
})
export class TranscriptDialogComponent {
  content: string;
  saving = false;
  error: string | null = null;

  get isEdit(): boolean {
    return this.data.existingTranscript !== null;
  }

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: TranscriptDialogData,
    private dialogRef: MatDialogRef<TranscriptDialogComponent>,
    private transcriptService: TranscriptService,
    private cdr: ChangeDetectorRef
  ) {
    // precompletat cu textul existent daca e edit, gol daca e create
    this.content = data.existingTranscript?.content ?? '';
  }

  save(): void {
    const trimmed = this.content.trim();
    if (!trimmed) return;

    this.saving = true;
    this.error = null;
    this.cdr.detectChanges();

    const request$ = this.isEdit
      ? this.transcriptService.updateTranscript(this.data.meetingId, {content: trimmed})
      : this.transcriptService.submitTranscript(this.data.meetingId, {content: trimmed});

    request$.subscribe({
      next: (transcript) => {
        this.saving = false;
        this.cdr.detectChanges();
        // inchidem dialogul si intoarcem noul transcript
        this.dialogRef.close(transcript);
      },
      error: (err) => {
        this.saving = false;
        this.error =
          err?.status === 409
            ? 'Exista deja un transcript pentru acest meeting. Foloseste Edit Transcript.'
            : 'Salvarea a esuat. Incearca din nou.';
        this.cdr.detectChanges();
      },
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
