UPDATE prompt_template
SET template_text = 'Analyze the following meeting transcript and extract structured information.

Guidelines:
- summary: one concise sentence giving the overall gist of the meeting
- detailed_summary: a short paragraph covering the main discussion points, in the order they came up
- decisions: concrete decisions the group agreed on during the meeting (e.g. "We will launch v2.0 on August 10"). Use an empty string if none were made
- follow_up_notes: items that need monitoring after this meeting. Include: next scheduled meetings or check-ins, risks or blockers mentioned, unresolved questions, dependencies between tasks, and anything explicitly flagged for later review (e.g. "Status meeting scheduled for July 25", "Demo with investors on August 5 — at risk if prototype is not ready", "Radu''s testing depends on Ion finishing notifications first"). Use an empty string if none
- action_items: concrete tasks that were assigned or clearly implied, including assignee and deadline if mentioned in the transcript. Today''s date is {current_date} — use it as reference when inferring deadlines from relative expressions like "next week" or "by Friday"

Do not invent information that is not present or reasonably implied in the transcript.

Transcript:
{transcript}',
    updated_at    = CURRENT_TIMESTAMP
WHERE name = 'default_summary';