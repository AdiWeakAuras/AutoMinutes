-- ==========================================================
-- Test data for AutoMinutes
-- ==========================================================

-- ==========================================================
-- ATTENDEES
-- ==========================================================
INSERT INTO attendee (name, email, role)
VALUES ('Ana Popescu', 'ana.popescu@example.com', 'PROJECT_MANAGER'),
       ('Radu Ionescu', 'radu.ionescu@example.com', 'DEVELOPER'),
       ('Maria Dumitrescu', 'maria.d@example.com', 'STAKEHOLDER');

-- ==========================================================
-- PROMPT TEMPLATES
-- ==========================================================
INSERT INTO prompt_template
(name,
 template_text,
 description)
VALUES ('default_summary',
        'Analyze the following meeting transcript and extract structured information.

Guidelines:
- summary: one concise sentence giving the overall gist of the meeting
- detailed_summary: a short paragraph covering the main discussion points, in the order they came up
- decisions: concrete decisions the group agreed on during the meeting. Use an empty string if none were made
- follow_up_notes: anything explicitly deferred, scheduled, or flagged for a future meeting or check-in. Use an empty string if none
- action_items: concrete tasks that were assigned or clearly implied, including assignee and deadline if mentioned in the transcript

Do not invent information that is not present or reasonably implied in the transcript.

Transcript:
{transcript}',
        'Default prompt for summarization');

-- ==========================================================
-- MEETINGS
-- ==========================================================
INSERT INTO meeting
(title,
 description,
 meeting_date,
 processing_status)
VALUES ('Sprint Planning',
        'Sprint 12 planning meeting',
        '2026-07-01 10:00:00',
        'PENDING'),
       ('Q2 Retrospective',
        'Second quarter retrospective meeting',
        '2026-07-03 14:00:00',
        'DONE');

-- ==========================================================
-- TRANSCRIPTS
-- ==========================================================
INSERT INTO transcript
(meeting_id,
 content)
VALUES (1,
        'Ana: Let''s begin the sprint planning. Radu: I will take the backend tasks.'),
       (2,
        'Maria: Q2 was successful. Ana: We delivered every planned feature on time.');

-- ==========================================================
-- MEETING ATTENDEES
-- ==========================================================
INSERT INTO meeting_attendee
(meeting_id,
 attendee_id)
VALUES (1, 1),
       (1, 2),
       (2, 1),
       (2, 3);

-- ==========================================================
-- AI RESULTS
-- ==========================================================
INSERT INTO ai_result
(transcript_id,
 prompt_template_id,
 summary,
 detailed_summary,
 decisions,
 follow_up_notes)
VALUES (2,
        1,
        'Q2 sprint was a success.',
        'All planned tasks were completed successfully and delivered on time.',
        'The team will continue using the current development workflow.',
        'Ana will prepare the Sprint Planning meeting for Q3.');

-- ==========================================================
-- ACTION ITEMS
-- ==========================================================
INSERT INTO action_item
(ai_result_id,
 description,
 proposed_assignee,
 deadline,
 status)
VALUES (1,
        'Prepare the Sprint Planning for Q3',
        'Ana Popescu',
        DATE '2026-07-10',
        'OPEN'),
       (1,
        'Configure the staging environment',
        'Radu Ionescu',
        DATE '2026-07-08',
        'IN_PROGRESS');