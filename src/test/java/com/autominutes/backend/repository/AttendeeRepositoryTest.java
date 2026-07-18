package com.autominutes.backend.repository;

import com.autominutes.backend.entity.Attendee;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AttendeeRepositoryTest {

    @Autowired
    private AttendeeRepository attendeeRepository;

    @Test
    void findByEmail_returnsSeededAttendee() {
        Optional<Attendee> result = attendeeRepository.findByEmail("ana.popescu@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Ana Popescu");
        assertThat(result.get().getRole()).isEqualTo("PROJECT_MANAGER");
    }

    @Test
    void findByEmail_returnsEmptyForUnknownEmail() {
        assertThat(attendeeRepository.findByEmail("nobody@example.com")).isEmpty();
    }

    @Test
    void save_persistsNewAttendee() {
        Attendee attendee = new Attendee();
        attendee.setName("New Person");
        attendee.setEmail("new.person@example.com");
        attendee.setRole("OBSERVER");

        Attendee saved = attendeeRepository.save(attendee);

        assertThat(saved.getId()).isNotNull();
        assertThat(attendeeRepository.findByEmail("new.person@example.com")).isPresent();
    }
}
