-- Indexes for assessment path load and related progress lookups.
-- Catalog is small today; these keep queries cheap as user history grows.

CREATE INDEX IF NOT EXISTS idx_assessment_user_node_created
	ON assessment (user_id, node_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_node_edge_to_node
	ON node_edge (to_node_id);

CREATE INDEX IF NOT EXISTS idx_user_plan_enrollment_user_status
	ON user_plan_enrollment (user_id, status);
