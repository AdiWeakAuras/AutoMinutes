-- AutoMinutes Meeting Management System
-- Initial schema matching JPA entities

-- ==========================================================
-- ATTENDEE
-- ==========================================================
CREATE TABLE attendee
(
    id BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255)                        NOT NULL,
    email      VARCHAR(255),
    role       VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- ==========================================================
-- PROMPT_TEMPLATE
-- ==========================================================
CREATE TABLE prompt_template
(
    id BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255)                        NOT NULL,
    template_text TEXT                                NOT NULL,
    description   TEXT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- ==========================================================
-- MEETING
-- ==========================================================
CREATE TABLE meeting
(
    id BIGSERIAL PRIMARY KEY,
    title             VARCHAR(255)                          NOT NULL,
    description       TEXT,
    meeting_date      TIMESTAMP                             NOT NULL,

    processing_status VARCHAR(50) DEFAULT 'PENDING'         NOT NULL,

    created_at        TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at        TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT ck_processing_status
        CHECK (
            processing_status IN
            (
             'PENDING',
             'PROCESSING',
             'DONE',
             'FAILED'
                )
            )
);

-- ==========================================================
-- TRANSCRIPT
-- ==========================================================
CREATE TABLE transcript
(
    id BIGSERIAL PRIMARY KEY,

    meeting_id BIGINT                              NOT NULL,

    content    TEXT                                NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT fk_transcript_meeting
        FOREIGN KEY (meeting_id)
            REFERENCES meeting (id)
            ON DELETE CASCADE,

    CONSTRAINT uk_transcript_meeting
        UNIQUE (meeting_id)
);

-- ==========================================================
-- MEETING_ATTENDEE
-- ==========================================================
CREATE TABLE meeting_attendee
(

    id BIGSERIAL PRIMARY KEY,

    meeting_id  BIGINT                              NOT NULL,

    attendee_id BIGINT                              NOT NULL,

    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT fk_meeting_attendee_meeting
        FOREIGN KEY (meeting_id)
            REFERENCES meeting (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_meeting_attendee_attendee
        FOREIGN KEY (attendee_id)
            REFERENCES attendee (id)
            ON DELETE CASCADE,

    CONSTRAINT uk_meeting_attendee
        UNIQUE (meeting_id, attendee_id)
);

-- ==========================================================
-- AI_RESULT
-- ==========================================================
CREATE TABLE ai_result
(

    id BIGSERIAL PRIMARY KEY,

    transcript_id      BIGINT                              NOT NULL,

    prompt_template_id BIGINT                              NOT NULL,

    summary            TEXT,

    detailed_summary   TEXT,

    decisions          TEXT,

    follow_up_notes    TEXT,

    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT fk_ai_result_transcript
        FOREIGN KEY (transcript_id)
            REFERENCES transcript (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_ai_result_prompt_template
        FOREIGN KEY (prompt_template_id)
            REFERENCES prompt_template (id)
            ON DELETE RESTRICT
);

-- ==========================================================
-- ACTION_ITEM
-- ==========================================================
CREATE TABLE action_item
(

    id BIGSERIAL PRIMARY KEY,

    ai_result_id      BIGINT                                NOT NULL,

    description       VARCHAR(500)                          NOT NULL,

    proposed_assignee VARCHAR(255),

    deadline          DATE,

    status            VARCHAR(50) DEFAULT 'OPEN'            NOT NULL,

    created_at        TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,

    updated_at        TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT fk_action_item_ai_result
        FOREIGN KEY (ai_result_id)
            REFERENCES ai_result (id)
            ON DELETE CASCADE,

    CONSTRAINT ck_action_item_status
        CHECK (
            status IN
            (
             'OPEN',
             'IN_PROGRESS',
             'DONE',
             'UNKNOWN'
                )
            )
);

-- ==========================================================
-- INDEXES
-- ==========================================================

CREATE INDEX idx_meeting_date
    ON meeting (meeting_date);

CREATE INDEX idx_meeting_processing_status
    ON meeting (processing_status);

CREATE INDEX idx_meeting_attendee_meeting
    ON meeting_attendee (meeting_id);

CREATE INDEX idx_meeting_attendee_attendee
    ON meeting_attendee (attendee_id);

CREATE INDEX idx_ai_result_transcript
    ON ai_result (transcript_id);

CREATE INDEX idx_ai_result_prompt_template
    ON ai_result (prompt_template_id);

CREATE INDEX idx_action_item_ai_result
    ON action_item (ai_result_id);

CREATE INDEX idx_action_item_status
    ON action_item (status);