import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

import {environment} from '../../../environments/environment';
import {
  MeetingDTO,
  MeetingSummaryDTO,
  MeetingCreateRequest,
  MeetingUpdateRequest,
} from '../models/api.model';

@Injectable({providedIn: 'root'})
export class MeetingService {
  private readonly baseUrl = `${environment.apiUrl}/meetings`;

  constructor(private http: HttpClient) {
  }

  getAllMeetings(): Observable<MeetingSummaryDTO[]> {
    return this.http.get<MeetingSummaryDTO[]>(this.baseUrl);
  }

  getMeetingById(id: number): Observable<MeetingDTO> {
    return this.http.get<MeetingDTO>(`${this.baseUrl}/${id}`);
  }

  createMeeting(request: MeetingCreateRequest): Observable<MeetingDTO> {
    return this.http.post<MeetingDTO>(this.baseUrl, request);
  }

  updateMeeting(id: number, request: MeetingUpdateRequest): Observable<MeetingDTO> {
    return this.http.patch<MeetingDTO>(`${this.baseUrl}/${id}`, request);
  }

  deleteMeeting(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
