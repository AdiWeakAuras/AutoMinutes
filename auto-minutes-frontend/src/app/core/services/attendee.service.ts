import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

import {environment} from '../../../environments/environment';
import {AttendeeDTO, AttendeeCreateRequest, AttendeeUpdateRequest} from '../models/api.model';

@Injectable({providedIn: 'root'})
export class AttendeeService {
  constructor(private http: HttpClient) {
  }

  private urlFor(meetingId: number): string {
    return `${environment.apiUrl}/meetings/${meetingId}/attendees`;
  }

  getAttendeesForMeeting(meetingId: number): Observable<AttendeeDTO[]> {
    return this.http.get<AttendeeDTO[]>(this.urlFor(meetingId));
  }

  getAttendeeById(meetingId: number, attendeeId: number): Observable<AttendeeDTO> {
    return this.http.get<AttendeeDTO>(`${this.urlFor(meetingId)}/${attendeeId}`);
  }

  addAttendee(meetingId: number, request: AttendeeCreateRequest): Observable<AttendeeDTO> {
    return this.http.post<AttendeeDTO>(this.urlFor(meetingId), request);
  }

  updateAttendee(
    meetingId: number,
    attendeeId: number,
    request: AttendeeUpdateRequest
  ): Observable<AttendeeDTO> {
    return this.http.patch<AttendeeDTO>(`${this.urlFor(meetingId)}/${attendeeId}`, request);
  }

  removeAttendee(meetingId: number, attendeeId: number): Observable<void> {
    return this.http.delete<void>(`${this.urlFor(meetingId)}/${attendeeId}`);
  }
}
