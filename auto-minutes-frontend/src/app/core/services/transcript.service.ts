import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

import {environment} from '../../../environments/environment';
import {TranscriptDTO, TranscriptCreateRequest, TranscriptUpdateRequest} from '../models/api.model';

@Injectable({providedIn: 'root'})
export class TranscriptService {
  constructor(private http: HttpClient) {
  }

  private urlFor(meetingId: number): string {
    return `${environment.apiUrl}/meetings/${meetingId}/transcript`;
  }

  getTranscript(meetingId: number): Observable<TranscriptDTO> {
    return this.http.get<TranscriptDTO>(this.urlFor(meetingId));
  }

  submitTranscript(meetingId: number, request: TranscriptCreateRequest): Observable<TranscriptDTO> {
    return this.http.post<TranscriptDTO>(this.urlFor(meetingId), request);
  }

  updateTranscript(meetingId: number, request: TranscriptUpdateRequest): Observable<TranscriptDTO> {
    return this.http.put<TranscriptDTO>(this.urlFor(meetingId), request);
  }
}
