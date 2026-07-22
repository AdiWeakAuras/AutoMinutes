import {Routes} from '@angular/router';
import {ShellComponent} from './layout/shell.component';
import {DashboardPageComponent} from './pages/dashboard/dashboard.component';
import {MeetingsPageComponent} from './pages/meetings/meetings.component';
import {ActionItemsPageComponent} from './pages/action-items/action-items.component';
import {AttendeesPageComponent} from './pages/attendees/attendees.component';

export const routes: Routes = [
  {
    path: '',
    component: ShellComponent,
    children: [
      {path: '', redirectTo: 'dashboard', pathMatch: 'full'},
      {path: 'dashboard', component: DashboardPageComponent},
      {path: 'meetings', component: MeetingsPageComponent},
      {path: 'action-items', component: ActionItemsPageComponent},
      {path: 'attendees', component: AttendeesPageComponent},
    ],
  },
];
