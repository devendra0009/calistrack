package com.davendra.calistrack_backend.path.entity;

import com.davendra.calistrack_backend.catalog.entity.Node;
import com.davendra.calistrack_backend.path.enums.PlacementAnswerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
		name = "path_question",
		uniqueConstraints = {
				@UniqueConstraint(name = "uq_path_question_goal_sort", columnNames = {"goal_node_id", "sort_order"}),
				@UniqueConstraint(name = "uq_path_question_goal_node", columnNames = {"goal_node_id", "node_id"})
		}
)
@Getter
@Setter
public class PathQuestionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "goal_node_id", nullable = false)
	private Node goalNode;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "node_id", nullable = false)
	private Node node;

	@Column(nullable = false, columnDefinition = "text")
	private String prompt;

	@Enumerated(EnumType.STRING)
	@Column(name = "answer_type", nullable = false, length = 20)
	private PlacementAnswerType answerType;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@PrePersist
	void onCreate() {
		createdAt = Instant.now();
	}
}
