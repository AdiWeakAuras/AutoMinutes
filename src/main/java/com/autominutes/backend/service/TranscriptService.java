package com.autominutes.backend.service;

import com.autominutes.backend.dto.TranscriptCreateRequest;
import com.autominutes.backend.dto.TranscriptDTO;
import com.autominutes.backend.dto.TranscriptUpdateRequest;

/**
 * Serviciu pentru gestionarea transcript-ului unui meeting.
 * <p>
 * Acopera cerinta 3.3 din specificatia backend-ului (Transcript Storage).
 * Relatia meeting-transcript este 1-la-1: un meeting poate avea cel mult un transcript.
 */
public interface TranscriptService {

    /**
     * Returneaza transcript-ul asociat unui meeting.
     *
     * @param meetingId id-ul meeting-ului
     * @return DTO-ul transcript-ului
     * @throws com.autominutes.backend.exception.ResourceNotFoundException daca meeting-ul nu exista
     *                                                                     sau nu are niciun transcript asociat
     */
    TranscriptDTO getTranscriptForMeeting(Long meetingId);

    /**
     * Inregistreaza transcript-ul brut al unui meeting. Poate fi apelat o singura data
     * per meeting; pentru corectii ulterioare se foloseste {@link #updateTranscript}.
     *
     * @param meetingId id-ul meeting-ului
     * @param request   continutul transcript-ului (obligatoriu, poate fi text lung)
     * @return transcript-ul nou creat
     * @throws com.autominutes.backend.exception.ResourceNotFoundException  daca meeting-ul nu exista
     * @throws com.autominutes.backend.exception.DuplicateResourceException daca meeting-ul are deja un transcript
     */
    TranscriptDTO submitTranscript(Long meetingId, TranscriptCreateRequest request);

    /**
     * Inlocuieste integral continutul unui transcript existent.
     *
     * @param meetingId id-ul meeting-ului
     * @param request   noul continut (obligatoriu)
     * @return transcript-ul actualizat
     * @throws com.autominutes.backend.exception.ResourceNotFoundException daca meeting-ul nu exista
     *                                                                     sau nu are niciun transcript asociat
     */
    TranscriptDTO updateTranscript(Long meetingId, TranscriptUpdateRequest request);
}