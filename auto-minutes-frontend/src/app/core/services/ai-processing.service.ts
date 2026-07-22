import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

import {environment} from '../../../environments/environment';
import {AIResultDTO, ProcessMeetingRequest} from '../models/api.model';

@Injectable({providedIn: 'root'})
export class AiProcessingService {
  constructor(private http: HttpClient) {
  }

  processTranscript(meetingId: number, request?: ProcessMeetingRequest): Observable<AIResultDTO> {
    return this.http.post<AIResultDTO>(
      `${environment.apiUrl}/meetings/${meetingId}/process`,
      request ?? {}
    );
  }

  getAiResults(meetingId: number): Observable<AIResultDTO[]> {
    return this.http.get<AIResultDTO[]>(`${environment.apiUrl}/meetings/${meetingId}/ai-results`);
  }
}
