--  adaugam coloana noua, nullable pentru moment (ca sa putem completa datele mai intai)
ALTER TABLE ai_result ADD COLUMN transcript_id BIGINT;

--  completam transcript_id pe baza relatiei existente meeting <-> transcript
-- (fiecare meeting are un singur transcript, deci gasim transcript-ul prin meeting_id)
UPDATE ai_result ar
SET transcript_id = (
    SELECT t.id
    FROM transcript t
    WHERE t.meeting_id = ar.meeting_id
);

--  acum ca toate rândurile au transcript_id completat, il facem obligatoriu
ALTER TABLE ai_result ALTER COLUMN transcript_id SET NOT NULL;

-- Pasul 4: adaugam foreign key catre transcript
ALTER TABLE ai_result
    ADD CONSTRAINT fk_ai_result_transcript FOREIGN KEY (transcript_id) REFERENCES transcript(id) ON DELETE CASCADE;

-- scoatem vechea foreign key si coloana meeting_id, nu mai sunt necesare
ALTER TABLE ai_result DROP CONSTRAINT fk_ai_result_meeting;
ALTER TABLE ai_result DROP COLUMN meeting_id;

-- actualizam indexul (stergem vechiul index pe meeting_id, cream unul nou pe transcript_id)
DROP INDEX IF EXISTS idx_ai_result_meeting;
CREATE INDEX idx_ai_result_transcript ON ai_result(transcript_id);