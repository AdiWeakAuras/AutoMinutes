package com.autominutes.backend.service;

import com.autominutes.backend.dto.AttendeeCreateRequest;
import com.autominutes.backend.dto.AttendeeDTO;
import com.autominutes.backend.dto.AttendeeUpdateRequest;

import java.util.List;

/**
 * Serviciu pentru gestionarea attendee-lor asociati unui meeting.
 * <p>
 * Acopera cerinta 3.2 din specificatia backend-ului (Attendee Management).
 * Relatia meeting-attendee este many-to-many: acelasi attendee (identificat unic
 * prin email) poate fi asociat mai multor meeting-uri, prin tabela de jonctiune
 * {@code meeting_attendee}.
 */
public interface AttendeeService {

    /**
     * Listeaza toti attendees asociati unui meeting.
     *
     * @param meetingId id-ul meeting-ului
     * @return lista de attendees, poate fi goala
     * @throws com.autominutes.backend.exception.ResourceNotFoundException daca meeting-ul nu exista
     */
    List<AttendeeDTO> getAttendeesForMeeting(Long meetingId);

    /**
     * Returneaza un attendee specific, doar daca e asociat meeting-ului dat.
     *
     * @param meetingId  id-ul meeting-ului
     * @param attendeeId id-ul attendee-ului
     * @return DTO-ul attendee-ului
     * @throws com.autominutes.backend.exception.ResourceNotFoundException daca legatura meeting-attendee nu exista
     */
    AttendeeDTO getAttendeeForMeeting(Long meetingId, Long attendeeId);

    /**
     * Adauga un attendee la un meeting. Daca exista deja un attendee cu acelasi email
     * in sistem (indiferent de alte meeting-uri), acesta este refolosit (nu se creeaza duplicat);
     * se creeaza doar legatura noua in {@code meeting_attendee}.
     *
     * @param meetingId id-ul meeting-ului la care se adauga attendee-ul
     * @param request   datele attendee-ului (nume, email obligatoriu, rol)
     * @return attendee-ul (nou creat sau refolosit)
     * @throws com.autominutes.backend.exception.ResourceNotFoundException  daca meeting-ul nu exista
     * @throws com.autominutes.backend.exception.DuplicateResourceException daca attendee-ul e deja asociat acestui meeting
     */
    AttendeeDTO addAttendeeToMeeting(Long meetingId, AttendeeCreateRequest request);

    /**
     * Actualizeaza partial detaliile unui attendee. Campurile nesetate (null) raman neschimbate.
     * Modificarea afecteaza attendee-ul global (deci si aparitiile lui la alte meeting-uri).
     *
     * @param meetingId  id-ul meeting-ului (folosit pentru validarea legaturii)
     * @param attendeeId id-ul attendee-ului de actualizat
     * @param request    campurile de actualizat
     * @return attendee-ul actualizat
     * @throws com.autominutes.backend.exception.ResourceNotFoundException daca legatura meeting-attendee nu exista
     */
    AttendeeDTO updateAttendee(Long meetingId, Long attendeeId, AttendeeUpdateRequest request);

    /**
     * Scoate un attendee dintr-un meeting (sterge doar legatura din {@code meeting_attendee}).
     * Entitatea {@code Attendee} in sine nu este stearsa, ramanand valabila pentru alte meeting-uri.
     *
     * @param meetingId  id-ul meeting-ului
     * @param attendeeId id-ul attendee-ului de scos
     * @throws com.autominutes.backend.exception.ResourceNotFoundException daca legatura meeting-attendee nu exista
     */
    void removeAttendeeFromMeeting(Long meetingId, Long attendeeId);
}