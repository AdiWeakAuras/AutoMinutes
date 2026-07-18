import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';

import {MatSidenavModule} from '@angular/material/sidenav';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatListModule} from '@angular/material/list';
import {MatCardModule} from '@angular/material/card';
import {MatTabsModule} from '@angular/material/tabs';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatChipsModule} from '@angular/material/chips';
import {MatPaginatorModule, PageEvent} from '@angular/material/paginator';
import {MatBadgeModule} from '@angular/material/badge';
import {MatMenuModule} from '@angular/material/menu';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {MatDividerModule} from '@angular/material/divider';
import {MatTooltipModule} from '@angular/material/tooltip';

type MeetingStatus = 'Completed' | 'Processed' | 'Processing' | 'Pending';

interface ActionItem {
  description: string;
  done: boolean;
}

interface Meeting {
  id: number;
  title: string;
  date: string;
  time: string;
  status: MeetingStatus;
  description: string;
  attendeesCount: number;
  summary: string;
  keyPoints: string[];
  decisions: string[];
  followUpNotes: string[];
  actionItems: ActionItem[];
  transcriptPreview: { speaker: string; line: string }[];
  createdBy: string;
  createdAt: string;
  updatedAt: string;
  aiModel: string;
  aiGeneratedOn: string;
}

const NAV_ITEMS = [
  {label: 'Dashboard', icon: 'space_dashboard'},
  {label: 'Meetings', icon: 'event_note'},
  {label: 'Action Items', icon: 'checklist'},
  {label: 'Attendees', icon: 'group'},
  {label: 'Prompts', icon: 'description'},
  {label: 'Settings', icon: 'settings'},
];

const MOCK_MEETINGS: Meeting[] = [
  {
    id: 1,
    title: 'Q2 Retrospective',
    date: 'Jul 3, 2026',
    time: '14:00 - 15:30',
    status: 'Completed',
    description:
      'Second quarter retrospective meeting with the team to discuss achievements, challenges and improvements.',
    attendeesCount: 3,
    summary:
      'Q2 was successful. All planned features were delivered on time. The team will continue with the current development pace in Q3.',
    keyPoints: [
      'All 8 planned tasks for Q2 were completed.',
      'The product launch was successful and well received.',
      'Performance improvements were noted in the backend.',
      'Need to improve documentation and internal processes.',
      'Team wants more automated testing in the next quarter.',
    ],
    decisions: [
      'Continue with the current development pace.',
      'Invest in automated testing tools.',
      'Improve onboarding documentation.',
    ],
    followUpNotes: [
      'Ana will prepare the Q3 sprint plan.',
      'Radu will research testing tools.',
      'Maria will coordinate with the stakeholders for feedback.',
    ],
    actionItems: [
      {description: 'Research and shortlist automated testing tools', done: false},
      {description: 'Draft Q3 sprint plan', done: true},
    ],
    transcriptPreview: [
      {speaker: 'Maria', line: 'What went well during Q2?'},
      {speaker: 'Ana', line: 'We delivered all planned features on time and the clients are happy with the results.'},
      {speaker: 'Radu', line: 'The backend performance improved a lot. We reduced response time by 30%.'},
      {speaker: 'Maria', line: 'What are the main challenges we faced?'},
      {speaker: 'Radu', line: 'The testing was manual in many areas, which slowed us down.'},
      {speaker: 'Ana', line: "Documentation is another area that needs more attention."},
      {speaker: 'Maria', line: "Great. Let's continue this pace in Q3 and focus on automation."},
    ],
    createdBy: 'Ana Popescu',
    createdAt: 'Jul 3, 2026 13:45',
    updatedAt: 'Jul 3, 2026 15:32',
    aiModel: 'GPT-4.1',
    aiGeneratedOn: 'Jul 3, 2026 15:32',
  },
  {
    id: 2,
    title: 'Sprint Planning – Sprint 12',
    date: 'Jul 1, 2026',
    time: '10:00 - 11:00',
    status: 'Processed',
    description: 'Planning session for sprint 12 backlog and capacity.',
    attendeesCount: 5,
    summary: 'Sprint 12 backlog was finalized with a focus on the reporting module.',
    keyPoints: ['Backlog groomed for 18 story points.', 'Reporting module prioritized.'],
    decisions: ['Reporting module ships first this sprint.'],
    followUpNotes: ['Vlad to break down reporting tasks.'],
    actionItems: [{description: 'Break down reporting module tasks', done: false}],
    transcriptPreview: [{speaker: 'Vlad', line: "Let's prioritize the reporting module this sprint."}],
    createdBy: 'Ana Popescu',
    createdAt: 'Jul 1, 2026 09:40',
    updatedAt: 'Jul 1, 2026 11:05',
    aiModel: 'GPT-4.1',
    aiGeneratedOn: 'Jul 1, 2026 11:05',
  },
  {
    id: 3,
    title: 'Product Roadmap Review',
    date: 'Jun 25, 2026',
    time: '11:00 - 12:00',
    status: 'Completed',
    description: 'Quarterly roadmap alignment with stakeholders.',
    attendeesCount: 6,
    summary: 'Roadmap for H2 was aligned across product and engineering.',
    keyPoints: ['H2 roadmap presented and discussed.'],
    decisions: ['Freeze H2 roadmap by end of week.'],
    followUpNotes: ['Elena to share roadmap doc with clients.'],
    actionItems: [{description: 'Share roadmap doc with clients', done: true}],
    transcriptPreview: [{speaker: 'Elena', line: 'Here is the proposed roadmap for H2.'}],
    createdBy: 'Ana Popescu',
    createdAt: 'Jun 25, 2026 10:40',
    updatedAt: 'Jun 25, 2026 12:10',
    aiModel: 'GPT-4.1',
    aiGeneratedOn: 'Jun 25, 2026 12:10',
  },
  {
    id: 4,
    title: 'Client Sync – Acme Corp',
    date: 'Jun 20, 2026',
    time: '15:00 - 15:45',
    status: 'Processed',
    description: 'Sync call with Acme Corp regarding phase 2 delivery.',
    attendeesCount: 4,
    summary: 'Acme Corp confirmed phase 2 scope and timeline.',
    keyPoints: ['Phase 2 scope confirmed.'],
    decisions: ['Kickoff phase 2 next Monday.'],
    followUpNotes: ['Send updated contract to Acme Corp.'],
    actionItems: [{description: 'Send updated contract', done: false}],
    transcriptPreview: [{speaker: 'Client', line: 'We are happy to proceed with phase 2.'}],
    createdBy: 'Ana Popescu',
    createdAt: 'Jun 20, 2026 14:40',
    updatedAt: 'Jun 20, 2026 15:50',
    aiModel: 'GPT-4.1',
    aiGeneratedOn: 'Jun 20, 2026 15:50',
  },
  {
    id: 5,
    title: 'Team Stand-up',
    date: 'Jun 19, 2026',
    time: '09:30 - 09:45',
    status: 'Processing',
    description: 'Daily stand-up meeting.',
    attendeesCount: 5,
    summary: '',
    keyPoints: [],
    decisions: [],
    followUpNotes: [],
    actionItems: [],
    transcriptPreview: [],
    createdBy: 'Ana Popescu',
    createdAt: 'Jun 19, 2026 09:30',
    updatedAt: 'Jun 19, 2026 09:46',
    aiModel: 'GPT-4.1',
    aiGeneratedOn: '—',
  },
  {
    id: 6,
    title: 'Weekly Planning',
    date: 'Jun 18, 2026',
    time: '10:00 - 10:30',
    status: 'Pending',
    description: 'Weekly planning meeting, not yet processed.',
    attendeesCount: 4,
    summary: '',
    keyPoints: [],
    decisions: [],
    followUpNotes: [],
    actionItems: [],
    transcriptPreview: [],
    createdBy: 'Ana Popescu',
    createdAt: 'Jun 18, 2026 09:55',
    updatedAt: 'Jun 18, 2026 09:55',
    aiModel: '—',
    aiGeneratedOn: '—',
  },
  {
    id: 7,
    title: 'Kickoff Meeting',
    date: 'Jun 15, 2026',
    time: '14:00 - 15:00',
    status: 'Completed',
    description: 'Project kickoff meeting with the full team.',
    attendeesCount: 8,
    summary: 'Project goals, scope and timeline were aligned.',
    keyPoints: ['Project goals confirmed.'],
    decisions: ['Sprint 0 starts next week.'],
    followUpNotes: ['Ana to send kickoff summary to all stakeholders.'],
    actionItems: [{description: 'Send kickoff summary', done: true}],
    transcriptPreview: [{speaker: 'Ana', line: "Welcome everyone, let's go over the project goals."}],
    createdBy: 'Ana Popescu',
    createdAt: 'Jun 15, 2026 13:40',
    updatedAt: 'Jun 15, 2026 15:10',
    aiModel: 'GPT-4.1',
    aiGeneratedOn: 'Jun 15, 2026 15:10',
  },
];

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatSidenavModule,
    MatToolbarModule,
    MatListModule,
    MatCardModule,
    MatTabsModule,
    MatIconModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatChipsModule,
    MatPaginatorModule,
    MatBadgeModule,
    MatMenuModule,
    MatProgressBarModule,
    MatDividerModule,
    MatTooltipModule,
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent {
  navItems = NAV_ITEMS;
  meetings: Meeting[] = MOCK_MEETINGS;
  searchTerm = '';

  pageIndex = 0;
  pageSize = 7;
  totalMeetings = 24;

  selectedMeetingId = this.meetings[0].id;

  processingCreditsUsed = 85;
  processingCreditsLimit = 100;

  get selectedMeeting(): Meeting {
    return this.meetings.find((m) => m.id === this.selectedMeetingId) ?? this.meetings[0];
  }

  get filteredMeetings(): Meeting[] {
    const term = this.searchTerm.trim().toLowerCase();
    if (!term) return this.meetings;
    return this.meetings.filter((m) => m.title.toLowerCase().includes(term));
  }

  get openActionItemsCount(): number {
    return this.selectedMeeting.actionItems.filter((a) => !a.done).length;
  }

  get doneActionItemsCount(): number {
    return this.selectedMeeting.actionItems.filter((a) => a.done).length;
  }

  get processingCreditsPercent(): number {
    return Math.round((this.processingCreditsUsed / this.processingCreditsLimit) * 100);
  }

  selectMeeting(id: number): void {
    this.selectedMeetingId = id;
  }

  toggleActionItem(item: ActionItem): void {
    item.done = !item.done;
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
  }

  statusColor(status: MeetingStatus): 'primary' | 'accent' | 'warn' | undefined {
    switch (status) {
      case 'Completed':
        return 'primary';
      case 'Processing':
        return 'accent';
      case 'Processed':
        return undefined;
      case 'Pending':
      default:
        return undefined;
    }
  }

  statusClass(status: MeetingStatus): string {
    switch (status) {
      case 'Completed':
        return 'status-chip status-chip--completed';
      case 'Processed':
        return 'status-chip status-chip--processed';
      case 'Processing':
        return 'status-chip status-chip--processing';
      case 'Pending':
        return 'status-chip status-chip--pending';
      default:
        return 'status-chip';
    }
  }
}
