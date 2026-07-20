package com.davendra.calistrack_backend.user.mapper;

import com.davendra.calistrack_backend.catalog.entity.Node;
import com.davendra.calistrack_backend.user.dto.GoalNodeSummary;
import com.davendra.calistrack_backend.user.dto.MeResponse;
import com.davendra.calistrack_backend.user.entity.AppUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserProfileMapper {

	@Mapping(source = "currentGoalNode", target = "goal")
	MeResponse toMeResponse(AppUser user);

	GoalNodeSummary toGoalSummary(Node node);
}
