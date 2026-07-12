package com.autominutes.backend.service;

import com.autominutes.backend.dto.MeetingCreateRequest;
import com.autominutes.backend.dto.MeetingDTO;
import com.autominutes.backend.dto.MeetingUpdateRequest;

import java.util.List;
import java.util.Optional;

/**
 * Serviciu pentru gestionarea meeting-urilor (creare, citire, actualizare, stergere).
 * <p>
 * Acopera cerinta 3.1 din specificatia backend-ului (Meeting Management).
 */
public interface MeetingService {

    /**
     * Returneaza toate meeting-urile din sistem.
     *
     * @return lista de meeting-uri mapate ca {@link MeetingDTO}, poate fi goala daca nu exista niciunul
     */
    List<MeetingDTO> getAllMeetings();

    /**
     * Cauta un meeting dupa id.
     *
     * @param id id-ul meeting-ului cautat
     * @return un {@link Optional} continand DTO-ul meeting-ului daca exista, altfel gol
     */
    Optional<MeetingDTO> getMeetingById(Long id);

    /**
     * Creeaza un meeting nou pe baza datelor primite de la client.
     *
     * @param request datele de intrare validate (titlu, descriere, data)
     * @return meeting-ul nou creat, cu id-ul generat de baza de date
     */
    MeetingDTO createMeeting(MeetingCreateRequest request);

    /**
     * Actualizeaza partial un meeting existent. Campurile nesetate (null) in request
     * raman neschimbate fata de valorile existente in baza de date.
     *
     * @param id      id-ul meeting-ului de actualizat
     * @param request campurile de actualizat (orice camp null e ignorat)
     * @return meeting-ul actualizat
     * @throws com.autominutes.backend.exception.ResourceNotFoundException daca nu exista un meeting cu acest id
     */
    MeetingDTO updateMeeting(Long id, MeetingUpdateRequest request);

    /**
     * Sterge un meeting. Datorita relatiilor cu {@code ON DELETE CASCADE} din schema,
     * sterge automat si transcript-ul, attendee-legaturile si rezultatele AI asociate.
     *
     * @param id id-ul meeting-ului de sters
     * @throws com.autominutes.backend.exception.ResourceNotFoundException daca nu exista un meeting cu acest id
     */
    void deleteMeeting(Long id);
}