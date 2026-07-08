INSERT INTO attendee (name, email, role) VALUES
('Ana Popescu', 'ana.popescu@example.com', 'PROJECT_MANAGER'),
('Radu Ionescu', 'radu.ionescu@example.com', 'DEVELOPER'),
('Maria Dumitrescu', 'maria.d@example.com', 'STAKEHOLDER');

INSERT INTO prompt_template (name, template_text, description) VALUES
('default_summary', 'Summarize the following meeting transcript: {transcript}', 'Default prompt for summarization');

INSERT INTO meeting (title, description, meeting_date, processing_status) VALUES
('Sprint Planning', 'Sprint 12 planning', '2026-07-01 10:00:00', 'PENDING'),
('Q2 Retrospective', 'Second quarter retrospective meeting', '2026-07-03 14:00:00', 'COMPLETED');

INSERT INTO transcript (meeting_id, content) VALUES
(1, 'Ana: Let''s begin the sprint planning... Radu: Okay, I''ll take the backend task...'),
(2, 'Maria: What went well during Q2... Ana: We delivered all planned features on time...');

UPDATE meeting SET transcript_id = 1 WHERE id = 1;
UPDATE meeting SET transcript_id = 2 WHERE id = 2;

INSERT INTO meeting_attendee (meeting_id, attendee_id) VALUES
(1, 1), (1, 2),
(2, 1), (2, 3);

INSERT INTO ai_result (meeting_id, prompt_template_id, summary, detailed_summary, decisions, follow_up_notes) VALUES
(2, 1, 'Q2 sprint was a success', 'All 8 planned tasks were completed and delivered on schedule.', 'We will continue with the same development pace in Q3.', 'Ana will prepare the Q3 sprint plan.');

INSERT INTO action_item (ai_result_id, description, proposed_assignee, deadline, status) VALUES
(1, 'Prepare the sprint plan for Q3', 'Ana Popescu', '2026-07-10', 'OPEN'),
(1, 'Configure the staging environment', 'Radu Ionescu', '2026-07-08', 'IN_PROGRESS');