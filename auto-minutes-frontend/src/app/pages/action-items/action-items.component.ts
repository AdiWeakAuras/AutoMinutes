import {Component, OnInit, ChangeDetectorRef} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {forkJoin, of} from 'rxjs';
import {catchError, switchMap} from 'rxjs/operators';

import {MatCardModule} from '@angular/material/card';
import {MatTableModule} from '@angular/material/table';
import {MatChipsModule} from '@angular/material/chips';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonToggleModule} from '@angular/material/button-toggle';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatButtonModule} from '@angular/material/button';

import {MeetingService} from '../../core/services/meeting.service';
import {AiProcessingService} from '../../core/services/ai-processing.service';
import {ActionItemDTO, ActionItemStatus} from '../../core/models/api.model';

interface ActionItemRow extends ActionItemDTO {
  meetingId: number;
  meetingTitle: string;
}

type StatusFilter = 'ALL' | ActionItemStatus;

@Component({
  selector: 'app-action-items-page',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatTableModule,
    MatChipsModule,
    MatIconModule,
    MatButtonToggleModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatButtonModule,
  ],
  templateUrl: './action-items.component.html',
  styleUrls: ['./action-items.component.scss'],
})
export class ActionItemsPageComponent implements OnInit {
  allItems: ActionItemRow[] = [];
  searchTerm = '';
  statusFilter: StatusFilter = 'ALL';

  loading = false;
  error: string | null = null;

  displayedColumns = ['description', 'meeting', 'assignee', 'deadline', 'status'];

  constructor(
    private meetingService: MeetingService,
    private aiProcessingService: AiProcessingService,
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

    this.meetingService
      .getAllMeetings()
      .pipe(
        switchMap((meetings) => {
          if (!meetings.length) return of([] as ActionItemRow[]);

          const requests = meetings.map((m) =>
            this.aiProcessingService.getAiResults(m.id).pipe(
              catchError(() => of([]))
            )
          );

          return forkJoin(requests).pipe(
            switchMap((resultsPerMeeting) => {
              const rows: ActionItemRow[] = [];
              resultsPerMeeting.forEach((aiResults, index) => {
                const meeting = meetings[index];
                aiResults.forEach((aiResult) => {
                  aiResult.actionItems.forEach((item) => {
                    rows.push({...item, meetingId: meeting.id, meetingTitle: meeting.title});
                  });
                });
              });
              return of(rows);
            })
          );
        })
      )
      .subscribe({
        next: (rows) => {
          this.allItems = rows;
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: () => {
          this.error = 'Nu am putut incarca action items. Verifica daca backend-ul ruleaza.';
          this.loading = false;
          this.cdr.detectChanges();
        },
      });
  }

  get filteredItems(): ActionItemRow[] {
    const term = this.searchTerm.trim().toLowerCase();
    return this.allItems.filter((item) => {
      const matchesStatus = this.statusFilter === 'ALL' || item.status === this.statusFilter;
      const matchesTerm =
        !term ||
        item.description.toLowerCase().includes(term) ||
        item.meetingTitle.toLowerCase().includes(term);
      return matchesStatus && matchesTerm;
    });
  }

  countFor(status: StatusFilter): number {
    if (status === 'ALL') return this.allItems.length;
    return this.allItems.filter((i) => i.status === status).length;
  }

  statusClass(status: ActionItemStatus): string {
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
}
