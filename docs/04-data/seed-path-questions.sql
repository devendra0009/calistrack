-- Create + seed path_question for existing DBs that already have node / node_edge.
-- Safe to re-run.

CREATE TABLE IF NOT EXISTS path_question (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    goal_node_id    UUID NOT NULL REFERENCES node (id),
    node_id         UUID NOT NULL REFERENCES node (id),
    prompt          TEXT NOT NULL,
    answer_type     VARCHAR(20) NOT NULL CHECK (answer_type IN ('REPS', 'YES_NO')),
    sort_order      INT NOT NULL CHECK (sort_order >= 1),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_path_question_goal_sort UNIQUE (goal_node_id, sort_order),
    CONSTRAINT uq_path_question_goal_node UNIQUE (goal_node_id, node_id)
);

-- Optional cleanup if you created the old redundant table earlier
DROP TABLE IF EXISTS goal_path_node;

INSERT INTO path_question (goal_node_id, node_id, prompt, answer_type, sort_order)
SELECT v.goal_node_id, v.node_id, v.prompt, v.answer_type, v.sort_order
FROM (VALUES
    ('22222222-2222-2222-2222-222222220010'::uuid, '22222222-2222-2222-2222-222222220001'::uuid,
     'How many Australian Pull-ups can you do?', 'REPS', 1),
    ('22222222-2222-2222-2222-222222220010'::uuid, '22222222-2222-2222-2222-222222220005'::uuid,
     'Can you do 5 Pull-ups?', 'YES_NO', 2),
    ('22222222-2222-2222-2222-222222220010'::uuid, '22222222-2222-2222-2222-222222220006'::uuid,
     'Can you do 10 Pull-ups?', 'YES_NO', 3)
) AS v(goal_node_id, node_id, prompt, answer_type, sort_order)
WHERE NOT EXISTS (
    SELECT 1 FROM path_question q
    WHERE q.goal_node_id = v.goal_node_id AND q.node_id = v.node_id
);
