package com.davendra.calistrack_backend.catalog.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/**
 * Directed skill graph edge: {@code fromNode} is a prerequisite of {@code toNode}.
 */
@Entity
@Table(
		name = "node_edge",
		uniqueConstraints = @UniqueConstraint(
				name = "uq_node_edge",
				columnNames = {"from_node_id", "to_node_id"}
		)
)
@Getter
@Setter
public class NodeEdge {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "from_node_id", nullable = false)
	private Node fromNode;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "to_node_id", nullable = false)
	private Node toNode;

	@Column(name = "relation_type", nullable = false, length = 20)
	private String relationType = "PREREQUISITE";

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@PrePersist
	void onCreate() {
		createdAt = Instant.now();
	}
}
