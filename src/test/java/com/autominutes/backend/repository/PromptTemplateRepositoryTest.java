package com.autominutes.backend.repository;

import com.autominutes.backend.entity.PromptTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PromptTemplateRepositoryTest {

    @Autowired
    private PromptTemplateRepository promptTemplateRepository;

    @Test
    void findByName_returnsSeededDefaultTemplate() {
        Optional<PromptTemplate> result = promptTemplateRepository.findByName("default_summary");

        assertThat(result).isPresent();
        assertThat(result.get().getTemplateText()).contains("{transcript}");
    }

    @Test
    void findByName_returnsEmptyForUnknownName() {
        assertThat(promptTemplateRepository.findByName("nonexistent_template")).isEmpty();
    }
}
